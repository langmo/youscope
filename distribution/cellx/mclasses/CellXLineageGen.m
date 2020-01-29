classdef CellXLineageGen
    %UNTITLED Summary of this class goes here
    %   Detailed explanation goes here
       
    methods (Static)
        
        
        function ret = addPairInTheList(mainList, newPair)
        
             ret = [mainList;newPair];
             
        end
        
        
        function [ret1 ret2] = findDevisionEvents(trackingInfoMatrix)
            
            %---find for each tracking line (i.e main line), the set
            % of tracking lines are their mothers (i.e secondary lines)
            daughertAndMotherPairs=[];
%            mothersPlusAbsorbedPairs =[];
            lineEventInds = find(trackingInfoMatrix(:,7)>0) ;
            fileSetLocationOfDivisionEvent = zeros(numel(lineEventInds),1);
            for lev = 1:numel(lineEventInds)
                eventPosition =  lineEventInds(lev);
                
                if trackingInfoMatrix(eventPosition,7) >0
                    
                    secondaryLineIndx = trackingInfoMatrix(eventPosition,7) ;

                end
                
                mainLineInds =  trackingInfoMatrix(eventPosition,4) ;
                
                daughertAndMotherPairs = CellXLineageGen.addPairInTheList(daughertAndMotherPairs,...
                                            [mainLineInds  secondaryLineIndx ] );
                                        
                fileSetLocationOfDivisionEvent(lev,1) = trackingInfoMatrix(eventPosition,1);
                
            end
            
            ret1 = daughertAndMotherPairs;
            ret2 = fileSetLocationOfDivisionEvent;
            
        end
        
       function [ret1 ret2] = findMergingEvents(trackingInfoMatrix)
            
            %---find for each tracking line (i.e main line), the set
            % of tracking lines are their mothers (i.e secondary lines)
            mergingPairs=[];
            lineEventInds = find(trackingInfoMatrix(:,9)>0) ;
            fileSetLocationOfMergingEvent = zeros(numel(lineEventInds),1);
            for lev = 1:numel(lineEventInds)
                eventPosition =  lineEventInds(lev);
                
                if trackingInfoMatrix(eventPosition,9) >0
                    
                    secondaryLineIndx = trackingInfoMatrix(eventPosition,9) ;

                end
                
                mainLineInds =  trackingInfoMatrix(eventPosition,4) ;
                
                mergingPairs = CellXLineageGen.addPairInTheList( mergingPairs,...
                                            [mainLineInds  secondaryLineIndx ] );
                                        
                fileSetLocationOfMergingEvent(lev,1) = trackingInfoMatrix(eventPosition,1);
                
            end
            
            ret1 = mergingPairs;
            ret2 = fileSetLocationOfMergingEvent;
            
        end
        
        function ret = removeShortLivedDivisionEvents(daughertAndMotherPairs, trackingInfoMatrix)
           
           
            % b) remove 'short-lived' daughters
            
            nrFileSets = max(trackingInfoMatrix(:,1)); 
            
            if ~isempty(daughertAndMotherPairs)
                daughters = daughertAndMotherPairs(:,1);
                nrOfDaughters = numel(daughters);
                daughterApearanceCounter = zeros(nrOfDaughters,1);
                daughterApearanceLastFileSet = zeros(nrOfDaughters,1);
                for nd = 1:nrOfDaughters
                    curDaughter = daughters(nd,1);
                    daughterApearanceIndices = trackingInfoMatrix(:,4)==curDaughter;
                    daughterApearanceCounter(nd,1) = sum(daughterApearanceIndices);
                    daughterApearanceLastFileSet(nd,1) = max(trackingInfoMatrix(daughterApearanceIndices,1));
                end
                acceptTag = ones(numel(daughters),1);
                for nd = 1:nrOfDaughters
                     if daughterApearanceCounter(nd,1)<=3 &&  daughterApearanceLastFileSet(nd,1)~= nrFileSets
                        acceptTag(nd,1)=0; 
                     end
                end
                
                ret = daughertAndMotherPairs(acceptTag>0,:);
                
            else
                ret=[];
            end
        
        end
        
        function ret = addPairsWithNoMothers(trackingInfoMatrix,daughertAndMotherPairs)
            
            % these pairs have their second-column element equal to 0
            
            % take total number of tracking lines
            nrOfTrackingLines = max(trackingInfoMatrix(:,4));
            
             allMainLines = (1:nrOfTrackingLines)';
            if ~isempty(daughertAndMotherPairs)
               nonAppearingMainLineIndx = ~ismember(allMainLines,daughertAndMotherPairs(:,1));
            else
               nonAppearingMainLineIndx = ~ismember(allMainLines,[]);
            end
            nonAppearingMainLines =  allMainLines(nonAppearingMainLineIndx);
        
            ret = [nonAppearingMainLines zeros(numel(nonAppearingMainLines),1)];
        
        end
        
        
        function ret = orderTrackingLines(allLineagePairs)
            
            nrOfTrackingLines = size(allLineagePairs,1);
            
            TrackIndexPositionInYaxis = zeros(nrOfTrackingLines,1);
            rowAvailability = ones(nrOfTrackingLines,1);
            
            curmom = 0;

            cnt=0;
            loopcnt =0;
            
            while any( ~(TrackIndexPositionInYaxis>0) )
                
                % just for safety reasons
                loopcnt=loopcnt+1;
                if loopcnt > 1e6
                    fprintf('failed to produce lineage plot')
                    break;
                end
                
                % get the daughters
                IndicesOfDaughters = find(allLineagePairs(:,2)==curmom & rowAvailability);
                daughters = allLineagePairs(IndicesOfDaughters ,1);
                
                % check if duaghters exist
                if ~isempty(daughters) % if daughter exists
                    % take the last one
                    curmom = daughters(end);
                    curmom_index = IndicesOfDaughters(end);
                    % write it in the series
                    cnt=cnt+1;
                    TrackIndexPositionInYaxis(curmom_index) = cnt;
                    % and make this row unavailable for search
                    rowAvailability(curmom_index) = 0;
                    
                else
                    
                    % update levels and old mom
                    curmom_index = allLineagePairs(:,1)==curmom;
                    oldmom = allLineagePairs(curmom_index,2);
                    curmom = oldmom;
                    
                end
                
            end
            
           ret = TrackIndexPositionInYaxis;
            
        end
        
        function printLineageFigure(lineagePairs,TrackIndexPositionInYaxis,trackingInfoMatrix,lineageFileName)
            
            % set the drawing Parameters
            lineWidth = 2;
            lineSpacing = 4;
            pointWidth = 2; 
            pointSpacing = 4;
            rowBlockSize = lineWidth+lineSpacing;
            colBlockSize =  pointWidth+pointSpacing;
            % take total number of tracking lines
            nrOfTrackingLines = numel(TrackIndexPositionInYaxis);
            
            % take total number of file Sets
            nrOfFileSets = max(trackingInfoMatrix(:,1));
            
            % generate initial image with the following characteristics
            imageLength =  lineWidth*nrOfTrackingLines + lineSpacing*(nrOfTrackingLines);
            imageWidth =   pointWidth*nrOfFileSets +  pointSpacing*(nrOfFileSets); 
            lineageMatrix = ones(imageLength,imageWidth,3);
             
            YtickLabelValsOnImage = zeros(nrOfTrackingLines,1);
            YtickOnImage = zeros(nrOfTrackingLines,1);
            StartingPositionOnImage = zeros(nrOfTrackingLines,1);
            for trackInds = 1:nrOfTrackingLines
                
                % data of this track
                currentTrackNumber = trackInds;
                
                trackIndexInLineagePairs =find(lineagePairs(:,1)==currentTrackNumber);
                Yposition = TrackIndexPositionInYaxis(trackIndexInLineagePairs);
                
                appearingFileSets = trackingInfoMatrix( trackingInfoMatrix(:,4)==trackInds,1);
                recoveredFileSets = appearingFileSets( find(trackingInfoMatrix( trackingInfoMatrix(:,4)==trackInds,5)) );
%                segmentationIndices = trackingInfoMatrix( trackingInfoMatrix(:,4)==trackInds,2);

                YtickLabelValsOnImage(trackInds,1) = currentTrackNumber;
                YtickOnImage(trackInds,1) = rowBlockSize*(Yposition-1)+round((1+lineWidth)/2);
                
                StartingPositionOnImage(trackInds,1) = colBlockSize*(appearingFileSets(1)-1);                
                
                if appearingFileSets(end)==nrOfFileSets
                    
                    % place the tracking line into the image
                    lineageMatrix(rowBlockSize*(Yposition-1)+1:rowBlockSize*(Yposition-1)+lineWidth,...
                        colBlockSize*(appearingFileSets(1)-1)+1:colBlockSize*(appearingFileSets(end)-1)+lineWidth+pointSpacing,...
                        :) = 0;                    
                else
                    
                    % place the tracking line into the image
                    lineageMatrix(rowBlockSize*(Yposition-1)+1:rowBlockSize*(Yposition-1)+lineWidth,...
                        colBlockSize*(appearingFileSets(1)-1)+1:colBlockSize*(appearingFileSets(end)-1)+lineWidth,...
                        :) = 0;
                    
                end       
                % mark any recovered cells of this track        
                if ~isempty(recoveredFileSets)
                    for rfs = 1:numel(recoveredFileSets)
                        % place the tracking line into the image
                        lineageMatrix(rowBlockSize*(Yposition-1)+1:rowBlockSize*(Yposition-1)+lineWidth,...
                            colBlockSize*(recoveredFileSets(rfs)-1)+1:colBlockSize*(recoveredFileSets(rfs)-1)+lineWidth,...
                            1) = 1;                    
                    end
                end 
                % check if mother exists
                motherTrackNumber = lineagePairs(trackIndexInLineagePairs,2);
                
                if  motherTrackNumber>0
                   % data of mother's track
                   motherTrackIndexInLineagePairs = find(lineagePairs(:,1)==motherTrackNumber);
                   YpositionOfMother = TrackIndexPositionInYaxis(motherTrackIndexInLineagePairs);          
                   
                   % place the connection line into the image
                   lineageMatrix(rowBlockSize*(YpositionOfMother-1)+1+lineWidth:rowBlockSize*(Yposition-1),...
                       colBlockSize*(appearingFileSets(1)-1)+1:colBlockSize*(appearingFileSets(1)-1)+lineWidth,...
                       :) = 0;
                   
                end         
            end
            
            % print the fihure
            dpi = 144; fnSize=8;
            offScreenFig = figure('visible','off');
            image(lineageMatrix)
            set(gca,'YTick',[],'YTickLabel',[])
            % place the track number
            for tr = 1:nrOfTrackingLines
                hold on
                xc = StartingPositionOnImage(tr,1);
                yc = YtickOnImage(tr,1);
                if YtickLabelValsOnImage(tr,1)>99
                   fnSize = 6; 
                end
                text(xc,yc,['\fontsize{' num2str(fnSize) '}{\color{black}' num2str( YtickLabelValsOnImage(tr,1) ) '}' ],...
                    'HorizontalAlignment','right','VerticalAlignment','middle');
            end

            % produce and set the Xticks
            XtickLabelValsOnImage = (1:3:nrOfFileSets)'; 
            XtickOnImage = (XtickLabelValsOnImage-1)*colBlockSize + round((1+ pointWidth)/2);
            set(gca,'XTick',XtickOnImage,'XTickLabel',XtickLabelValsOnImage)
            %xlabel('file set number','FontSize',16)
            %ylabel('cell tracking index','FontSize',16)
            box off
            print(offScreenFig, '-depsc', sprintf('-r%d',dpi), lineageFileName);
            close(offScreenFig);
            
            
        end
        
        
        function varargout = generateLineageTree(trackingInfoMatrix, varargin)
            
            
            % 1 --- find for each tracking line (i.e main line), the set
            % of tracking lines are their mothers (i.e secondary lines)
            [daughertAndMotherPairs  fileSetLocationOfDivisionEvent]= CellXLineageGen.findDevisionEvents(trackingInfoMatrix);
            
            % 2 --- find for each tracking line (i.e main line), the set
            % of tracking lines are their mothers (i.e secondary lines)
            [mergingPairs fileSetLocationOfMergingEvent]= CellXLineageGen.findMergingEvents(trackingInfoMatrix);
            
            
            % 3 --- update the daughertAndMotherPairs with only the ones that:
            % a) appear in mergind and splitting
            % b) produce a daughter that is not 'short-lived'
            if ~isempty(daughertAndMotherPairs) &&  ~isempty(mergingPairs)
                tf = ~ismember(daughertAndMotherPairs,fliplr(mergingPairs),'rows');
                daughertAndMotherPairs =  daughertAndMotherPairs(tf,:);
            end
            daughertAndMotherPairs = CellXLineageGen.removeShortLivedDivisionEvents(daughertAndMotherPairs,trackingInfoMatrix);
            
            % 4 --- add pairs with no mothers
            noMotherPairs = CellXLineageGen.addPairsWithNoMothers(trackingInfoMatrix,daughertAndMotherPairs);
            
            % 5 --- gather all pairs
            lineagePairs =[noMotherPairs; daughertAndMotherPairs];
            
            % 6 --- place tracking lines into the Y axis
            TrackIndexPositionInYaxis  = CellXLineageGen.orderTrackingLines(lineagePairs);
            
            % 7 ---- generate figure
            if ~isempty(varargin)
                lineageFilename = varargin{1};
                CellXLineageGen.printLineageFigure(lineagePairs,TrackIndexPositionInYaxis,trackingInfoMatrix,lineageFilename);
            end
            
            if nargout==1
                varargout{1} = lineagePairs;
            end
            if nargout==2
                varargout{1} = lineagePairs;
                varargout{2} = TrackIndexPositionInYaxis;
            end
        end

        
        function stats = generateLineageComparisonTree(trackingInfoMatrixControl,trackingInfoMatrixSegm, varargin)
            
            [lineagePairsControl YpositionControl] = CellXLineageGen.generateLineageTree(trackingInfoMatrixControl);
            [lineagePairsSegm YpositionSegm] = CellXLineageGen.generateLineageTree(trackingInfoMatrixSegm);
            
            if ~isempty(varargin)
                lineageFilename = varargin{1};
                stats = CellXLineageGen.printLineageComparisonFigure(lineagePairsControl,YpositionControl,trackingInfoMatrixControl,...
                                                             lineagePairsSegm,YpositionSegm,trackingInfoMatrixSegm,...
                                                             lineageFilename);
            end
            
        end
        
        function stats = printLineageComparisonFigure(lineagePairs,TrackIndexPositionInYaxis,trackingInfoMatrix,...
                                                             lineagePairsSegm,YpositionSegm,trackingInfoMatrixSegm,...
                                                             lineageFileName)           
            % set the drawing Parameters
            lineWidth = 2;
            lineSpacing = 4;
            pointWidth = 2; 
            pointSpacing = 4;
            rowBlockSize = lineWidth+lineSpacing;
            colBlockSize =  pointWidth+pointSpacing;
            % take total number of tracking lines
            nrOfTrackingLines = numel(TrackIndexPositionInYaxis);
            
            % take total number of file Sets
            nrOfFileSets = max(trackingInfoMatrix(:,1));
            
            % generate initial image with the following characteristics
            imageLength =  lineWidth*nrOfTrackingLines + lineSpacing*(nrOfTrackingLines);
            imageWidth =   pointWidth*nrOfFileSets +  pointSpacing*(nrOfFileSets); 
            lineageMatrix = ones(imageLength,imageWidth,3);
             
            YtickLabelValsOnImage = zeros(nrOfTrackingLines,1);
            YtickOnImage = zeros(nrOfTrackingLines,1);
            StartingPositionOnImage = zeros(nrOfTrackingLines,1);
            
           totalDiv=0; correctDiv=0; missedDiv=0; wrongDiv=0; 
            
            for trackInds = 1:nrOfTrackingLines
                
                % data of this track
                currentTrackNumber = trackInds;
                
                trackIndexInLineagePairs =find(lineagePairs(:,1)==currentTrackNumber);
                Yposition = TrackIndexPositionInYaxis(trackIndexInLineagePairs);
                
                appearingFileSets = trackingInfoMatrix( trackingInfoMatrix(:,4)==trackInds,1);
                recoveredFileSets = appearingFileSets( find(trackingInfoMatrix( trackingInfoMatrix(:,4)==trackInds,5)) );
%                segmentationIndices = trackingInfoMatrix( trackingInfoMatrix(:,4)==trackInds,2);

                YtickLabelValsOnImage(trackInds,1) = currentTrackNumber;
                YtickOnImage(trackInds,1) = rowBlockSize*(Yposition-1)+round((1+lineWidth)/2);
                
                StartingPositionOnImage(trackInds,1) = colBlockSize*(appearingFileSets(1)-1);                
                
                if appearingFileSets(end)==nrOfFileSets
                    
                    % place the tracking line into the image
                    lineageMatrix(rowBlockSize*(Yposition-1)+1:rowBlockSize*(Yposition-1)+lineWidth,...
                        colBlockSize*(appearingFileSets(1)-1)+1:colBlockSize*(appearingFileSets(end)-1)+lineWidth+pointSpacing,...
                        :) = 0;                    
                else
                    
                    % place the tracking line into the image
                    lineageMatrix(rowBlockSize*(Yposition-1)+1:rowBlockSize*(Yposition-1)+lineWidth,...
                        colBlockSize*(appearingFileSets(1)-1)+1:colBlockSize*(appearingFileSets(end)-1)+lineWidth,...
                        :) = 0;
                    
                end       
                % mark any recovered cells of this track        
                if ~isempty(recoveredFileSets)
                    for rfs = 1:numel(recoveredFileSets)
                        % place the tracking line into the image
                        lineageMatrix(rowBlockSize*(Yposition-1)+1:rowBlockSize*(Yposition-1)+lineWidth,...
                            colBlockSize*(recoveredFileSets(rfs)-1)+1:colBlockSize*(recoveredFileSets(rfs)-1)+lineWidth,...
                            1) = 1;                    
                    end
                end 
                % check if mother exists
                motherTrackNumber = lineagePairs(trackIndexInLineagePairs,2);
                if  motherTrackNumber>0
                   totalDiv = totalDiv+1;
                   % data of mother's track
                   motherTrackIndexInLineagePairs = find(lineagePairs(:,1)==motherTrackNumber);
                   YpositionOfMother = TrackIndexPositionInYaxis(motherTrackIndexInLineagePairs);          
                   
                   % data of mother's track on Segmentation Result
                   trackIndexInLineagePairsSegm =find(lineagePairsSegm(:,1)==currentTrackNumber);
                   motherTrackNumberSegm = lineagePairsSegm(trackIndexInLineagePairsSegm,2);
                   
                   if motherTrackNumberSegm==motherTrackNumber
                      % place the normally connection line into the image
                        lineageMatrix(rowBlockSize*(YpositionOfMother-1)+1+lineWidth:rowBlockSize*(Yposition-1),...
                       colBlockSize*(appearingFileSets(1)-1)+1:colBlockSize*(appearingFileSets(1)-1)+lineWidth,...
                       :) = 0;
                       correctDiv = correctDiv+1;
                   elseif motherTrackNumberSegm~=motherTrackNumber && motherTrackNumberSegm==0
                       % place a blue connection line into the image
                       lineageMatrix(rowBlockSize*(YpositionOfMother-1)+1+lineWidth:rowBlockSize*(Yposition-1),...
                       colBlockSize*(appearingFileSets(1)-1)+1:colBlockSize*(appearingFileSets(1)-1)+lineWidth,...
                       [1,3]) = 0;
                       missedDiv = missedDiv+1;
                   elseif motherTrackNumberSegm~=motherTrackNumber && motherTrackNumberSegm~=0
                       % place a green connection line into the image
                       lineageMatrix(rowBlockSize*(YpositionOfMother-1)+1+lineWidth:rowBlockSize*(Yposition-1),...
                       colBlockSize*(appearingFileSets(1)-1)+1:colBlockSize*(appearingFileSets(1)-1)+lineWidth,...
                       [1,2]) = 0;
                       wrongDiv = wrongDiv+1;
                   else
                       fprintf('comparison case is not covered \n')
                   end
                end 
                
            end
            % save stats
            stats = [totalDiv,correctDiv,missedDiv, wrongDiv];
            % print the fihure
            dpi = 144; fnSize=8;
            offScreenFig = figure('visible','off');
            image(lineageMatrix)
            set(gca,'YTick',[],'YTickLabel',[])
            % place the track number
            for tr = 1:nrOfTrackingLines
                hold on
                xc = StartingPositionOnImage(tr,1);
                yc = YtickOnImage(tr,1);
                if YtickLabelValsOnImage(tr,1)>99
                   fnSize = 6; 
                end
                text(xc,yc,['\fontsize{' num2str(fnSize) '}{\color{black}' num2str( YtickLabelValsOnImage(tr,1) ) '}' ],...
                    'HorizontalAlignment','right','VerticalAlignment','middle');
            end

            % produce and set the Xticks
            XtickLabelValsOnImage = (1:3:nrOfFileSets)'; 
            XtickOnImage = (XtickLabelValsOnImage-1)*colBlockSize + round((1+ pointWidth)/2);
            set(gca,'XTick',XtickOnImage,'XTickLabel',XtickLabelValsOnImage)
            %xlabel('file set number','FontSize',16)
            %ylabel('cell tracking index','FontSize',16)
            box off
            print(offScreenFig, '-depsc', sprintf('-r%d',dpi), lineageFileName);
            close(offScreenFig);
            
            
        end
        
        
        
        
    end
    
end

