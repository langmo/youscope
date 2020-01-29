classdef CellXTracker < handle
    %UNTITLED3 Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
        
        % general
        segmentationConfig;
        fileHandler;
        nrOfFrames;
        
        % for consecutive weighted graph matching
        currentFileSetNumber;
        nextFileSetNumber;
        currentFileSetData;
        nextFileSetData;
        currentFileSetSegmentationMask;
        nextFileSetSegmentationMask;
        currentOverlapMatrices;
        currentDistanceMatrices;
        currentShapeMatrices;
        nrOfCellsInFrames;
        terminalCost;
        
        % for cost and assignment storage
        currentCostMatrix;
        currentAssignment;
        currentAssignmentCosts
        concecutiveCostJointMatrix
        concecutiveAssignmentJointMatrix
        
        % for cell neighborhood storage
        currentNeighborhoodMatrix
        nextNeighborhoodMatrix
        neighborhoodMatrices
        
        % for tracking line manipulation
        trackingLinesInitial;
        trackingLinesInitialCosts;
        trackingLinesInitialThreshold;
        trackingLines;
        trackingLinesCosts;
        
        % start-end connector
        startEndGapConnector 
        startEndSplitMergeConnector
        startEndConnectingNodes
        connectedTrackingLines
        
        % recovery
        trackRecoverer
        
        % final tracking information
        trackingInfoMatrix
        trackingInfoNames
    end
    
    methods
        
        function obj = CellXTracker(config, fileHandler)
            obj.segmentationConfig   = config;
            obj.fileHandler  =fileHandler;
            obj.nrOfFrames = size(fileHandler.fileSets,2);
        end
        
        function run(this)
            
            % 1. run consecutive frame Weighted Graph Matching
            fprintf(' Tracking cells ...\n');
            fprintf('   Weighted Graph Matching for cells on consecutive frames ... \n');
            tic
            fprintf('   Weighted Graph Matching progress: %d',0);n=1;

            for nrf = 1:this.nrOfFrames
                if nrf<this.nrOfFrames
                    % take frame numbers
                    this.currentFileSetNumber = nrf;
                    this.nextFileSetNumber = nrf+1;
                    
                    % 1a  load the concecutive frame data and segmentation masks
                    this.loadConsecutiveFileSetData()
                    
                    % 1b store neighborhood matrix for current frame % next
                    % frame
                    if nrf==1
                        this.currentNeighborhoodMatrix = ...
                            this.computeNeighborhoodInMask(this.currentFileSetSegmentationMask,this.currentFileSetData);
                    else
                        this.currentNeighborhoodMatrix = this.nextNeighborhoodMatrix;
                    end
                    this.nextNeighborhoodMatrix= ...
                        this.computeNeighborhoodInMask(this.nextFileSetSegmentationMask,this.nextFileSetData);
                    
                    % 1c compute the  cost matrix for consecutive assignments
                    this.currentCostMatrix = this.computeCostMatrixForConsecutiveFileSets();
                    % append terminal costs
                    this.currentCostMatrix = this.appendTerminalCostsToCostMatrix(this.terminalCost);
                    
                    % 1d do weighted graph matching
                    this.currentAssignment = this.doWeightedGraphMatching();
                    % make terminating assignments=0
                    this.currentAssignment(this.currentAssignment>numel(this.nextFileSetData)) = 0;
                    
                    % 1e. take resulting assignement costs
                    this.currentAssignmentCosts = this.getAssignmentCosts();
                    
                    % store info on the number of cells in frame
                    this.nrOfCellsInFrames(nrf) = numel(this.currentFileSetData);
                    
                else
                    
                    this.currentNeighborhoodMatrix = this.nextNeighborhoodMatrix;
                    %dummy assignment of last frame
                    this.currentAssignment = zeros(numel(this.nextFileSetData),1);
                    this.currentAssignmentCosts = Inf*ones(numel(this.nextFileSetData),1);
                    
                    % store info on the number of cells in frame
                    this.nrOfCellsInFrames(nrf) = numel(this.nextFileSetData);
                end
                
                
                % 1f. produce concecutiveAssignmentJointMatrix and
                % corresponding cost matrix
                this.concecutiveAssignmentJointMatrix{nrf} = this.currentAssignment;
                this.concecutiveCostJointMatrix{nrf} = this.currentAssignmentCosts;
                
                % 1g. store neighborhood matrix for current frame
                this.neighborhoodMatrices{nrf} = this.currentNeighborhoodMatrix;
                progperc =  num2str(round(100*nrf/this.nrOfFrames));
                fprintf(repmat('\b',1,n));
                fprintf(progperc);
                n=numel(progperc);
            end
            %toc
            fprintf('\n');
            
            % 2. compute the initial tracking lines
            [this.trackingLinesInitial, this.trackingLinesInitialCosts] =...
                this.computeTrackingLines();
            
            
            % 3. threshold the initial tracking lines based on the cost
            % districution and update them
            this.trackingLinesInitialThreshold = this.computeTrackingLinesInitialThreshold();
            [this.trackingLines, this.trackingLinesCosts] = this.updateTrackingLines();
            
            % 4. run  Weighted Graph Matching on the start-end Nodes
            fprintf('   Weighted Graph Matching for start/end nodes ... \n');
            %tic
            this.startEndGapConnector  = CellXTrackerStartEndGapConnector(this.segmentationConfig,...
                                                                    this.fileHandler,...
                                                                    this.trackingLines,...
                                                                    this.neighborhoodMatrices,...
                                                                    this.terminalCost);
            this.startEndGapConnector.run();
            %toc
            %fprintf('finish start-end nodes connection \n ');
            
            
            % 5. recover cells in gap filling sequences
            fprintf('   Recovering cells ... \n');
            %tic
            if ~isempty(this.startEndGapConnector.startEndConnectingNodes)
               gapFillingPairs = this.startEndGapConnector.startEndConnectingNodes(...
                               this.startEndGapConnector.startEndConnectingNodes(:,5)==0 ,:);
            else
                gapFillingPairs =[];
            end
            this.trackRecoverer = CellXTrackRecoverer(this.startEndGapConnector.updatedTrackingLines,...
                                                     gapFillingPairs,this.segmentationConfig,...
                                                     this.fileHandler,this.nrOfCellsInFrames);
            this.trackRecoverer.run();
            %toc
            %fprintf('finish cell recovery \n ');
            
            
            % 6. update the neighborhood matrices for the frames that cells
            % are recovered
            uniqueRecoveryFileSets = unique(this.trackRecoverer.recoveryFileSetIndices);
            for nrrf = 1:numel(uniqueRecoveryFileSets)
                
                fileSetIndx =  uniqueRecoveryFileSets(nrrf);
               
                [updFileSetData , updSegmentationMask] = this.updateDataAndSegmentationMask(fileSetIndx);
     
               newNeighborhoodMatrix = ...
                           this.computeNeighborhoodInMask(updSegmentationMask,updFileSetData );
                        
               %  store neighborhood matrix for current file index
               this.neighborhoodMatrices{fileSetIndx } =  newNeighborhoodMatrix;         
                
            end
            
            
            % 7. connect split-merging pairs
            fprintf('   Weighted Graph Matching for start/end nodes ... \n');
            %fprintf('connecting start-end nodes for merge-split events... \n ');
            %tic
            this.startEndSplitMergeConnector  = CellXTrackerStartEndSplitMergeConnector(this.segmentationConfig,...
                                                                    this.fileHandler,...
                                                                    this.trackRecoverer.trackingLinesAfterRecovery,...
                                                                    this.neighborhoodMatrices,...
                                                                    this.terminalCost,...
                                                                    this.trackRecoverer);
            this.startEndSplitMergeConnector.run();
            %toc
            %fprintf('finish start-end nodes connection \n ');
            
            
            % 7. produce final tracking result: store tracking Information to matrix           
            %    and produce lineage plot
            this.startEndConnectingNodes = [this.startEndGapConnector.startEndConnectingNodes ;...
                                            this.startEndSplitMergeConnector.startEndConnectingNodes];       
            this.constructTrackingInfoMatrix()
            if this.segmentationConfig.debugLevel>0
               CellXImgGen.generateLineageTree(this.trackingInfoMatrix); 
            end
            
            fprintf(' Finished cell tracking\n');
            t=toc;
            fprintf(' Elapsed time: %4.2fs\n', t);
        end
        
        
        function [ret1 , ret2] = updateDataAndSegmentationMask(this,fileSetIndx)
               
                %load initial data
                d1 = load(this.fileHandler.fileSets(fileSetIndx).seedsMatFile);
                seedVarName =  fieldnames(d1);
                initFileSetData = d1.(genvarname(seedVarName{1}));
                
                %load initial segmentation mask
                m1 = load(this.fileHandler.fileSets(fileSetIndx).maskMatFile);
                maskVarName = fieldnames(m1);
                initSegmentationMask = m1.(genvarname(maskVarName{1}));
                
         
                % append seeds if there are recovered
                % cells in the frame
                recIndx = ismember(this.trackRecoverer.recoveryFileSetIndices,...
                    fileSetIndx);
                
                fileSetRecoveredSeeds = this.trackRecoverer.recoveredSeeds(recIndx);
                fileSetRecoveredSeedsSegIdxs = ...
                   this.trackRecoverer.recoverySegmentationIndices(recIndx);
                [~, ix]  = sort(fileSetRecoveredSeedsSegIdxs);
                fileSetRecoveredSeeds = fileSetRecoveredSeeds(ix);
                fileSetRecoveredSeedsSegIdxs = fileSetRecoveredSeedsSegIdxs(ix);
                ret1 =[initFileSetData fileSetRecoveredSeeds];

                % update the mask 
                 updSegmentationMask = initSegmentationMask;
                for nrs = 1:numel(fileSetRecoveredSeeds)
                    curSegIndx = fileSetRecoveredSeedsSegIdxs(nrs);
                    curPixelListLind = fileSetRecoveredSeeds(nrs).cellPixelListLindx;
                    updSegmentationMask(curPixelListLind) = curSegIndx;
                end
              
                ret2 = updSegmentationMask; 
        
        end
        
        function loadConsecutiveFileSetData(this)
            
            if this.currentFileSetNumber==1
                
                d1 = load(this.fileHandler.fileSets(this.currentFileSetNumber).seedsMatFile);
                seedVarName =  fieldnames(d1);
                this.currentFileSetData = d1.(genvarname(seedVarName{1}));
                
                m1 = load(this.fileHandler.fileSets(this.currentFileSetNumber).maskMatFile);
                maskVarName = fieldnames(m1);
                this.currentFileSetSegmentationMask = m1.(genvarname(maskVarName{1}));
                
            else
                
                this.currentFileSetData = this.nextFileSetData;
                
                this.currentFileSetSegmentationMask =  this.nextFileSetSegmentationMask;
                
            end
            
            d2 = load(this.fileHandler.fileSets(this.nextFileSetNumber).seedsMatFile);
            seedVarName =  fieldnames(d2);
            this.nextFileSetData = d2.(genvarname(seedVarName{1}));
            
            m2 = load(this.fileHandler.fileSets(this.nextFileSetNumber).maskMatFile);
            maskVarName = fieldnames(m2);
            this.nextFileSetSegmentationMask = m2.(genvarname(maskVarName{1}));
        end
        
        
        function ret1 = computeNeighborhoodInMask(this, currentSegmentationMask,currentFileSetData)
            
            NeighborMat = zeros(numel(currentFileSetData),1);
            
            % for current frame
            currentFrameCellCenters = vertcat(currentFileSetData(:).centroid);
            % for  each cell
            for k=1:size(currentFrameCellCenters,1)
                
                CurCellCentroid =  currentFrameCellCenters(k,:);
                
                
                CentroidDistances =  sqrt(( currentFrameCellCenters(:,1)-CurCellCentroid(1)).^2 + ...
                    (currentFrameCellCenters(:,2)-CurCellCentroid(2)).^2);
                
                PotentialNeighbInd = find(CentroidDistances<2*this.segmentationConfig.maximumCellLength & CentroidDistances>0);
                
                % for all potential touching neighbors connect the centers and check
                % if there is a cell in between
                
                if ~isempty(PotentialNeighbInd)
                    cnt=0;
                    for nrn = 1:length(PotentialNeighbInd)
                        curNeighCenter= currentFrameCellCenters( PotentialNeighbInd(nrn),:);
                        % draw the connecting line
                        [~,~,maskprofile] = improfile(currentSegmentationMask,...
                            [CurCellCentroid(1)   curNeighCenter(1)],[CurCellCentroid(2)    curNeighCenter(2)]);
                        % if too many  zeros exist then do not accept
                        NrBackPixels =  length(find(maskprofile==0));
                        if  NrBackPixels <  this.segmentationConfig.maximumCellLength
                            % check if there is any other number, apart from current cell's, neighbor's and zero
                            Cell_In_Between_Indices = find(maskprofile~=k & maskprofile~=0 & maskprofile~=PotentialNeighbInd(nrn));
                            if isempty(Cell_In_Between_Indices) % make it a neighbor
                                cnt=cnt+1;
                                NeighborMat(k,cnt) = PotentialNeighbInd(nrn);
                            end
                        end
                    end
                    
                end
                
            end
            
            ret1 = NeighborMat;
            
        end
        
        function ret =  computeCostMatrixForConsecutiveFileSets(this)
            
            % find the overlap matrices
            [overlapCostMatrix12 overlapCostMatrix21] = this.computeOverlapsBetweenFileSets();
            this.currentOverlapMatrices{1} = overlapCostMatrix12 ;
            this.currentOverlapMatrices{2} = overlapCostMatrix21 ;
            
            % compute  distance costs
            [distanceCostMatrix12  distanceCostMatrix21] = this.computeDistanceCostsBetweenFileSets();
            this.currentDistanceMatrices{1} = distanceCostMatrix12;
            this.currentDistanceMatrices{2} = distanceCostMatrix21;
            
            % compute shape Cost
            [shapeCostMatrix12  shapeCostMatrix21] = this.computeShapeCostsBetweenFileSets();
            this.currentShapeMatrices{1} = shapeCostMatrix12;
            this.currentShapeMatrices{2} = shapeCostMatrix21;
            
            % construct the joint cost Matrix
            ret = this.computeJointCostMatrix();
            
        end
        
        function [ret12 ret21]  = computeShapeCostsBetweenFileSets(this)
            
            nrSeedsCurrentFrame = numel(this.currentFileSetData);
            nrSeedsNextFrame = numel(this.nextFileSetData);
            
            ret12 = zeros(nrSeedsCurrentFrame, nrSeedsNextFrame);
            ret21 = zeros(nrSeedsNextFrame,nrSeedsCurrentFrame);
            
                      
            for k =  1:nrSeedsCurrentFrame
                for l =1:nrSeedsNextFrame
                    
                    % take the means/stds of the defined distance
                    % distributions
                    ck =[this.currentFileSetData(k).majorAxisLength,...
                         this.currentFileSetData(k).majorAxisLength,...
                         round(this.currentFileSetData(k).orientation)];
                     
                    cl =[this.nextFileSetData(l).majorAxisLength,...
                         this.nextFileSetData(l).majorAxisLength,...
                          round(this.nextFileSetData(l).orientation)];
                             
                    refck = [ck(1:2) 180];  
                    refcl = [cl(1:2) 180]; 
                    
                    maxAlowedCost = sqrt(sum([0.25 0.25 0.5].^2));
                     
                    % compute euklidean distance
                    currentCellDistance_ck = sqrt(sum(((ck-cl)./refck).^2 ))/maxAlowedCost;
                    currentCellDistance_cl = sqrt(sum(((ck-cl)./refcl).^2 ))/maxAlowedCost;
                   
                    
                    % assign the costs
                    ret12(k,l) =  currentCellDistance_ck;
                    ret21(l,k) =  currentCellDistance_cl;
                    
                end
            end
                  
            
        end
        
        
        
        
        function [ret12 ret21]  = computeOverlapsBetweenFileSets(this)
            
            M1 = this.currentFileSetSegmentationMask;
            M2 = this.nextFileSetSegmentationMask;
            % take max of matrices
            max1 = max(M1(:));
            max2 = max(M2(:));
            % take the  maximum of the 2
            MaxOffs = max(max1,max2);
            
            % add offset to non-zero pixels
            M1a = zeros(size(M1));
            NonZero1 = find(M1>0); M1a(NonZero1) = M1(NonZero1)+ MaxOffs +1;
            
            M2a = zeros(size(M2));
            NonZero2 = find(M2>0); M2a(NonZero2) = M2(NonZero2)+ MaxOffs +1;
            
            % sum the offset-images
            MoffsetSum = M1a+M2a;
            OverlapIndices = find(MoffsetSum>2*(MaxOffs+1)+1);
            OverlapNums = MoffsetSum(OverlapIndices);
            
            % substract the initial images
            Msubstract = M1-M2;
            SubstractNums = Msubstract(OverlapIndices);
            
            % find the unique systems
            ab = [OverlapNums-2*(MaxOffs+1) SubstractNums];
            abuniq = unique(ab,'rows');
            
            % solve the 2x2 system (the result are the Overlap Matrix Indices)
            CellFr2 = (abuniq(:,1)-abuniq(:,2))/2;
            CellFr1 = abuniq(:,1) - CellFr2;
            OverlapMatInd = [CellFr1 CellFr2];
            % loop through the entries of the Overlap Matrx and store the result
            OverlapMat12 = zeros(max1,max2);
            OverlapMat21 = zeros(max2,max1);
            
            for nrr =1: size(OverlapMatInd,1)
                % we have to count how many times
                % the abuniq row was observed in the
                % ab matrix
                currow = abuniq(nrr,:);
                CurrowCounts = length(find((ab(:,1)==currow(1) &  ab(:,2)==currow(2))));
                % find initial area of each cell
                
                CellArea1 = numel(this.currentFileSetData(OverlapMatInd(nrr,1)).cellPixelListLindx);
                %CellArea1 = length(find(M1(:)==OverlapMatInd(nrr,1)));
                OverlapPerc1 =  CurrowCounts/CellArea1;
                %logicalM2 = (M2(:)==OverlapMatInd(nrr,1));
                CellArea2 = numel(this.nextFileSetData(OverlapMatInd(nrr,2)).cellPixelListLindx);
                %CellArea2 = length(find(M2(:)==OverlapMatInd(nrr,2)));
                OverlapPerc2 =  CurrowCounts/CellArea2;
                % store the overlap percentage
                OverlapMat12(OverlapMatInd(nrr,1),OverlapMatInd(nrr,2)) = OverlapPerc1;
                OverlapMat21(OverlapMatInd(nrr,2),OverlapMatInd(nrr,1)) = OverlapPerc2;
            end
            % store the result
            ret12  = (1 - OverlapMat12)./ones(size(OverlapMat12));
            ret21  = (1 - OverlapMat21)./ones(size(OverlapMat21));
                        
        end
        
        
        function [ret12 ret21]  = computeDistanceCostsBetweenFileSets(this)
            
            
            nrSeedsCurrentFrame = numel(this.currentFileSetData);
            nrSeedsNextFrame = numel(this.nextFileSetData);
            
            ret12 = zeros(nrSeedsCurrentFrame, nrSeedsNextFrame);
            ret21 = zeros(nrSeedsNextFrame,nrSeedsCurrentFrame);           
            
            % define maximum allowed distance
            maxAllowedDistance =this.segmentationConfig.spatialMotionFactor;
            
            
            for k =  1:nrSeedsCurrentFrame
                for l =1:nrSeedsNextFrame
                    
                    % take the means/stds of the defined distance
                    % distributions
                    ck =this.currentFileSetData(k).centroid;
                    cl =this.nextFileSetData(l).centroid;
                    % compute euklidean distance
                    currentCellDistance = sqrt(sum( (ck-cl).^2 ));
                    currentCellCost = currentCellDistance/maxAllowedDistance;
                    
                    % assign the costs
                    ret12(k,l) =  currentCellCost;
                    ret21(l,k) =  ret12(k,l);
                    
                end
            end
            
            
        end
        
        
        function ret = computeJointCostMatrix(this)
            
            
            ret = (1/1)*(this.currentOverlapMatrices{1} + this.currentOverlapMatrices{2}') +...
                  (1/2)*( this.currentDistanceMatrices{1} + this.currentDistanceMatrices{2}') +...
                  (1/2)*( this.currentShapeMatrices{1} + this.currentShapeMatrices{2}');
              
           this.terminalCost = 2*(1+(1/2)+(1/2));   
            
        end
        
        
        function ret = appendTerminalCostsToCostMatrix(this,terminalCost)
            
            terminalCostMatrix = terminalCost*eye( size(this.currentCostMatrix,1) );
            terminalCostMatrix(terminalCostMatrix==0) = Inf;
            
            ret = [this.currentCostMatrix terminalCostMatrix];
            
            
        end
        
        
        function ret = doWeightedGraphMatching(this)
            
            G = this.currentCostMatrix;
            [assignment, ~ , ~] = munkres(G);
            ret = assignment;
            
        end
        
        function ret = getAssignmentCosts(this)
            
            nrOfAssignments = numel(this.currentAssignment);
            ret = zeros(nrOfAssignments,1);
            
            for k = 1:nrOfAssignments
                
                if  this.currentAssignment(k)>0
                    ret(k) = this.currentCostMatrix(k,this.currentAssignment(k));
                else
                    ret(k) = Inf;
                end
            end
            
        end
        
        function ret = computeConcecutiveAssignmentJointMatrix(this)
            
            nrSeedsCurrentFrame = numel(this.currentFileSetData);
            
            ret = [repmat(this.currentFileSetNumber,nrSeedsCurrentFrame ,1), ...
                (1:nrSeedsCurrentFrame)', ...
                repmat(this.currentFileSetNumber+1,nrSeedsCurrentFrame ,1), ...
                this.currentAssignment, ...
                this.currentAssignmentCosts];
            
        end
        
        
        function [ret1 ret2]= computeTrackingLines(this)
            
            TraceMat = [];CostMat=[];
            NrFrames = this.nrOfFrames;
            for nrf = 1:NrFrames
                if nrf==1 % first frame has the initial row indices
                    curmatch = this.concecutiveAssignmentJointMatrix{nrf};
                    TraceMat(:,nrf) = (1:numel(curmatch))';
                    % store the result to a temporary column
                    tracemat_previous = TraceMat(:,nrf);
                else % for the rest of the frames
                    curmatch = this.concecutiveAssignmentJointMatrix{nrf-1};
                    curcost = this.concecutiveCostJointMatrix{nrf-1};
                    %---- update the existing tracked cells
                    % first find the nonzero indices of the previous frame
                    NonZeroRowPositionsOfPreviousFrame = find(tracemat_previous);
                    NonZeroIndicesOfPreviousFrame = tracemat_previous(NonZeroRowPositionsOfPreviousFrame);
                    % update them
                    TraceMat(NonZeroRowPositionsOfPreviousFrame,nrf)=curmatch(NonZeroIndicesOfPreviousFrame);
                    CostMat(NonZeroRowPositionsOfPreviousFrame,nrf-1) = curcost(NonZeroIndicesOfPreviousFrame);
                    % assign the new cells (if any)
                    nextmatch =this.concecutiveAssignmentJointMatrix{nrf};
                    NrOfCellsInCurrentFrame = numel(nextmatch);
                    NewCellInd = setdiff((1: NrOfCellsInCurrentFrame)',curmatch);
                    TraceMat(numel(tracemat_previous)+1:numel(tracemat_previous)+length(NewCellInd),nrf)=NewCellInd ;
                    CostMat(numel(tracemat_previous)+1:numel(tracemat_previous)+length(NewCellInd),nrf-1)=Inf;
                    tracemat_previous = TraceMat(:,nrf);
                end
            end
            
            ret1 = TraceMat;
            ret2 = CostMat;
            
        end
        
        
        function ret = computeTrackingLinesInitialThreshold(this)
            
            ret = 0.75*this.terminalCost;
            
        end
        
        function [ret1 ret2] = updateTrackingLines(this)
            %take the threshold
            thresholdValue = this.trackingLinesInitialThreshold;
            % find (row,col) of entries that exceed the threshold
            [rowIndx colIndx] = find(this.trackingLinesInitialCosts>thresholdValue & this.trackingLinesInitialCosts~=Inf);
            ret1 = this.trackingLinesInitial;
            ret2 = this.trackingLinesInitialCosts;
            % we have to take each row and 'cut' it in the value > threshold
            [~ ,m, ~] = unique(rowIndx,'last');
            
            while ~isempty(rowIndx)
                % take first unique entries
                curCutsIndices = [rowIndx(m) colIndx(m)];
                % make there costs Inf and the rest of the line zero
                for k = 1:size(curCutsIndices,1)
                    % make overthresholded entry
                    ret2(curCutsIndices(k,1),curCutsIndices(k,2))=Inf;
                    % take remaining costs
                    remainingCosts = ret2(curCutsIndices(k,1),curCutsIndices(k,2)+1:end);
                    % append the line to the end
                    ret2(end+1,curCutsIndices(k,2)+1:end) = remainingCosts;
                    % make the initial line zero
                    ret2(curCutsIndices(k,1),curCutsIndices(k,2)+1:end) = 0;
                    % repeat the process for the tracking lines
                    remainingLine = ret1(curCutsIndices(k,1),curCutsIndices(k,2)+1:end);
                    ret1(end+1,curCutsIndices(k,2)+1:end) = remainingLine ;
                    ret1(curCutsIndices(k,1),curCutsIndices(k,2)+1:end) = 0;
                    
                end
                % recompute the cuts
                [rowIndx colIndx] = find(ret2>thresholdValue & ret2~=Inf);
                % we have to take each row and 'cut' it in the value > threshold
                [~ ,m, ~] = unique(rowIndx,'last');
            end
        end
        
        function constructTrackingInfoMatrix(this)
            % use information of:
            % trackingLinesAfterRecovery,nrOfCellsInFramesAfterRecovery, 
            % mergingSplittingPairs and recoveryFrame-segmentation Idxs 
            this.trackingInfoMatrix=[];
            this.trackingInfoNames = { 'track.fileSet.index','cell.index','cell.frame','track.index'...
                                       'track.spanned','track.child.of.cell','track.child.of.track',...
                                       'track.absorbs.cell', 'track.absorbs.track'};
            
            % take recovered cells
            if ~isempty(this.trackRecoverer.recoverySegmentationIndices')
                recoveryMatrix = [this.trackRecoverer.recoveryFileSetIndices' this.trackRecoverer.recoverySegmentationIndices'];
            else
                 recoveryMatrix =[];
            end
            
            
            % take split events (1= splitting)
            if ~isempty(this.startEndConnectingNodes)  
            splitMatrix = this.startEndConnectingNodes(...
                               this.startEndConnectingNodes(:,5)==1,1:4);
            else
                splitMatrix =[];
            end
            
            % take merge events (2=merging)
            
            if  ~isempty(this.startEndConnectingNodes)
                
                mergeMatrix = this.startEndConnectingNodes(...
                               this.startEndConnectingNodes(:,5)==2,1:4);
            else
              mergeMatrix =[];
            end
            
            for nrf = 1:this.nrOfFrames
                NumberOfCellsInFrame = this.trackRecoverer.nrOfCellsInFramesAfterRecovery(nrf);
                for nrc = 1:NumberOfCellsInFrame
                                     
                    % assign the tracking index
                    trackingIndx = find(this.trackRecoverer.trackingLinesAfterRecovery(:,nrf)==nrc);
                    if isempty(trackingIndx)
                       error('cell %d in fileSet %d has empty tracking index',nrc,nrf)
                    end
                    % store all the information
                    cellInfo = [nrf nrc this.fileHandler.fileSets(nrf).frameIdx trackingIndx];
                    this.addTrackingInfoOfCell(cellInfo);
                    
                end
                
            end
            
            % give the recovery tag
            this.trackingInfoMatrix = [this.trackingInfoMatrix  zeros(size(this.trackingInfoMatrix,1),1)];
            if ~isempty(recoveryMatrix)
                [~, recoveryLocationIndx] = ismember(recoveryMatrix, this.trackingInfoMatrix(:,1:2), 'rows');
                this.trackingInfoMatrix(recoveryLocationIndx,5) =1;
            end
            
            % give the 'track.child.of.(cell,track)' info 
            this.trackingInfoMatrix = [this.trackingInfoMatrix  zeros(size(this.trackingInfoMatrix,1),2)];
            if ~isempty(splitMatrix)
                [~, splitLocationIndx] = ismember( splitMatrix(:,1:2), this.trackingInfoMatrix(:,1:2), 'rows');
                [~, splitLocationIndxMotherTrack] = ismember( splitMatrix(:,3:4), this.trackingInfoMatrix(:,1:2), 'rows');
                trackChildOfCell = splitMatrix(:,4);
                trackChildOftrack= this.trackingInfoMatrix(splitLocationIndxMotherTrack,4);
                this.trackingInfoMatrix(splitLocationIndx,6:7) = [trackChildOfCell  trackChildOftrack];
            end
            
             % give motherOf index
             this.trackingInfoMatrix = [this.trackingInfoMatrix  zeros(size(this.trackingInfoMatrix,1),2)];
            if ~isempty(mergeMatrix)
                [~, mergeLocationIndx] = ismember( mergeMatrix(:,1:2), this.trackingInfoMatrix(:,1:2), 'rows'); 
                [~, mergeLocationIndxAbsorbedTrack] = ismember( mergeMatrix(:,3:4), this.trackingInfoMatrix(:,1:2), 'rows');
                trackAbsorbsTrack = this.trackingInfoMatrix(mergeLocationIndxAbsorbedTrack,4);
                trackAbsorbsCell = mergeMatrix(:,4);
                this.trackingInfoMatrix(mergeLocationIndx,8:9) = [trackAbsorbsCell trackAbsorbsTrack];
            end
        end
        
        function addTrackingInfoOfCell(this,currentCellInfo)
            this.trackingInfoMatrix = [this.trackingInfoMatrix ; currentCellInfo];            
        end
        
        
        function ret = getTrackData(this)       
            ret =  this.trackRecoverer.getRecoveredCellsStructure();
            ret.tracks = this.trackingInfoMatrix;
            ret.tracksHeader = this.trackingInfoNames;    
        end
        
        
    end
    
end



