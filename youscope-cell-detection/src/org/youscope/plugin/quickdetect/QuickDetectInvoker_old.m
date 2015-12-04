%% Get image
cd(scriptsFolder);

%% Process image if available
if ~isempty(imageEvent)
	if ~exist('quantImages', 'var')
		quantImages = cell(1, 0);
	end
    %% Detect cells
    [cellPositions, detectionImage] = cellDetection(imageEvent, minCellDiameter^2, maxCellDiameter^2, detectionThreshold, 1/internalBinning, quantImages);
    
    %% Pass generated detection image to YouScope
    if exist('imageSink', 'var')
    	imageSink.imageMade(toYouScopeImage(detectionImage));
    end
    
    % Correct indexing of cell positions:
	% Matlab counts starting at 1, Java starting at 0
	cellPositions(:, 1:2) = cellPositions(:, 1:2) - 1;
    
    %% Send found cell data to YouScope
    if ~isempty(cellPositions)
    	% Convert to string table
    	cellPositionsStr = reshape(cellstr(num2str(cellPositions(:))), size(cellPositions, 1), size(cellPositions,2));
    	% Convert to array of row objects
    	cellTableRows = javaArray('org.youscope.common.TableDataRow', size(cellPositionsStr, 1));
    	for i=1:size(cellPositionsStr, 1)
    		cellTableRows(i) = org.youscope.common.TableDataRow(cellPositionsStr(i, :), [], [], java.util.Date());
    	end
    	% Submit to YouScope
    	tableDataSink.newRows([], cellTableRows);
    end
    
end
