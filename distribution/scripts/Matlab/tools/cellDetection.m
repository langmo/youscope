function varargout = cellDetection(imageData, cellSizeLowerLimit, cellSizeUpperLimit, threshold, resizeScale, quantImageData)
% This file identifies all cells in an image.
%
% Input arguments: 
% --------------------
% imageData ... image as a two dimensional array of grayscale (uint8) pixels
% cellSizeUpperLimit ...Lower limit of cell area (in square pixels).
%                       All smaller detected objects are sorted out.
% cellSizeLowerLimit... Upper limit of cell area (in square pixels).
%                       All larger detected objects are sorted out.
% threshold ... Threshold used in the edge detection algorithm.
% resizeScale ... Resize the width and the height of the image by this factor. A lower
%               value will increase speed, but possibly lowers quality of detection.
%
% Return value
% -------------------
% Table representing the positions of all detected cells. Rows represent
% the cells, the first column is the cell's x position, the second its y
% position (in pixels), and the third its area (in square pixels).


% Copyright 2010 Moritz Lang
% This file is part of the Lemming Toolbox.
% 
% This file is free software: you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation, either version 3 of the License, or
% (at your option) any later version.
% 
% This file is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
% 
% You should have received a copy of the GNU General Public License
% along with the Lemming Toolbox.  If not, see
% <http://www.gnu.org/licenses/>.

%% Configuration
% True if intermediate images should be shown.
debugging = false;

%% Get Matlab image matrix if youscope one
if strcmp(class(imageData), 'ch.ethz.csb.youscope.shared.ImageEvent')
    imageData = toMatlabImage(imageData);
end

if ~exist('quantImageData', 'var')
    quantImageData = cell(1, 0);
elseif isjava(quantImageData)
    temp = cell(1, length(quantImageData));
    for i = 1:length(quantImageData)
        temp{i} = toMatlabImage(quantImageData(i));
    end
    quantImageData = temp;
end

%% Output original image
if debugging
    figure('Name','Original Image','NumberTitle','off'); %#ok<UNRCH>
    colormap(gray);
    imh = image(imageData);
    set(imh, 'CDataMapping', 'scaled');
    set(gca, 'Box', 'on', 'MinorGridLineStyle', 'none', 'XTick', [], 'YTick', []);
end


%% Resize image
if resizeScale ~= 1
    workImage = imresize(imageData, resizeScale);
else
    workImage = imageData;
end
cellSizeLowerLimit = cellSizeLowerLimit * resizeScale^2;
cellSizeUpperLimit = cellSizeUpperLimit * resizeScale^2;

%% Filter the Pixel-Noise
workImage = medfilt2(workImage);
if debugging
    figure('Name','Filtered Image','NumberTitle','off'); %#ok<UNRCH>
    colormap(gray);
    imh = image(workImage);
    set(imh, 'CDataMapping', 'scaled');
    set(gca, 'Box', 'on', 'MinorGridLineStyle', 'none', 'XTick', [], 'YTick', []);
end

%% Detect edges in the image (=shadows of cell membranes)
[BWs] = edge(workImage,'sobel', threshold);
if debugging
    figure('Name','Detected Edges','NumberTitle','off'); %#ok<UNRCH>
    colormap(gray);
    imh = image(BWs);
    set(imh, 'CDataMapping', 'scaled');
    set(gca, 'Box', 'on', 'MinorGridLineStyle', 'none', 'XTick', [], 'YTick', []);
end

%% Dilate the Image
% The detected edges of a cell usually have holes. However, we need a
% closed boundary of the cell. Thus we are simply enlarging the edges in
% the hope that all holes are filled.
se90 = strel('line', 3, 90);
se0 = strel('line', 3, 0);
BWsdil = imdilate(BWs, [se90 se0]);
if debugging
    figure('Name','Dilated Image','NumberTitle','off'); %#ok<UNRCH>
    colormap(gray);
    imh = image(BWsdil);
    set(imh, 'CDataMapping', 'scaled');
    set(gca, 'Box', 'on', 'MinorGridLineStyle', 'none', 'XTick', [], 'YTick', []);
end

%% Fill Interior Gaps 
% We only have cell boundaries, but for the analysis we need the whole
% cells. We thus just fill everything that is completely enclosed by a cell
% membrane/edge
BWdfill = imfill(BWsdil, 'holes');
if debugging
    figure('Name','Filled holes','NumberTitle','off'); %#ok<UNRCH>
    colormap(gray);
    imh = image(BWdfill);
    set(imh, 'CDataMapping', 'scaled');
    set(gca, 'Box', 'on', 'MinorGridLineStyle', 'none', 'XTick', [], 'YTick', []);
end

%% Filter by the Object Area
% At this point, we will (heopfully) have found most cells. However, we
% have also false detection, namely some artefacts being wrongly identified
% as cells. We now filter them out by requiering that every cell has a
% certain size.

% Gives every connected set of pixels (=cell) an own ID
[Lnew,numNew] = bwlabeln(BWdfill); %#ok<NASGU>
% Calculates the area and the center of every connected set of pixels
% (=cell).
statsNew = regionprops(Lnew, {'Area', 'Centroid'});
% Find objects which are not too small and not too big. These objects
% probably are cells. All others probably not.
idx = find(([statsNew.Area] > cellSizeLowerLimit)&([statsNew.Area] < cellSizeUpperLimit));
Lnew(~ismember(Lnew,idx)) = 0;

if debugging
    figure('Name','Detected Cells','NumberTitle','off');  %#ok<UNRCH>
    colormap(gray);
    imh = image(Lnew);
    set(imh, 'CDataMapping', 'scaled');
    set(gca, 'Box', 'on', 'MinorGridLineStyle', 'none', 'XTick', [], 'YTick', []);
end

%% Get positions of the cells
if isempty(idx)
    cellPositions = zeros(0, 3 + length(quantImageData));
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
if debugging
    figure('Name','Detected Cells, deleted doubles','NumberTitle','off');  %#ok<UNRCH>
    colormap(gray);
    imh = image(Lnew);
    set(imh, 'CDataMapping', 'scaled');
    set(gca, 'Box', 'on', 'MinorGridLineStyle', 'none', 'XTick', [], 'YTick', []);
end

%% relabel images in detection image, since due to deleting not all numbers
%% are set, and they don't correspond to the cell table
[Lnew] = bwlabeln(Lnew);

%% Resize detection image back to original size
if resizeScale ~= 1
    Lnew = imresize(Lnew, 1/resizeScale, 'nearest');
end
%% quantify fluorescence
quant = zeros(size(cellPositions, 1), length(quantImageData)+1);
for c=1:size(cellPositions, 1)
    quant(c, 1) = mean(imageData(Lnew(:)==c)) / double(intmax(class(imageData)));
end
for img=1:length(quantImageData)
    for c=1:size(cellPositions, 1)
        quant(c, img+1) = mean(quantImageData{img}(Lnew(:)==c)) / double(intmax(class(quantImageData{img})));
    end
end
cellPositions = [cellPositions, quant];

%% Return values
if nargout >= 1
    varargout{1} = cellPositions;
end

if nargout >= 2
    varargout{2} = uint8(Lnew);
end