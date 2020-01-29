function [newCellTable, lostCellTable, nextCellID] = cellTracking(cellTable, lostCellTable, newCellPositions, maxDistance, nextCellID, lostCellSurvivalTime)
% Tracks the cells over several frames. The cell tables all are organized
% in the following way:
%
%       |  xpos   ypos  cellID
% --------------------------------------
% cell1 |
% cell2 |
% cell3 |
% ....
%
% Parameters:
% --------------------
% cellTable ... Table returned by the last call to this function.
% lostCellTable... Table consisting of all cells which were found
%                  previously, but not in the last (and possibly former)
%                  calls.
% newCellPositions ... positions of cells detected (normally output of
%                      CellDetection().
% maxDistance ... maximum distance a cell may travel between images to be
%                 still considered to be the same cell (in pixels).
% nextCellID ... ID the next cell which is detected for the first time
%                should get.
%
% Return values
% -------------------
% newCellTable... Table of all cells, their positions and IDs found in this
%                 run.
% lostCellTable... Updated table of the lost cells.
% nextCellID ... updated value for the next cell ID.

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
% along with the Lemming Toolbox.  If not, see
% <http://www.gnu.org/licenses/>.

% Get new position data
numNew = size(newCellPositions, 1);
newX = newCellPositions(:, 1);
newY = newCellPositions(:, 2);

% Get old position data
oldX = cellTable(:, 1);
oldY = cellTable(:, 2);
numOld = size(cellTable, 1);

% Initialize output
newCellTable = zeros(numNew, size(cellTable, 2));
nextPosInCellTable = 1;

% Calculate distances between the cells
% The rows of the table represent the new, the columns the old cells, thus
% element dist(5,2) is the distance between the new cell 5 to
% the old cell 2.
dist = (repmat(oldX', numNew, 1) - repmat(newX, 1, numOld)).^2 +...
    (repmat(oldY', numNew, 1) - repmat(newY, 1, numOld)).^2;

% Add dummy entry so that first run is successfull.
if isempty(dist)
    dist = inf;
end
%% Find matches
% Saves for every new cell if it could be associated with an old cell.
foundNew = zeros(1, numNew);
% Saves for every old cell if it could be associated with a new cell.
foundOld = zeros(1, numOld);
while true
    % Pair that has the smallest distance
    [minDist, idx] = min(dist(:));
    % Stop loop if this minimal distance is too high
    if minDist > maxDistance^2
        break;
    end
    % Get cell indices which matched
    [newMatch, oldMatch] = ind2sub(size(dist), idx);
    % Invalidate found cells in distance table so that they won't be found
    % again
    dist(newMatch, :) = inf;
    dist(:, oldMatch) = inf;
    foundNew(newMatch) = 1;
    foundOld(oldMatch) = 1;
    
    % Save new found cell in output
    newCellTable(nextPosInCellTable, :) = [newX(newMatch), newY(newMatch), cellTable(oldMatch, 3), cellTable(oldMatch, 1:2), cellTable(oldMatch, 4:end-2)];
    nextPosInCellTable = nextPosInCellTable + 1;
end

%% Look if one "newly" detected cell is just a cell which has been lost
%% previously
numLost = size(lostCellTable, 1);
newFoundIdx = find(~foundNew);
numNewFound = length(newFoundIdx);
if numLost > 0 && numNewFound > 0
    distLost = (repmat(lostCellTable(:, 1)', numNewFound, 1) - repmat(newX(newFoundIdx), 1, numLost)).^2 +...
        (repmat(lostCellTable(:, 2)', numNewFound, 1) - repmat(newY(newFoundIdx), 1, numLost)).^2;
    foundLost = zeros(1, numLost);
    while true
        % Pair that has the smallest distance
        [minDist, idx] = min(distLost(:));
        % Stop loop if this minimal distance is too high
        if minDist > maxDistance^2
            break;
        end
        % Get cell indices which matched
        [newMatchTemp, oldMatch] = ind2sub(size(distLost), idx);
        newMatch = newFoundIdx(newMatchTemp);
        newFoundIdx(newMatchTemp) = -1;
        % Invalidate found cells in distance table so that they won't be found
        % again
        distLost(newMatchTemp, :) = inf;
        distLost(:, oldMatch) = inf;
        foundNew(newMatch) = 1;
        foundLost(oldMatch) = 1;
        
        % Save new found cell in output
        newCellTable(nextPosInCellTable, :) = [newX(newMatch), newY(newMatch), lostCellTable(oldMatch, 3), lostCellTable(oldMatch, 1:2), lostCellTable(oldMatch, 4:end-3)];
        nextPosInCellTable = nextPosInCellTable + 1;
    end
    lostCellTable(find(foundLost), :) = []; %#ok<FNDSB>
    newFoundIdx(newFoundIdx<0) = [];
end

%% Add cells which where not found
for notFoundIdx = newFoundIdx
    newCellTable(nextPosInCellTable, :) = [newX(notFoundIdx), newY(notFoundIdx), nextCellID, NaN * ones(1, size(newCellTable, 2) - 3)];
    nextCellID = nextCellID + 1;
    nextPosInCellTable = nextPosInCellTable + 1;
end

%% Add cells which were lost to the lost-Table
for notFoundIdx = find(~foundOld)
    lostCellTable(end+1, :) = [oldX(notFoundIdx), oldY(notFoundIdx), cellTable(notFoundIdx, 3:end), 0]; %#ok<AGROW>
end

%% Increase "not-found time" for lost cells and delete cells which are lost
%% already too long
lostCellTable(:, end) = lostCellTable(:, end) + 1;
lostCellTable(lostCellTable(:, end) > lostCellSurvivalTime, :) = [];
