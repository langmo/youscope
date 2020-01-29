classdef CellXYouScopeInterface < handle
    %UNTITLED Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
        
       fileSetNum 
       configFileName
       config
       fSet
       
       segmImage
       currentSegmentedCells
       previousSegmentedCells
       currentSegmentationMask
       previousSegmentationMask
       
       fluoTags
       flatFieldFileNames
       fluoInitialImages

       currentAssignment
       trackingIndices
       currentResult
       previousResult
       
       
    end
    
    methods
       
       % constructor 
       function obj = CellXYouScopeInterface(fileSetNum, configFileName,segmImage,...
                                              fluoTags,flatFieldFileNames,fluoInitialImages,...
                                              previousSegmentedCells,previousSegmentationMask,...
                                              previousResult)
            obj.fileSetNum = fileSetNum;                              
            obj.configFileName  = configFileName;
            obj.segmImage = segmImage;
            obj.fluoTags = fluoTags;
            obj.flatFieldFileNames =flatFieldFileNames;
            obj.fluoInitialImages=fluoInitialImages;
            obj.previousSegmentedCells =  previousSegmentedCells;
            obj.previousSegmentationMask = previousSegmentationMask;
            obj.previousResult = previousResult;
            
        end
        
        
        function run(this)
            %---- read the configuration file of the current experiment
            this.config = CellXConfiguration.readXML(this.configFileName);
            this.config.check();
            
            % construct a fileSet
            this.fSet = CellXFileSet(this.fileSetNum,'');
            
            % check existense of fluoFiles
            if ~isempty(this.fluoTags)
                nrFluoTags = numel(this.fluoTags);
                for nrft = 1:nrFluoTags
                    % pass the parameters of the fluo-images
                    if ~isempty(this.flatFieldFileNames)
                        % take  the flatfield files paths
                        this.fSet.addFluoImageTag('', this.fluoTags{nrft}, this.flatFieldFileNames{nrft} );
                    else
                        this.fSet.addFluoImageTag('', this.fluoTags{nrft});
                    end 
                end
            end
            
            % run the segmentation
            cellXSegmenter = CellXSegmenterYouScope(this.config, this.segmImage );
            cellXSegmenter.run();
            this.currentSegmentedCells = cellXSegmenter.getDetectedCells();
            fprintf('Detected %d cell(s) on current frame \n', numel(this.currentSegmentedCells));
            
            % run the intensity extractor
            cellXIntensityExtractor = CellXIntensityExtractorYouScope(this.config, this.fSet, this.currentSegmentedCells, this.fluoInitialImages);
            cellXIntensityExtractor.run();
            
            % take the current segmentation mask
            dim = size(this.segmImage);
            this.currentSegmentationMask = CellXResultExtractorYouScope.takeSegmentationMask(this.currentSegmentedCells, dim);
            
            %  if tracking is to be done
            if ~isempty(this.previousSegmentationMask)
                
                trackerYouScope = CellXTrackerYouScope(this.config,...
                                             this.previousSegmentedCells,this.previousSegmentationMask,...
                                             this.currentSegmentedCells,this.currentSegmentationMask);
                trackerYouScope.run();
                this.currentAssignment = trackerYouScope.currentAssignment;               
                
                % find the new trackking Indices                
                trackingIndicesPrevious = this.previousResult.data(:,end);
                maxTrackingIndex = max(trackingIndicesPrevious);
                % for this frame
                nrCellsCurrentFrame = numel(this.currentSegmentedCells);
                this.trackingIndices =zeros(nrCellsCurrentFrame,1);
                % take the nonzero assignments
                nonZeroAssignments =  this.currentAssignment>0;
                % pass the tracking indices
                this.trackingIndices(this.currentAssignment(nonZeroAssignments)) = ...
                     trackingIndicesPrevious(nonZeroAssignments);
                % give the remaining zero values a new tracking index 
                zeroAssignments =  find(this.trackingIndices==0);
                if ~isempty(zeroAssignments)
                    newTrackingIndices = maxTrackingIndex+1:maxTrackingIndex+numel(zeroAssignments);
                    this.trackingIndices(zeroAssignments) = newTrackingIndices';
                end
                
            else
                this.trackingIndices = (1:numel(this.currentSegmentedCells))';
            end
            
            % store current result
             this.currentResult = CellXResultExtractorYouScope.extractSegmentationResults(...
                  this.fSet, this.currentSegmentedCells, this.config, this.trackingIndices);
        end
        
        
        
        
    end
    
end


