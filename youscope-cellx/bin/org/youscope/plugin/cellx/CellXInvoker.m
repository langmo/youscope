%% Check variables
if ~exist('tableDataSink', 'var') ...
        || ~exist('detectionImage', 'var') ...
        || ~exist('fluorescenceImages', 'var') ...
        || ~exist('lastResult', 'var') ...
        || ~exist('currentResult', 'var') ...
        || ~exist('scriptsFolder', 'var') ...
        || ~exist('configFileName', 'var')
    error('YouScopeCellXInterface:EmptyParameters', 'Not all parameters for the YouScope-CellX Interfacer were set.');
end

%% add paths
pathFolders = {...
	[scriptsFolder, '/mclasses'], ...
	[scriptsFolder, '/mscripts'], ...
	[scriptsFolder, '/mfunctions'], ...
	[scriptsFolder, '/mex'], ...
	[scriptsFolder, '/mex/maxflow'], ...
	[scriptsFolder, '/YouScopeInterface']};
for i=1:length(pathFolders)
	addpath(pathFolders{i}, '-begin');
end

%% deactivate serialization warnings
oldWarnState = warning('off', 'MATLAB:structOnObject');


%% Get last state (needed for incremental tracking
if ~isempty(lastResult)
	if trackCells
    	previousSegmentedCells = lastResult.getData('segmentedCells');
    	previousSegmentationMask = lastResult.getData('segmentationMask');
    	previousCellTable = lastResult.getData('cellTable');
    	if ~isempty(previousSegmentedCells)
	        previousSegmentedCells = hlp_deserialize(typecast(previousSegmentedCells, 'uint8'));
	    else
	    	previousSegmentedCells = [];    
	    end
	    if ~isempty(previousSegmentationMask)
	        previousSegmentationMask = hlp_deserialize(typecast(previousSegmentationMask, 'uint8'));
	    else
	    	previousSegmentationMask = [];
	    end
	    if ~isempty(previousCellTable)
	        previousCellTable = hlp_deserialize(typecast(previousCellTable, 'uint8'));
	    else
	    	previousCellTable = [];
	    end
   	else
   		previousSegmentedCells = [];
    	previousSegmentationMask = [];
    	previousCellTable = [];
   	end
    invocationNo = lastResult.getData('invocation');

    
     if ~isempty(invocationNo)
     	invocationNo = invocationNo + 1;
     else
        invocationNo = 1;
    end
else
    previousSegmentedCells = [];
    previousSegmentationMask = [];
    previousCellTable = [];
    invocationNo = 1;
end

%% Convert images, pretend we have 16bit to be on the save side...
segmImage = toMatlabImage(detectionImage) * double(intmax('uint16'));
fluoInitialImages = cell(1, length(fluorescenceImages));
fluoTags = cell(1, length(fluorescenceImages));
flatFieldFileNames = cell(1, length(fluorescenceImages));
for i = 1:length(fluorescenceImages)
    fluoInitialImages{i} = toMatlabImage(fluorescenceImages(i)) * double(intmax('uint16'));
    fluoTags{i} = sprintf('fluo%g', i);
    % TODO: Should we also use a flat field image?
    flatFieldFileNames{i} = [];
end

%% Read in Configuration
config = CellXConfiguration.readXML(configFileName);
config.check();
    
%% Run Cell Detection & Tracking
cellXYouScopeInterfacer = CellXYouScopeInterface(invocationNo, configFileName,segmImage,...
    fluoTags,flatFieldFileNames,fluoInitialImages,...
    previousSegmentedCells,previousSegmentationMask,...
    previousCellTable);
cellXYouScopeInterfacer.run;

%% Save tracking data for next call
currentResult.setData('segmentedCells', hlp_serialize(cellXYouScopeInterfacer.currentSegmentedCells));
currentResult.setData('segmentationMask', hlp_serialize(cellXYouScopeInterfacer.currentSegmentationMask));
currentResult.setData('cellTable', hlp_serialize(cellXYouScopeInterfacer.currentResult));    
currentResult.setData('invocation', invocationNo);

%% Send data to YouScope
if numel(cellXYouScopeInterfacer.currentResult.data) > 0
	% Convert to string table
	cellPositionsStr = reshape(cellstr(num2str(cellXYouScopeInterfacer.currentResult.data(:))), size(cellXYouScopeInterfacer.currentResult.data, 1), size(cellXYouScopeInterfacer.currentResult.data, 2));
	% Convert to array of row objects
	cellTableRows = javaArray('org.youscope.common.TableDataRow', size(cellPositionsStr, 1));
	for i=1:size(cellPositionsStr, 1)
		cellTableRows(i) = org.youscope.common.TableDataRow(cellPositionsStr(i, :), [], [], java.util.Date());
	end
	% Submit to YouScope
	tableDataSink.newRows([], cellTableRows);
end
if exist('imageSink', 'var')
    imageSink.imageMade(toYouScopeImage(uint8(cellXYouScopeInterfacer.currentSegmentationMask)));  
end
   
%% Display headers for debugging
if exist('debug_mode', 'var')
	fprintf('\nHeaders: ');   
	for i=1:length(cellXYouScopeInterfacer.currentResult.headers)
		if i>1
			fprintf(', ');
		end
		fprintf('%s', cellXYouScopeInterfacer.currentResult.headers{i});
	end    
	fprintf('\n');
end

%% Reset warning state
warning(oldWarnState);

%% Remove added paths
for i=1:length(pathFolders)
	rmpath(pathFolders{i});
end 