classdef CellXTrackerStartEndConnector < handle
    %UNTITLED Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
        segmentationConfig
        fileHandler
        trackingLines
        neighborhoodMatrix
        terminatingCost
        %trackingLineCosts
        startNodesPrimary
        endNodesPrimary
        pairListOfStartEndNodes
        
        % spliting & merging
        splitingNodePairs
        mergingNodePairs
        
        % compute costs
        uniqueStartNodes
        uniqueEndNodes
        fileSetsToLoad
        loadedDataIndices
        fileSetDataMatrices
        fileSetSegmentationMasks
        pairIndicesToComputeCosts
        startEndCostMatrix
        remainingPairIndicesToComputeCosts
        
        % assignments
        startEndNodeAssignment
        startEndConnectingNodes
        
        % refined tracking lines
        updatedTrackingLines
        
    end
    
    methods
        
        function obj = CellXTrackerStartEndConnector(config,fset,tlines,neighbors,terminalCost)
            obj.segmentationConfig   = config;
            obj.fileHandler = fset;
            obj.trackingLines = tlines;
            obj.terminatingCost = terminalCost;
            obj.neighborhoodMatrix = neighbors;
            
        end
        
        function run(this)
            
            % 1. locate initial start-end nodes from the  trackingLines
            %     and costruct the pair list the entries of the cost matrix
            %      that we will compute later on)
            
            [this.startNodesPrimary, this.endNodesPrimary] = ...
                this.computePrimaryStartEndNodes();
            
            this.pairListOfStartEndNodes = this.findPairListOfPrimaryStartEndNodes();
            
            if ~isempty(this.pairListOfStartEndNodes)
                
                % 2. augment the system with nodes necessary for
                %    splitting (i.e backward neighboring nodes of start-nodes)
                %    & merging (i.e forward neighboring nodes of end-nodes)
                
                this.splitingNodePairs =  this.findSplittingNodes();
                this.addPairsInList(this.splitingNodePairs)
                
                this.mergingNodePairs = this.findMergingNodes();
                this.addPairsInList(this.mergingNodePairs)
                
                
                % 3. compute the costs of the augmented start-end node pair List
                this.remainingPairIndicesToComputeCosts = (1:size(this.pairListOfStartEndNodes,1))';
                % compute unique start-nodes & end-nodes
                this.uniqueStartNodes =  unique(this.pairListOfStartEndNodes(:,1:2),'rows');
                this.uniqueEndNodes =  unique(this.pairListOfStartEndNodes(:,3:4),'rows');
                this.startEndCostMatrix = Inf*ones(size( this.uniqueEndNodes,1),size( this.uniqueStartNodes,1));
                this.loadedDataIndices = [];
                while ~isempty(this.remainingPairIndicesToComputeCosts)
                    
                    % 3.1 first find optimal frames to load
                    this.fileSetsToLoad = this.findOptimalFleSetsToLoad();
                    % 3.2 find pair-Indices to compute costs
                    this.pairIndicesToComputeCosts = this.findPairIndicesToComputeCosts();
                    % 3.3 compute the costs
                    this.computeStartEndCosts();
                    % 3.4 update the remaining pair list
                    this.remainingPairIndicesToComputeCosts = this.updatePairIndicesToComputeCosts();
                end
                
                % augment the cost matrix with the diagonal
                % end-terminating matrix
                this.addDiagonalTerminatingCostMatrix();
                
                % 4. do the weighted graph matching
                this.startEndNodeAssignment = this.doStartEndWeightedGraphMatching();
                
                % 5. finalize tracking lines
                connectingPairsIndx = this.startEndNodeAssignment>0 ...
                    & this.startEndNodeAssignment<= size(this.uniqueStartNodes,1);
                connectingEndNodes = this.uniqueEndNodes(connectingPairsIndx,:);
                connectingStartNodes = this.uniqueStartNodes(this.startEndNodeAssignment(connectingPairsIndx),:);
                this.startEndConnectingNodes = [ connectingStartNodes connectingEndNodes];
                % and augment their tags
                startEndConnectingNodesTags = zeros(size(this.startEndConnectingNodes,1),1);
                for nrcn=1:size(this.startEndConnectingNodes,1)
                    tf = ismember(this.pairListOfStartEndNodes(:,1:4), this.startEndConnectingNodes(nrcn,:),'rows');
                    startEndConnectingNodesTags(nrcn,1) = this.pairListOfStartEndNodes(tf,5);
                end
                this.startEndConnectingNodes = [ this.startEndConnectingNodes, startEndConnectingNodesTags];
                % 6. empty some entries
                this.fileSetDataMatrices = [];
                this.fileSetSegmentationMasks = [];
                
                % 7. refine tracking lines
                this.updatedTrackingLines = this.refineTrackingLines();
            else
                this.updatedTrackingLines = this.trackingLines;
            end
            
        end
        
        
        function [ret1 ret2] = computePrimaryStartEndNodes(this)
            % 0->X is a start
            % X->0 is an end
            % where X is a positive segmentation index
            
            ret1=[];% primary starts
            ret2=[];% primary ends
            trackLines = this.trackingLines;
            trackLInesBinary = (trackLines>0);
            trackLInesBinaryDiff  = trackLInesBinary(:,2:end)-trackLInesBinary(:,1:end-1);
            % now find  the 'starts' = 1
            [startNodeIndicesRow startNodeIndicesCol]  = find(trackLInesBinaryDiff==1);
            startNodeFileSetIndices = startNodeIndicesCol+1;
            startNodeIndicesLinear = sub2ind(size(trackLines),startNodeIndicesRow ,  startNodeFileSetIndices);
            startNodeSegmentationIndices  = this.trackingLines(startNodeIndicesLinear);
            ret1.fileSetIndices = startNodeFileSetIndices;
            ret1.segmentationIndices = startNodeSegmentationIndices;
            % and find the 'ends' = -1
            [endNodeIndicesRow endNodeIndicesCol]  = find(trackLInesBinaryDiff==-1);
            endNodeFileSetIndices = endNodeIndicesCol;
            endNodeIndicesLinear = sub2ind(size(trackLines),endNodeIndicesRow ,  endNodeFileSetIndices);
            endNodeSegmentationIndices  = this.trackingLines(endNodeIndicesLinear);
            ret2.fileSetIndices = endNodeFileSetIndices;
            ret2.segmentationIndices = endNodeSegmentationIndices;
        end
        
        function ret = findPairListOfPrimaryStartEndNodes(this)
            
            ret=[];
            % 1. starts can only get connected with ends that appear
            % before in time
            endNodesFileSetIndices = this.endNodesPrimary.fileSetIndices;
            endNodesSegmentationIndices = this.endNodesPrimary.segmentationIndices;
            nrOfStartNodes = numel(this.startNodesPrimary.fileSetIndices);
            for k=1: nrOfStartNodes
                currentStartNodeFileSetIndx = this.startNodesPrimary.fileSetIndices(k);
                currentStartNodeSegmentationIndx = this.startNodesPrimary.segmentationIndices(k);
                % find possible ends
                possibleEndNodesIndices = (endNodesFileSetIndices < currentStartNodeFileSetIndx &...
                    endNodesFileSetIndices >= currentStartNodeFileSetIndx - this.segmentationConfig.maxGapConnectionDistance -1);
                
                if ~isempty( possibleEndNodesIndices )
                    
                    possibleEndNodes = [ endNodesFileSetIndices(possibleEndNodesIndices),...
                        endNodesSegmentationIndices(possibleEndNodesIndices)];
                    
                    ret = [ret; repmat([currentStartNodeFileSetIndx,currentStartNodeSegmentationIndx],...
                        size(possibleEndNodes,1),1),possibleEndNodes, zeros(size(possibleEndNodes,1),1)];
                end
                
            end
            
            
        end
        
        
        function ret = findSplittingNodes(this)
            % 1. loop through the start-node list
            % 2. for each end node, find current neighbors
            % 3. backtrace them to the previous frames
            % 4. make the backtraced nodes 'new ends'
            ret = [];
            nrOfStartNodes = numel(this.startNodesPrimary.fileSetIndices);
            
            for k = 1: nrOfStartNodes
                % step 1
                currentStartNodeFileSetIndx = this.startNodesPrimary.fileSetIndices(k);
                currentStartNodeSegmentationIndx = this.startNodesPrimary.segmentationIndices(k);
                % step 2
                currentStartNodeNeighb = this.neighborhoodMatrix{currentStartNodeFileSetIndx}(currentStartNodeSegmentationIndx,:);
                currentStartNodeNeighb = currentStartNodeNeighb(currentStartNodeNeighb>0);
                % step 3 (take only ones that are tracked)
                for bn=1:numel(currentStartNodeNeighb)
                    curnode =  currentStartNodeNeighb(bn);
                    n = this.trackingLines(:,currentStartNodeFileSetIndx)== curnode;
                    if any(n)
                        currBackNeighbSegmentationIndx = this.trackingLines(n,currentStartNodeFileSetIndx-1);
                        if  currBackNeighbSegmentationIndx >0
                            % stp 4
                            ret = [ret;currentStartNodeFileSetIndx, currentStartNodeSegmentationIndx,...
                                currentStartNodeFileSetIndx-1, currBackNeighbSegmentationIndx, 1];
                            
                        end
                    end
                    
                end
                
                
            end
            
        end
        
        
        function ret = findMergingNodes(this)
            
            % 1. loop through the end-node list
            % 2. for each end node, find current neighbors
            % 3. trace them to forward frames
            % 4. make the forward-traced-nodes 'new starts'
            ret = [];
            nrOfEndNodes = numel(this.endNodesPrimary.fileSetIndices);
            
            for k = 1:  nrOfEndNodes
                % step 1
                currentEndNodeFileSetIndx = this.endNodesPrimary.fileSetIndices(k);
                currentEndNodeSegmentationIndx = this.endNodesPrimary.segmentationIndices(k);
                % step 2
                currentEndNodeNeighb = this.neighborhoodMatrix{currentEndNodeFileSetIndx}(currentEndNodeSegmentationIndx,:);
                currentEndNodeNeighb = currentEndNodeNeighb(currentEndNodeNeighb>0);
                % step 3 (take only ones that are tracked)
                for bn=1:numel(currentEndNodeNeighb)
                    curnode =  currentEndNodeNeighb(bn);
                    n = this.trackingLines(:,currentEndNodeFileSetIndx)== curnode;
                    if any(n)
                        currForwardNeighbSegmentationIndx = this.trackingLines(n,currentEndNodeFileSetIndx+1);
                        if  currForwardNeighbSegmentationIndx >0
                            % step 4
                            ret = [ret;currentEndNodeFileSetIndx+1, currForwardNeighbSegmentationIndx,...
                                currentEndNodeFileSetIndx, currentEndNodeSegmentationIndx,2 ];
                            
                        end
                    end
                    
                end
                
                
            end
            
        end
        
        
        function addPairsInList(this,newNodes)
            
            this.pairListOfStartEndNodes = [this.pairListOfStartEndNodes;newNodes];
            
        end
        
        function ret = findOptimalFleSetsToLoad(this)
            
            % take all participating frames
            allFileSetPairs = this.pairListOfStartEndNodes(this.remainingPairIndicesToComputeCosts,[1 3]);
            uniqueFs1 = unique(allFileSetPairs(:,1));
            uniqueFs1Counts = zeros(numel(uniqueFs1),1);
            for nrc = 1: numel(uniqueFs1)
                uniqueFs1Counts(nrc) = sum((allFileSetPairs(:,1) == uniqueFs1(nrc,1)));
            end
            % sort them
            [~, indices_counts] = sort(uniqueFs1Counts,'descend');
            % we start with the frame with the largest number of hits
            primaryFileSetToLoad  = uniqueFs1(indices_counts(1));
            % find complementary frames to load
            complementaryFileSetsToLoad  = unique(allFileSetPairs( allFileSetPairs(:,1)==primaryFileSetToLoad,2));
            % take min and max, and load also the frames in-between
            currentMinFrame = min(min(complementaryFileSetsToLoad),primaryFileSetToLoad);
            currentMaxFrame = max(max(complementaryFileSetsToLoad),primaryFileSetToLoad);
            % frames to load...
            ret = currentMinFrame:currentMaxFrame;
            
        end
        
        function ret = findPairIndicesToComputeCosts(this)
            
            % we need to find all the pairs from the
            % pairListOfStartEndNodes that can be
            % computed given the fileSetsToLoad
            pairListFileSets = this.pairListOfStartEndNodes(this.remainingPairIndicesToComputeCosts,[1 3]);
            loadedFileSets = this.fileSetsToLoad;
            pairListIndices = pairListFileSets(:,1)>=min(loadedFileSets) & pairListFileSets(:,1)<=max(loadedFileSets) & ...
                pairListFileSets(:,2)>=min(loadedFileSets) & pairListFileSets(:,2)<=max(loadedFileSets) ;
            
            ret = this.remainingPairIndicesToComputeCosts( pairListIndices,:);
        end
        
        
        function ret = updatePairIndicesToComputeCosts(this)
            % take the difference between the remainingPairIndicesToComputeCosts
            %  and the pairIndicesToComputeCost
            
            ret = setdiff(this.remainingPairIndicesToComputeCosts,this.pairIndicesToComputeCosts);
            
        end
        
        function computeStartEndCosts(this)
            
            % load the required frame data
            [this.fileSetDataMatrices this.fileSetSegmentationMasks] = this.loadConnectorData();
            % loop through the pairs that we need to compute costs
            for pin = 1:numel(this.pairIndicesToComputeCosts)
                curPair = this.pairListOfStartEndNodes(this.pairIndicesToComputeCosts(pin),1:4);
                curPairCostTag = this.pairListOfStartEndNodes(this.pairIndicesToComputeCosts(pin),5);
                curCost = this.computeCostOfPair(curPair,curPairCostTag);
                rowIndxOfCostMatrix = ismember(this.uniqueEndNodes ,curPair(3:4),'rows');
                colIndxOfCostMatrix = ismember(this.uniqueStartNodes ,curPair(1:2),'rows');
                this.startEndCostMatrix(rowIndxOfCostMatrix,colIndxOfCostMatrix) = curCost;
            end
        end
        
        function [ret1 ret2] = loadConnectorData(this)
            
            ret1 =cell(1,numel(this.fileSetsToLoad));
            ret2 = cell(1,numel(this.fileSetsToLoad));
            for nrf = 1:numel(this.fileSetsToLoad)
                
                d1 = load(this.fileHandler.fileSets(this.fileSetsToLoad(nrf)).seedsMatFile);
                seedVarName =  fieldnames(d1);
                ret1{nrf} = d1.(genvarname(seedVarName{1}));
                
                m1 = load(this.fileHandler.fileSets(this.fileSetsToLoad(nrf)).maskMatFile);
                matVarName =  fieldnames(m1);
                ret2{nrf} = m1.(genvarname(matVarName{1}));
                
            end
            this.loadedDataIndices =this.fileSetsToLoad;
            
            
        end
        
        function ret = computeCostOfPair(this,pair, pairCostTag)
            
            fileSetIndex1 = pair(1);
            segmentationIndex1 = pair(2);
            fileSetIndex2 = pair(3);
            segmentationIndex2 = pair(4);

            % gap-filling case (pairCostTag==0)
            % requires using intermediate frames for overlaping term
            if pairCostTag==0
                
                % gapping cost (shape(25%) + distance(25%) + overlap(50%) )
               gappingCost = this.computePrimaryCostOf2Cells([fileSetIndex1, segmentationIndex1],...
                                                          [fileSetIndex2, segmentationIndex2 ],pairCostTag);
                
                % additional overlap cost (result is 0 or Inf)
                additionalOverlapGapingCost = this.computeOverlapGapingCostOf2Cells([fileSetIndex1, segmentationIndex1],...
                    [fileSetIndex2, segmentationIndex2 ]);
                
                ret = gappingCost + additionalOverlapGapingCost;
                
            else % merging-splitting case
                
                 % compute merging-splitting cost (overlap(75%) + distance(25%))
                 mergingSplittingCost = this.computeMergingSplittingCostOf2Cells([fileSetIndex1, segmentationIndex1],...
                                                          [fileSetIndex2, segmentationIndex2 ],pairCostTag);

                additionalOverlapMSCost=0;
                ret =  mergingSplittingCost + additionalOverlapMSCost;
                
            end
            
        end
        
        function ret  = computePrimaryCostOf2Cells(this, cell1,cell2, pairCostTag)
            
            % 1. take the means/stds of the defined distance
            % distributions
            FileSetOfCell1Data = this.fileSetDataMatrices{this.fileSetsToLoad==cell1(1)};
            FileSetOfCell2Data = this.fileSetDataMatrices{this.fileSetsToLoad==cell2(1)};
            Cell1Data =  FileSetOfCell1Data(cell1(2));
            Cell2Data =  FileSetOfCell2Data(cell2(2));
            
            % compute distance cost
            distanceCost = this.computeDistanceCost(Cell1Data,Cell2Data);
            
            % 2. compute overlap cost
            overlapCost = this.computeOverlapCost(Cell1Data,Cell2Data, pairCostTag);
            
            % 3. compute shape cost
             shapeCost = this.computeShapeCost(Cell1Data,Cell2Data);
            
            % produce total cost
            ret = distanceCost + shapeCost + 2*overlapCost;
            
        end
        
        function ret  = computeMergingSplittingCostOf2Cells(this, cell1,cell2, pairCostTag)
            
            % 1. take the means/stds of the defined distance
            % distributions
            FileSetOfCell1Data = this.fileSetDataMatrices{this.fileSetsToLoad==cell1(1)};
            FileSetOfCell2Data = this.fileSetDataMatrices{this.fileSetsToLoad==cell2(1)};
            Cell1Data =  FileSetOfCell1Data(cell1(2));
            Cell2Data =  FileSetOfCell2Data(cell2(2));
            
            % compute distance cost
            distanceCost = this.computeDistanceCost(Cell1Data,Cell2Data);
            
            % 2. compute overlap cost
            overlapCost = this.computeOverlapCost(Cell1Data,Cell2Data, pairCostTag);
                     
            % produce total cost
            ret = distanceCost + 3*overlapCost;
            
        end
        

        function ret = computeDistanceCost(this,cell1,cell2)
            
             maxAllowedDistance = this.segmentationConfig.spatialMotionFactor;
             
             ck =cell1.centroid;
             cl =cell2.centroid;
             % compute euklidean distance
             currentCellDistance = sqrt(sum( (ck-cl).^2 ));
             currentCellCost = currentCellDistance/maxAllowedDistance;
             
             % assign the costs
             ret = currentCellCost;
             
        end
        
        function ret = computeShapeCost(this,cell1,cell2)
            
            % take the means/stds of the defined distance
            % distributions
            ck =[cell1.majorAxisLength,...
                 cell1.majorAxisLength,...
                round(cell1.orientation)];
            
            cl =[cell2.majorAxisLength,...
                cell2.majorAxisLength,...
                round(cell2.orientation)];
            
            refck = [ck(1:2) 180];
            refcl = [cl(1:2) 180];
            
            maxAllowedCost = sqrt(sum([0.25 0.25 0.5].^2));
            
            % compute euklidean distance
            currentCellDistance_ck = sqrt(sum(((ck-cl)./refck).^2 ))/maxAllowedCost;
            currentCellDistance_cl = sqrt(sum(((ck-cl)./refcl).^2 ))/maxAllowedCost;
            
            
            % assign the costs
            ret = (currentCellDistance_ck+ currentCellDistance_cl)/2;
            
        end
        
        function ret = computeOverlapCost(this,cell1,cell2,pairCostTag)
            
            overlapFraction = zeros(1,2);
            LindCell1 = cell1.cellPixelListLindx;
            LindCell2 = cell2.cellPixelListLindx;
            
            overlapFraction(1) = numel(intersect(LindCell1, LindCell2))/numel(LindCell1);
            overlapFraction(2) = numel(intersect(LindCell1, LindCell2))/numel(LindCell2);
            
            if pairCostTag == 0
                
               ret =   mean([1 1] - overlapFraction);
               
            elseif pairCostTag == 1
                
                ret =   1-overlapFraction(1); 
                
            elseif pairCostTag == 2
                
                ret =   1-overlapFraction(2); 
                
            end
            
            
        end
        
        
        function ret  = computeOverlapGapingCostOf2Cells(this, cell1,cell2)
            ret = 0;
            if cell1(1)-cell2(1)>1
                
                FileSetOfCell1Data = this.fileSetDataMatrices{this.fileSetsToLoad==cell1(1)};
                FileSetOfCell2Data = this.fileSetDataMatrices{this.fileSetsToLoad==cell2(1)};
                
                startNodeEllipseData = [FileSetOfCell1Data(cell1(2)).centroid';...
                    FileSetOfCell1Data(cell1(2)).majorAxisLength;...
                    FileSetOfCell1Data(cell1(2)).minorAxisLength;...
                    FileSetOfCell1Data(cell1(2)).orientation];
                
                endNodeEllipseData = [FileSetOfCell2Data(cell2(2)).centroid';...
                    FileSetOfCell2Data(cell2(2)).majorAxisLength;...
                    FileSetOfCell2Data(cell2(2)).minorAxisLength;...
                    FileSetOfCell2Data(cell2(2)).orientation];
                
                fileSetIndToComputeCost =(cell2(1)+1:cell1(1)-1);
                nrOfFileSetToComputeCost = numel(fileSetIndToComputeCost);
                cnt=0;
                while (ret~=Inf) && cnt<nrOfFileSetToComputeCost
                    cnt=cnt+1;
                    InBetweenFileSetIndx = fileSetIndToComputeCost(cnt);
                    InBetweenSegmentationMask = this.fileSetSegmentationMasks{this.fileSetsToLoad==InBetweenFileSetIndx};
                    
                     freeAreaPercentage = this.computeFreeAreaPercentage(InBetweenSegmentationMask,...
                        startNodeEllipseData,...
                        endNodeEllipseData);
                    
                    if freeAreaPercentage<0.75
                        ret = Inf;
                    end
                    
                end
                
            end
            
        end
        
        function ret = computeFreeAreaPercentage(this,InBetweenSegmentationMask,cell1Data, cell2Data)
          
            %----- first we have to find the new defined ellipse coordinates
            ImHeight =size(InBetweenSegmentationMask,1);
            ImWidth  =size(InBetweenSegmentationMask,2);
 
            % get new cell's ellipse data
            cellEllipseData = mean( [cell1Data cell2Data],2);
            xbar= cellEllipseData(1);
            ybar= cellEllipseData(2);
            a = cellEllipseData(3)/2;
            b = cellEllipseData(4)/2;
            theta =  pi*cellEllipseData(5)/180;
            
            % get spatial motion parameter
            spatialMotion = this.segmentationConfig.spatialMotionFactor;
            
            % find cells within the window defided by the spatial motion
            xBoundLeft = round(max(xbar - spatialMotion - a,1));
            xBoundRight = round(min(xbar + spatialMotion + a,ImWidth));
            yBoundUp = round(max(ybar - spatialMotion - a,1));
            yBoundDown = round(min(ybar + spatialMotion + a, ImHeight));
            
            % crop the mask given the bounding box
            rectCropingRegion = [ xBoundLeft  yBoundUp xBoundRight-xBoundLeft  yBoundDown-yBoundUp];
            croppedMask = imcrop(InBetweenSegmentationMask,rectCropingRegion);
            % computed the distance image of the segmantation croppped mask
            croppedMaskDist = bwdist(~(croppedMask>0));
            
            % construct the ellipse contour
            phi = linspace(0,2*pi,60);
            cosphi = cos(phi);
            sinphi = sin(phi);
            R = [ cos(theta)   sin(theta)
                -sin(theta)   cos(theta)];
            
            xy = [a*cosphi; b*sinphi];
            xy = R*xy;
            
            x = xy(1,:) + spatialMotion + a;
            y = xy(2,:) + spatialMotion + a;
            
            % construct the hypothetical ellipse mask
            ellipseBinaryMask = poly2mask(x, y, rectCropingRegion(4)+1, rectCropingRegion(3)+1);
            %croppedEllipseMask = imcrop(ellipseBinaryMask,rectCropingRegion);
            Lind = find(  ellipseBinaryMask>0);
            % computed the distance image of the segmantation croppped mask
            croppedEllipseMaskDist = bwdist(~ ellipseBinaryMask);
            
            % return the percentage of the  croppedEllipseMaskDist that
            % has values higher than the croppedMaskDist
            distDifference = croppedEllipseMaskDist(Lind)-croppedMaskDist(Lind);
            ret = sum(distDifference>0)/numel(Lind);
            
        end
        
            
        function ret = computeInterpolatedEllipseLInds(this,InBetweenSegmentationMask,cell1Data, cell2Data)
            
            %----- first we have to find the new defined ellipse coordinates
            ImHeight =size(InBetweenSegmentationMask,1);
            ImWidth  =size(InBetweenSegmentationMask,2);
            % initial ellipse mask
            EllMask = zeros(ImHeight,ImWidth);
            
            % get new cell's ellipse data
            cellEllipseData = mean( [cell1Data cell2Data],2);
            xbar= cellEllipseData(1);
            ybar= cellEllipseData(2);
            a = cellEllipseData(3)/2;
            b = cellEllipseData(4)/2;
            theta =  pi*cellEllipseData(5)/180;
            
            % construct the ellipse contour
            phi = linspace(0,2*pi,200);
            cosphi = cos(phi);
            sinphi = sin(phi);
            R = [ cos(theta)   sin(theta)
                -sin(theta)   cos(theta)];
            
            xy = [a*cosphi; b*sinphi];
            xy = R*xy;
            
            x = xy(1,:) + xbar;
            y = xy(2,:) + ybar;
            
            % construct the ellipse area
            [cx cy ~] = improfile(EllMask,x,y);
            cxr = round(cx);
            cyr = round(cy);
            EllMaskUpd = full(sparse(cyr , cxr,  1, ImHeight,ImWidth ));
            EllMaskDisk = imfill(EllMaskUpd);
            % take the coordinates
            ret= find( EllMaskDisk >0);
            %----- end of finding the new defined ellipse coordinates
        end
        
        
        
        function addDiagonalTerminatingCostMatrix(this)
            
            nrOfEndNodes = size(this.uniqueEndNodes,1);
            additionalCostMatrix = Inf*ones(nrOfEndNodes ,nrOfEndNodes );
            additionalCostMatrix(logical(eye(nrOfEndNodes))) =  this.terminatingCost;
            this.startEndCostMatrix = [ this.startEndCostMatrix  additionalCostMatrix];
            
        end
        
        
        function ret = doStartEndWeightedGraphMatching(this)
            
            G = this.startEndCostMatrix;
            [assignment, ~ , ~] = munkres(G);
            ret = assignment;
            
        end
        
        function ret = refineTrackingLines(this)
            
            tempTrackingLines = this.trackingLines;
            gapfillingConnectorPairs = this.startEndConnectingNodes(this.startEndConnectingNodes(:,5)==0 ,1:4);
            trackingLineNumber =  1;
            while trackingLineNumber<=size(tempTrackingLines,1)
                startFinishSearchTag =1;
                endFinishSearchTag =1;
                currentTrackingLine = tempTrackingLines( trackingLineNumber,:);
                
                while ( startFinishSearchTag || endFinishSearchTag)
                    
                    while (startFinishSearchTag)
                        logind = find(currentTrackingLine>0);
                        fileSetOfStart = logind(1);
                        if fileSetOfStart==1
                            startFinishSearchTag=0;
                        else
                            
                            currentTrackingLineStart = [fileSetOfStart currentTrackingLine(fileSetOfStart)];
                            tf = ismember(gapfillingConnectorPairs(:,1:2),  currentTrackingLineStart,'rows');
                            if any(tf)
                                newTrackingLineEnd = gapfillingConnectorPairs(tf,3:4);
                                connectingTrackingLineIndex =  tempTrackingLines(:,newTrackingLineEnd(1))==newTrackingLineEnd(2);
                                % connect tracking lines
                                a = 1:newTrackingLineEnd(1);
                                tempTrackingLines(trackingLineNumber,a) =  tempTrackingLines(connectingTrackingLineIndex,a);
                                % remove entry from gapfillingConnectorPairs
                                gapfillingConnectorPairs =  gapfillingConnectorPairs(~tf,:);
                                % remove  connectingTrackingLine from tempTrackingLines
                                tempTrackingLines =  tempTrackingLines(~connectingTrackingLineIndex,:);
                                % update current tracking line
                                currentTrackingLine = tempTrackingLines( trackingLineNumber,:);
                            else
                                startFinishSearchTag=0;
                            end
                            
                        end
                        
                    end
                    
                    while (endFinishSearchTag)
                        logind = find(currentTrackingLine>0);
                        fileSetOfEnd = logind(end);
                        if fileSetOfEnd == size(tempTrackingLines,2)
                            endFinishSearchTag=0;
                        else
                            currentTrackingLineEnd = [fileSetOfEnd currentTrackingLine(fileSetOfEnd)];
                            tf = ismember(gapfillingConnectorPairs(:,3:4), currentTrackingLineEnd,'rows');
                            if any(tf)
                                newTrackingLineStart = gapfillingConnectorPairs(tf,1:2);
                                connectingTrackingLineIndex =  tempTrackingLines(:,newTrackingLineStart(1))==newTrackingLineStart(2);
                                % connect tracking lines
                                a = newTrackingLineStart(1):size(tempTrackingLines,2);
                                tempTrackingLines(trackingLineNumber,a) =  tempTrackingLines(connectingTrackingLineIndex,a);
                                % remove entry from gapfillingConnectorPairs
                                gapfillingConnectorPairs =  gapfillingConnectorPairs(~tf,:);
                                % remove  connectingTrackingLine from tempTrackingLines
                                tempTrackingLines =  tempTrackingLines(~connectingTrackingLineIndex,:);
                                % update current tracking line
                                currentTrackingLine = tempTrackingLines( trackingLineNumber,:);
                            else
                                endFinishSearchTag=0;
                            end
                            
                            
                            
                        end
                        
                    end
                    
                end
                
                trackingLineNumber = trackingLineNumber+1;
                
            end
            
            ret=tempTrackingLines;
        end
        
        
        
        
    end
    
end

