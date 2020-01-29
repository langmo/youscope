%% Get detection image
if ~exist('imageEvent', 'var') || isempty(imageEvent)
    error('QuickDetect:ParametersMissing', 'No imageEvent provided');
end
% Get image metadata
bytesPerPixel = imageEvent.getBytesPerPixel();
maxValue = 2^imageEvent.getBitDepth()-1;
imageType = ['uint', mat2str(8 * bytesPerPixel)];

% create matrix out of image data
imageData = reshape(typecast(imageEvent.getImageData(), imageType), imageEvent.getWidth(), imageEvent.getHeight())';

% Flip dimensions if necessary
if imageEvent.isTransposeY()
    imageData = flipud(imageData);
end
if imageEvent.isTransposeX()
    imageData = fliplr(imageData);
end
if imageEvent.isSwitchXY()
    imageData = imageData';
end

% Convert to uint8
if strcmpi(class(imageData), 'uint8') 
    % Filetype already the right one...
elseif strcmpi(class(imageData), 'uint16')
    imageData = uint8(imageData / (maxValue / uint16(intmax('uint8'))));   
elseif strcmpi(class(imageData), 'uint32')
    imageData = cast(imageData / (maxValue / uint32(intmax('uint8'))), 'uint8');   
elseif(strcmpi(class(imageData), 'int8'))
    imageData = int16(imageData);
    imageData(imageData<0) = int16(intmax('uint8')) + 1 + imageData(imageData<0);
    imageData = cast(imageData, 'uint8');
elseif(strcmpi(class(imageData), 'int16'))
    imageData = int32(imageData);
    imageData(imageData<0) = int32(intmax('uint16')) + 1 + imageData(imageData<0);
    imageData = cast(imageData / (int32(intmax('uint16')) / int32(intmax('uint8'))), 'uint16');
else
    error('QuickDetect:ImageTypeNotSupported', 'Only support 8 and 16 bit grayscale images.')
end

%% Get quantification images
if ~exist('quantImages', 'var')
    quantImages = zeros(1, 0);
end

quantImageData = cell(size(quantImages));
for i = 1:length(quantImages)
    bytesPerPixel = quantImages(i).getBytesPerPixel();
    maxValue = 2^quantImages(i).getBitDepth()-1;
    imageType = ['uint', mat2str(8 * bytesPerPixel)];
    
    % create matrix out of image data
    quantImageData{i} = reshape(typecast(quantImages(i).getImageData(), imageType), quantImages(i).getWidth(), quantImages(i).getHeight())';
    
    % Flip dimensions if necessary
    if quantImages(i).isTransposeY()
        quantImageData{i} = flipud(quantImageData{i});
    end
    if quantImages(i).isTransposeX()
        quantImageData{i} = fliplr(quantImageData{i});
    end
    if quantImages(i).isSwitchXY()
        quantImageData{i} = quantImageData{i}';
    end
    
    % Convert to uint8
    if strcmpi(class(quantImageData{i}), 'uint8') 
        % Filetype already the right one...
    elseif strcmpi(class(quantImageData{i}), 'uint16')
        quantImageData{i} = uint8(quantImageData{i} / (maxValue / uint16(intmax('uint8'))));   
    elseif strcmpi(class(quantImageData{i}), 'uint32')
        quantImageData{i} = cast(quantImageData{i} / (maxValue / uint32(intmax('uint8'))), 'uint8');   
    elseif(strcmpi(class(quantImageData{i}), 'int8'))
        quantImageData{i} = int16(quantImageData{i});
        quantImageData{i}(quantImageData{i}<0) = int16(intmax('uint8')) + 1 + quantImageData{i}(quantImageData{i}<0);
        quantImageData{i} = cast(quantImageData{i}, 'uint8');
    elseif(strcmpi(class(quantImageData{i}), 'int16'))
        quantImageData{i} = int32(quantImageData{i});
        quantImageData{i}(quantImageData{i}<0) = int32(intmax('uint16')) + 1 + quantImageData{i}(quantImageData{i}<0);
        quantImageData{i} = cast(quantImageData{i} / (int32(intmax('uint16')) / int32(intmax('uint8'))), 'uint16');
    else
        error('MicroscopeAnalysis:ImageTypeNotSupported', 'Only support 8 and 16 bit grayscale images.')
    end
end

%% Cell detection
resizeScale = 1/internalBinning;
cellSizeLowerLimit = minCellDiameter^2;
cellSizeUpperLimit = maxCellDiameter^2;

if resizeScale ~= 1
    workImage = imresize(imageData, resizeScale);
else
    workImage = imageData;
end
cellSizeLowerLimit = cellSizeLowerLimit * resizeScale^2;
cellSizeUpperLimit = cellSizeUpperLimit * resizeScale^2;

%% Filter the Pixel-Noise
workImage = medfilt2(workImage);

%% Detect edges in the image (=shadows of cell membranes)
[BWs] = edge(workImage,'sobel', threshold);

%% Dilate the Image
% The detected edges of a cell usually have holes. However, we need a
% closed boundary of the cell. Thus we are simply enlarging the edges in
% the hope that all holes are filled.
se90 = strel('line', 3, 90);
se0 = strel('line', 3, 0);
BWsdil = imdilate(BWs, [se90 se0]);

%% Fill Interior Gaps 
% We only have cell boundaries, but for the analysis we need the whole
% cells. We thus just fill everything that is completely enclosed by a cell
% membrane/edge
BWdfill = imfill(BWsdil, 'holes');

%% Filter by the Object Area
% At this point, we will (heopfully) have found most cells. However, we
% have also false detection, namely some artefacts being wrongly identified
% as cells. We now filter them out by requiering that every cell has a
% certain size.

% Gives every connected set of pixels (=cell) an own ID
[Lnew,numNew] = bwlabeln(BWdfill);
% Calculates the area and the center of every connected set of pixels
% (=cell).
statsNew = regionprops(Lnew, {'Area', 'Centroid'});
% Find objects which are not too small and not too big. These objects
% probably are cells. All others probably not.
idx = find(([statsNew.Area] > cellSizeLowerLimit)&([statsNew.Area] < cellSizeUpperLimit));
Lnew(~ismember(Lnew,idx)) = 0;

%% Get positions of the cells
if isempty(idx)
    cellPositions = zeros(0, 3);
else
    cellPositionsTemp = {statsNew(idx).Centroid}';
    cellAreaTemp = {statsNew(idx).Area}';
    cellPositions = [cell2mat(cellPositionsTemp) / resizeScale, cell2mat(cellAreaTemp) / resizeScale^2];
end

%% Delete cells that are too close to each other
numCells = size(cellPositions, 1);
cellDist = (repmat(cellPositions(:, 1)', numCells, 1) - repmat(cellPositions(:, 1), 1, numCells)).^2 +...
    (repmat(cellPositions(:, 2)', numCells, 1) - repmat(cellPositions(:, 2), 1, numCells)).^2;
[row, col] = find(cellDist < cellSizeUpperLimit *2.5);%*3
cellPositions(col(col>row), :) = [];
Lnew(ismember(Lnew, idx(col(col>row)))) = 0;

% relabel images in detection image, since due to deleting not all numbers
% are set, and they don't correspond to the cell table
[Lnew] = bwlabeln(Lnew);

% Resize detection image back to original size
if resizeScale ~= 1
    Lnew = imresize(Lnew, 1/resizeScale, 'nearest');
end

%% Correct indexing of cell positions:
% Matlab counts starting at 1, Java starting at 0
cellPositions(:, 1:2) = cellPositions(:, 1:2) - 1;

%% quantify fluorescence
if  isempty(quantImageData)
    for c=1:size(cellPositions, 1)
        tableSink.addRow(java.lang.Integer(c), ...
            [], ...
            java.lang.Double(cellPositions(c,1)), ...
            java.lang.Double(cellPositions(c,2)), ...
            java.lang.Double(cellPositions(c,3)), ...
            []);
    end
else
    for c=1:size(cellPositions, 1)
        for img=1:length(quantImageData)
            quant = mean(quantImageData{img}(Lnew(:)==c)) / double(intmax(class(quantImageData{img})));
            tableSink.addRow(java.lang.Integer(c), ...
                java.lang.Integer(img), ...
                java.lang.Double(cellPositions(c,1)), ...
                java.lang.Double(cellPositions(c,2)), ...
                java.lang.Double(cellPositions(c,3)), ...
                java.lang.Double(quant));
        end
    end
end
detectionImage = uint8(Lnew);

%% Convert detection image to YouScope image
% Matlab saves image data in the other way round than the rest of
% the world...
detectionImage = detectionImage';

if length(size(detectionImage)) == 2
    % Grayscale image
    colorImage = false;
    bytesPerPixel = 1;
    bitDepth = 8;
else
    % Color image -> Create RGBA image
    colorImage = true;
    bytesPerPixel = 4;
    bitDepth = 8;

    % Set alpha to maximum (no transparency).
    alpha = ones(size(detectionImage, 1), size(detectionImage, 2)) * 255;

    bitShifts = [24, 16, 8, 0];

    packed      =               bitshift(uint32(alpha),                bitShifts(1));
    packed      = bitor(packed, bitshift(uint32(detectionImage(:, :, 1)), bitShifts(2)));
    packed      = bitor(packed, bitshift(uint32(detectionImage(:, :, 2)), bitShifts(3)));
    detectionImage = bitor(packed, bitshift(uint32(detectionImage(:, :, 3)), bitShifts(4)));
end

%% Create YouScope image event.
detectionImageEvent = org.youscope.common.ImageEvent.createImage(detectionImage(:), size(detectionImage, 1), size(detectionImage, 2), bitDepth);
if colorImage
    detectionImageEvent.setBands(3);
end
%% Pass generated detection image to YouScope
if exist('imageSink', 'var')
    imageSink.imageMade(detectionImageEvent);
end