classdef CellXTrackRecoverer < handle
    %UNTITLED Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
        
        initialTrackingLines
        gapFillingPairs
        segmentationConfig
        fileHandler
        nrOfCellsInFramesAfterRecovery
        
        recoveryFrameIndices
        recoveryFileSetIndices
        recoverySegmentationIndices
        
        recoveryFrameIndicesOfSourceCell
        recoveryFileSetIndicesOfSourceCell
        recoverySegmentationIndicesOfSourceCell
        
        loadedDataIndices
        trackingLinesAfterRecovery
        recoveredSeeds
        
    end
    
    methods
        
       function obj = CellXTrackRecoverer(initialTrackingLines,  gapFillingPairs, segmentationConfig, fileSet, nrOfCellsInFrames)
            obj.initialTrackingLines   = initialTrackingLines;
            obj.gapFillingPairs  = gapFillingPairs;
            obj.segmentationConfig =  segmentationConfig;
            obj.fileHandler =  fileSet;
            obj.nrOfCellsInFramesAfterRecovery = nrOfCellsInFrames;
       end
        
       function run(this)

            this.trackingLinesAfterRecovery = this.initialTrackingLines;
            % find the gap filling pairs that have frame
            % differences more than 1
            if ~isempty(this.gapFillingPairs)
               recoveryFillingPairs =  this.gapFillingPairs((this.gapFillingPairs(:,1) - this.gapFillingPairs(:,3))>1,1:4);
            else
               recoveryFillingPairs = []; 
            end
            % loop through the gap filling pairs 
            % and do the recovery 
            remainingFillingPairs = recoveryFillingPairs;
            while ~isempty(remainingFillingPairs)
                 
                % find optimal frames to load
                 [optimalFileSetsToLoad gapfillingPairLogicalIndices] = this.findOptimalFileSetsToLoad(remainingFillingPairs);
                 
                 % do the current recovery
                 this.recoverCellsInFileSets(optimalFileSetsToLoad,remainingFillingPairs(gapfillingPairLogicalIndices,:));
                 
                 % update remaining pairs 
                 remainingFillingPairs=remainingFillingPairs(~gapfillingPairLogicalIndices,:);
            end

        end
        
        function [ret1 ret2] = findOptimalFileSetsToLoad(this,cellPairs)
            
            requiredFileSetMatrix = zeros(size(cellPairs,1),this.segmentationConfig.maxGapConnectionDistance+2);
            requiredFileSetMatrixNumber = zeros(size(cellPairs,1),1);
            ret2 =  zeros(size(cellPairs,1),1);
            for nrp = 1:size(cellPairs,1)
                currentCellPairBoundFrames = cellPairs(nrp,[3,1]);
                currentCellPairFrames = currentCellPairBoundFrames(1):currentCellPairBoundFrames(2);
                requiredFileSetMatrix(nrp,1:numel(currentCellPairFrames)) =  currentCellPairFrames; 
                requiredFileSetMatrixNumber(nrp,1)= numel(currentCellPairFrames);
            end
            % start with the largest number of frames
            logind =  find(requiredFileSetMatrixNumber==max(requiredFileSetMatrixNumber) );
            ret1 =  requiredFileSetMatrix(logind(1),  requiredFileSetMatrix(logind(1),:)>0 );
            % find all lines of requiredFileSetMatrix that require the same frames 
            for nrp = 1:size(cellPairs,1)
                currentRequiredFileSetMatrix =  requiredFileSetMatrix(nrp, requiredFileSetMatrix(nrp, :)>0 );
                if all(ismember(currentRequiredFileSetMatrix,ret1))
                   ret2(nrp,1)= 1;
                end
                
            end
            ret2 = logical(ret2);
        end
        
        function recoverCellsInFileSets(this,fileSets, cellPairs)
            
            % load frame data and segmentation masks
            fileSetData = this.loadFileSetData(fileSets);
            
            % loop through cell-pairs that define recovery (endNode -> recoveredCells ->startNode )
            for ncp  =1:size( cellPairs,1)
               % take boundary cells
               currentCellEnd = cellPairs(ncp,3:4);
               endNodeData = fileSetData{this.loadedDataIndices==currentCellEnd(1)}(currentCellEnd(2));
               
               currentCellStart = cellPairs(ncp,1:2);
               startNodeData = fileSetData{this.loadedDataIndices==currentCellStart(1)}(currentCellStart(2));
              
               % add seed objects
               for nrs = 1:currentCellStart(1)- currentCellEnd(1)-1
                    
                    % do nearest neighborhood interpolation for the
                    % recovered seeds
                    xInterpLine = [0 currentCellStart(1)-currentCellEnd(1)];
                    if nrs > xInterpLine(2)/2
                       newSeed = startNodeData;
                       recoveredCellFileSetIndexOfSourceCell = currentCellStart(1);
                       recoveredCellSegmentationIndexOfSourceCell = currentCellStart(2);
                    else
                       newSeed = endNodeData;
                       recoveredCellFileSetIndexOfSourceCell = currentCellEnd(1);
                       recoveredCellSegmentationIndexOfSourceCell = currentCellEnd(2);
                    end
                    
                   % for fileSet index
                   recoveredCellFileSetIndex = nrs + currentCellEnd(1);
  
                   % for segmentation index
                   nrRecoveredCellsInFrame = numel(find(this.recoveryFileSetIndices==recoveredCellFileSetIndex));
                   recoveredCellSegmentationIndex = numel(fileSetData{this.loadedDataIndices==recoveredCellFileSetIndex})+nrRecoveredCellsInFrame+1;
                   
                   % pass new segmentation cell index to tracking lines
                   trackingLineIndx = this.initialTrackingLines(:,currentCellEnd(1))==currentCellEnd(2);
                   this.trackingLinesAfterRecovery(trackingLineIndx, recoveredCellFileSetIndex) =  recoveredCellSegmentationIndex;
                   this.nrOfCellsInFramesAfterRecovery(recoveredCellFileSetIndex) = recoveredCellSegmentationIndex;
                   
                   % add recovery frame Indices and segmentation indices 
                   this.addRecoveryFileSetIndex(recoveredCellFileSetIndex);
                   this.addRecoveryFrameIndex(recoveredCellFileSetIndex);
                   this.addRecoverySegmentationIndex(recoveredCellSegmentationIndex);
                   % add recovery frame Indices and segmentation indices of
                   % the source cell
                   this.addRecoveryFileSetIndexOfSourceCell(recoveredCellFileSetIndexOfSourceCell);
                   this.addRecoveryFrameIndexOfSourceCell(recoveredCellFileSetIndexOfSourceCell);
                   this.addRecoverySegmentationIndexOfSourceCell(recoveredCellSegmentationIndexOfSourceCell);
                   
                   % add new cell object to 'recovered seeds'
                   this.addRecoveredCell(newSeed);
               end
              
            end
   
              
        end
        
%         function ret = interpolateFluorescenceData(this,fluoFeatureList1,fluoFeatureList2,xline,x)
%             
%             ret = CellXFluoFeatures();
%             propertyNames = properties(ret);
%             % for the fluotype
%             ret.(genvarname( propertyNames{1} )) = fluoFeatureList1.fluoTypes;
%             % for the rest...
%             for k= 2:numel(propertyNames)  
%                ret.(genvarname( propertyNames{k} )) = ...
%                    interp1(xline,[fluoFeatureList1.(genvarname( propertyNames{k} )) ...
%                                   fluoFeatureList2.(genvarname( propertyNames{k} ))],x);
%             end
%             
%         end
   
        function ret1 = loadFileSetData(this,frames)
            
            ret1 =cell(1,numel(frames));
            for nrf = 1:numel(frames)
                
                d1 = load(this.fileHandler.fileSets(frames(nrf)).seedsMatFile);
                seedVarName =  fieldnames(d1);
                ret1{nrf} = d1.(genvarname(seedVarName{1}));
                
            end
            this.loadedDataIndices = frames;
        end
                
        
        function addRecoveryFileSetIndex(this,newFrameIndex)
            
            this.recoveryFileSetIndices = [this.recoveryFileSetIndices newFrameIndex];
            
        end
        
        function addRecoveryFrameIndex(this,newFrameIndex)
            
            this.recoveryFrameIndices = [this.recoveryFrameIndices...
                                             this.fileHandler.fileSets(newFrameIndex).frameIdx];
            
        end
        
        
        function addRecoverySegmentationIndex(this,newSegmentationIndex)
            
            this.recoverySegmentationIndices = [this.recoverySegmentationIndices newSegmentationIndex];
            
        end
        
        function addRecoveredCell(this,newCell)
            
            this.recoveredSeeds = [this.recoveredSeeds newCell];
        end
        
        
        function addRecoveryFileSetIndexOfSourceCell(this,newFileSetIndex)
            
            this.recoveryFileSetIndicesOfSourceCell = [this.recoveryFileSetIndicesOfSourceCell newFileSetIndex];
            
        end
        
        function addRecoveryFrameIndexOfSourceCell(this,newFileSetIndex)
            
            this.recoveryFrameIndicesOfSourceCell = [this.recoveryFrameIndicesOfSourceCell...
                                             this.fileHandler.fileSets(newFileSetIndex).frameIdx];
            
        end
        
        
        function addRecoverySegmentationIndexOfSourceCell(this,sourceSegmentationIndex)
            
            this.recoverySegmentationIndicesOfSourceCell = [this.recoverySegmentationIndicesOfSourceCell sourceSegmentationIndex];
            
        end
        
        
        function ret = getRecoveredCellsStructure(this)          
            ret.recoveredSeeds = this.recoveredSeeds;
            ret.recoveredSeedsFrameIdx = this.recoveryFrameIndices;
            ret.recoveredSeedsSegmentationIdx = this. recoverySegmentationIndices;
            ret.recoveredSeedsFrameIdxOfSourceCell = this.recoveryFrameIndicesOfSourceCell;
            ret.recoveredSeedsSegmentationIdxOfSourceCell = this.recoverySegmentationIndicesOfSourceCell;
        end
        
        
    end
    
end

