function colorImage = cellDetectionToImage(imageData, cellPositions, glowWidth, colorStrength, increaseContrast)
% This function visualizes the detected cells in an image

% Copyright 2010 Moritz Lang
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
% along with this file.  If not, see
% <http://www.gnu.org/licenses/>.

if ~exist('increaseContrast', 'var')
    increaseContrast = false;
end

%% Get Matlab image matrix if youscope one
if strcmp(class(imageData), 'ch.ethz.csb.youscope.shared.ImageEvent')
    imageData = toMatlabImage(imageData);
end
if increaseContrast
    imageData = uint8(double(imageData) * (double(intmax('uint8')) / double(max(imageData(:)))) / (1+colorStrength));
else
    imageData = uint8(double(imageData) / (1+colorStrength));
end
%% We can not draw pixels at noninteger positions
cellPositions = round(cellPositions);

%% Set basic output image colors to gray
imageRed = imageData;
imageGreen = imageData;
imageBlue = imageData;
[imageHeight, imageWidth] = size(imageRed);

%% Create glow image
if isnan(glowWidth)
    glowWidth = 11;
end
if mod(glowWidth, 2) == 0
    glowWidth = glowWidth + 1;
end
half = (glowWidth-1)/2;
glowImage = colorStrength * (1 - (sqrt(repmat((-half:half).^2, glowWidth, 1) + repmat((-half:half)'.^2, 1, glowWidth))) / half);
glowImage(glowImage<0) = 0;

%% Highlight all cells
for i = find(~isnan(cellPositions(:, 1)') & ~isnan(cellPositions(:, 1)'))
    if cellPositions(i, 1) - half < 1 || cellPositions(i, 1) + half > imageWidth...
            || cellPositions(i, 2) - half < 1 || cellPositions(i, 2) + half > imageHeight
        continue;
    end
    imageBlue(cellPositions(i, 2) - half : cellPositions(i, 2) + half, cellPositions(i, 1) - half : cellPositions(i, 1) + half) = uint8(double(imageBlue(cellPositions(i, 2) - half : cellPositions(i, 2) + half, cellPositions(i, 1) - half : cellPositions(i, 1) + half)) .* (1 + glowImage));
end

colorImage = cat(3, imageRed, imageGreen, imageBlue);
%colorImage = cat(size(imageRed, 1), size(imageRed, 2), 3);
%colorImage(:, :, 1) = imageRed;
%colorImage(:, :, 2) = imageGreen;
%colorImage(:, :, 3) = imageBlue;
end
