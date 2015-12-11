%% Add Matlab tools path
addpath(scriptsFolder, '-begin');

%% Process image if available
if exist('imageEvent', 'var') && exist('detectionResult', 'var') && ~isempty(imageEvent) && ~isempty(detectionResult)
	% Create matrix, with first element being the x, second the y-position, and third the area.
	dataTable = detectionResult.getDetectionTable();
	dataHeader = detectionResult.getDetectionHeaders();
	
	cellPositions = zeros(size(dataTable, 1), 3);
	
	% Find x-position column
	foundColumn = false;
	for i=1:length(dataHeader)
		if strcmp(char(dataHeader(i)), 'cell.center.x');
			foundColumn = true;
			cellPositions(:, 1) = str2double(cell(dataTable(:, i))) + 1;
			break;
		end
	end
	if ~foundColumn
		error('GlowVisualizer:InvalidCellDetectionTable', 'Table column "cell.center.x" does not exist in cell detection table.');
	end
	
	foundColumn = false;
	for i=1:length(dataHeader)
		if strcmp(char(dataHeader(i)), 'cell.center.y');
			foundColumn = true;
			cellPositions(:, 2) = str2double(cell(dataTable(:, i))) + 1;
			break;
		end
	end
	if ~foundColumn
		error('GlowVisualizer:InvalidCellDetectionTable', 'Table column "cell.center.y" does not exist in cell detection table.');
	end
	
	foundColumn = false;
	for i=1:length(dataHeader)
		if strcmp(char(dataHeader(i)), 'cell.area');
			foundColumn = true;
			cellPositions(:, 3) = str2double(cell(dataTable(:, i)));
			break;
		end
	end
	if ~foundColumn
		error('GlowVisualizer:InvalidCellDetectionTable', 'Table column "cell.area" does not exist in cell detection table.');
	end


    %% Generate detection image
    colorImage = cellDetectionToImage(imageEvent, cellPositions, sqrt(mean(cellPositions(:, 3))) * 3, glowStrength, false);

    %% Pass generated image to YouScope
    imageSink.imageMade(toYouScopeImage(colorImage));
end
