%% get image data
if ~exist('imageEvent', 'var') || isempty(imageEvent) || ~exist('detectionResult', 'var') || isempty(detectionResult)
    error('GlowVisualizer:ParametersMissing', 'Either imageEvent or detectionResult are not defined.');
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
    error('GlowVisualizer:ImageTypeNotSupported', 'Only support 8 and 16 bit grayscale images.')
end

%% Process table data
table = detectionResult.getCellTable();
xposColumn = table.getColumnView('cell.center.x');
yposColumn = table.getColumnView('cell.center.y');
areaColumn = table.getColumnView('cell.area');
try
    quantColumn = table.getColumnView('quantification_image');
catch exception
    quantColumn = [];
end
% Create matrix, with first element being the x, second the y-position, and third the area.
cellPositions = zeros(table.getNumRows(), 3);
currentElement = 1;
for row = 1:table.getNumRows()
    if ~isempty(quantColumn)
        % Only take first appearance of cell.
        quant = quantColumn.getValue(row-1);
        if ~isempty(quant) && quant ~= 0
            continue;
        end
    end
    cellPositions(currentElement,1) = xposColumn.getValue(row-1);
    cellPositions(currentElement,2) = yposColumn.getValue(row-1);
    cellPositions(currentElement,3) = areaColumn.getValue(row-1);
    currentElement = currentElement+1;
end
cellPositions(currentElement:end, :) = [];

%% Generate detection image
% We can not draw pixels at noninteger positions
cellPositions = round(cellPositions);

% Set basic output image colors to gray
imageRed = imageData / (1+glowStrength);
imageGreen = imageData / (1+glowStrength);
imageBlue = imageData / (1+glowStrength);
[imageHeight, imageWidth] = size(imageRed);

% Create glow image
glowWidth = round(sqrt(mean(cellPositions(:, 3))) * 3);
if mod(glowWidth, 2) == 0
    glowWidth = glowWidth + 1;
end
half = (glowWidth-1)/2;
glowImage = glowStrength * (1 - (sqrt(repmat((-half:half).^2, glowWidth, 1) + repmat((-half:half)'.^2, 1, glowWidth))) / half);
glowImage(glowImage<0) = 0;

% Highlight all cells
for i = find(~isnan(cellPositions(:, 1)') & ~isnan(cellPositions(:, 1)'))
    if cellPositions(i, 1) - half < 1
        cellPositions(i, 1) = 1+half;
    elseif cellPositions(i, 1) + half > imageWidth
        cellPositions(i, 1) = imageWidth - half;
    end
    if cellPositions(i, 2) - half < 1
        cellPositions(i, 2) = 1+half;
    elseif cellPositions(i, 2) + half > imageHeight
        cellPositions(i, 2) = imageHeight-half;
    end
    imageBlue(cellPositions(i, 2) - half : cellPositions(i, 2) + half, cellPositions(i, 1) - half : cellPositions(i, 1) + half) = uint8(double(imageBlue(cellPositions(i, 2) - half : cellPositions(i, 2) + half, cellPositions(i, 1) - half : cellPositions(i, 1) + half)) .* (1 + glowImage));
end

finalImage = cat(3, imageRed', imageGreen', imageBlue');

%% Pass generated image to YouScope
% Set alpha to maximum (no transparency).
alpha = ones(size(finalImage, 1), size(finalImage, 2)) * 255;

bitShifts = [24, 16, 8, 0];

packed      =               bitshift(uint32(alpha),                bitShifts(1));
packed      = bitor(packed, bitshift(uint32(finalImage(:, :, 1)), bitShifts(2)));
packed      = bitor(packed, bitshift(uint32(finalImage(:, :, 2)), bitShifts(3)));
finalImage = bitor(packed, bitshift(uint32(finalImage(:, :, 3)), bitShifts(4)));

imageEvent = org.youscope.common.ImageEvent.createImage(finalImage(:), size(finalImage, 1), size(finalImage, 2), 8);
imageEvent.setBands(3);
imageSink.imageMade(imageEvent);
