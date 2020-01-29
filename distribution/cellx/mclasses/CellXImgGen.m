classdef CellXImgGen
    %CELLXIMAGEGENERATOR Summary of this class goes here
    %   Detailed explanation goes here
    
    
    
    methods (Static)
        
        function rgb = generateSegmenterDebugImage(seeds, baseImg)
            rgb = CellXImgGen.generateHoughTransformImage(seeds, baseImg);
            r = rgb(:,:,1);
            g = rgb(:,:,2);
            b = rgb(:,:,3);
            seedCount = size(seeds,2);
            
            for i = 1:seedCount
                s = seeds(i);
                ind = s.perimeterPixelListLindx;
                
                if(s.skipped == 0)
                    r(ind) = 0.4+0.6*rand(1);
                    g(ind) = 0.4+0.6*rand(1);
                    b(ind) = 0.4+0.6*rand(1);
                elseif( s.skipped>2 )
                    dashedInd = ind(1:3:end);
                    rc = 0.4+0.6*rand(1);
                    gc = 0.4+0.6*rand(1);
                    bc = 0.4+0.6*rand(1);
                    r(dashedInd) = rc;
                    g(dashedInd) = gc;
                    b(dashedInd) = bc;
                end
                
            end
            rgb = cat(3, r, g, b);
            
            %            %imwrite(rgb, [filename '_raw.png'], 'png');
            %
            %            dim = size(this.image);
            %            h = figure;
            %            set(h, 'Position', [0 0 dim(1) dim(2)]);
            %            image(rgb);
            %            axis image;
            %            axis off;
            %
            %            for i = 1:seedCount
            %                s = seeds(i);
            %                text(s.houghCenterX, s.houghCenterY, ['\fontsize{8}{\color{black}' num2str(i) '}' ],...
            %                    'HorizontalAlignment','center', ...
            %                    'BackgroundColor',[1 1 1],'EdgeColor','black');
            %            end
            % %           print(h, '-dpng', '-r300', filename);
            % %           close(h);
            
        end
        
        
        function rgb = generateHoughTransformImage(seeds, baseImg)
            r = baseImg;
            g = baseImg;
            b = baseImg;
            seedCount = size(seeds,2);
            for i = 1:seedCount
                s = seeds(i);
                seedX = s.houghCenterX;
                seedY = s.houghCenterY;
                radius = s.houghRadius;
                if(s.skipped==0)
                    start = max(1,seedX-radius);
                    e = min(size(baseImg,2), seedX+radius);
                    width = [seedY-1 seedY seedY+1];
                    for pix=start:1:e
                        r(width, pix) = 1;
                        g(width, pix) = 0;
                        b(width, pix) = 0;
                    end
                    start = max(1,seedY-radius);
                    e = min(size(baseImg,1), seedY+radius);
                    width = [seedX-1 seedX seedX+1];
                    for pix=start:1:e
                        r(pix, width) = 1;
                        g(pix, width) = 0;
                        b(pix, width) = 0;
                    end
                else
                    start = max(1,seedX-radius);
                    e = min(size(baseImg,2), seedX+radius);
                    for pix=start:2:e
                        r(seedY, pix) = 1;
                        g(seedY, pix) = 0;
                        b(seedY, pix) = 0;
                    end
                    start = max(1,seedY-radius);
                    e = min(size(baseImg,1), seedY+radius);
                    for pix=start:2:e
                        r(pix, seedX) = 1;
                        g(pix, seedX) = 0;
                        b(pix, seedX) = 0;
                    end
                end
            end
            rgb = cat(3,r,g,b);
        end
        
        
        function rgb = generateControlImage(seeds, baseImg)
            
            r = baseImg;
            g = baseImg;
            b = baseImg;
            seedCount = size(seeds,2);
            for i = 1:seedCount
                s = seeds(i);
                seedX = s.houghCenterX;
                seedY = s.houghCenterY;
                radius = 1;
                
                cr = 0.4+0.6*rand(1);
                cg = 0.4+0.6*rand(1);
                cb = 0.4+0.6*rand(1);
                
                if(s.skipped==0)
                    start = max(1,seedX-radius);
                    e = min(size(baseImg,2), seedX+radius);
                    for pix=start:1:e
                        r(seedY, pix) = cr;
                        g(seedY, pix) = cg;
                        b(seedY, pix) = cb;
                    end
                    start = max(1,seedY-radius);
                    e = min(size(baseImg,1), seedY+radius);
                    for pix=start:1:e
                        r(pix, seedX) = cr;
                        g(pix, seedX) = cg;
                        b(pix, seedX) = cb;
                    end
                end
                ind = s.perimeterPixelListLindx;
                if(s.skipped == 0)
                    r(ind) = cr;
                    g(ind) = cg;
                    b(ind) = cb;
                end
            end
            rgb = cat(3, r, g, b);
        end
        
   
        
                
%         function createControlImageWithCellIndices(seeds, baseImg, varargin)
%             
%             r = baseImg;
%             g = baseImg;
%             b = baseImg;
%             seedCount = size(seeds,2);
%             for i = 1:seedCount
%                 s = seeds(i);
%                 
%                 cr = 0.4+0.6*rand(1);
%                 cg = 0.4+0.6*rand(1);
%                 cb = 0.4+0.6*rand(1);
%                 
%                 ind = s.perimeterPixelListLindx;
%                 if(s.skipped == 0)
%                     r(ind) = cr;
%                     g(ind) = cg;
%                     b(ind) = cb;
%                 end
%             end
%             rgb = cat(3, r, g, b);
%             
%             offScreenFig = figure('visible','off');
%             image(rgb);
%             [H,W,~] = size(r);
%             dpi = 1;
%             set(offScreenFig, 'paperposition', [0 0 W/dpi H/dpi]);
%             set(offScreenFig, 'papersize', [W/dpi H/dpi]);
%             
%             for i = 1:seedCount
%                 s = seeds(i);
%                 
%                 text(  'String', num2str(i), ...
%                     'FontSize', 14, ...
%                     'Color', [0 0.9 0.4], ...
%                     'Position', s.centroid, ...
%                     'HorizontalAlignment', 'center', ...
%                     'VerticalAlignment', 'middle');
%             end
%             
%             if( size(varargin,2)==0 )
%                 set(offScreenFig,'visible','on')
%             elseif(size(varargin,2)==1)
%                 print(offScreenFig, '-dpng', sprintf('-r%d',dpi), varargin{1});
%                 fprintf('Wrote %s\n', varargin{1});
%                 close(offScreenFig);
%             else
%                 close(offScreenFig);
%                 error('Too many variable arguments');
%             end
%             
%         end
        
        
        
         function createControlImageWithCellIndices(seeds, baseImg, varargin)
            
            r = baseImg;
            g = baseImg;
            b = baseImg;
            seedCount = size(seeds,2);
            for i = 1:seedCount
                s = seeds(i);
                
                cr = 0.4+0.6*rand(1);
                cg = 0.4+0.6*rand(1);
                cb = 0.4+0.6*rand(1);
                
                ind = s.membranePixelListLindx;
                if(s.skipped == 0)
                    r(ind) = cr;
                    g(ind) = cg;
                    b(ind) = cb;
                end
            end
            rgb = cat(3, r, g, b);
            dpi = 144;
            offScreenFig = figure('visible','off');
            image(rgb)
            axis image
            axis off
            hold on
            
%            imshow(rgb, 'Border', 'tight');
%            [H,W,~] = size(r);
%            dpi = 72;
%            set(offScreenFig, 'paperposition', [0 0 W/dpi H/dpi]);
%            set(offScreenFig, 'papersize', [W/dpi H/dpi]);

            fs = round(mean(vertcat(seeds.minorAxisLength)*0.3));  

           for i = 1:seedCount
               s = seeds(i);
%               fs = round(s.minorAxisLength*0.3);  
               text(s.centroid(1),s.centroid(2),['\fontsize{' num2str(fs) '}{\color{green}' num2str(i) '}' ],...
                       'HorizontalAlignment','center');
%                 text(  'String', num2str(i), ...
%                     'FontName', 'Courier', ...
%                     'FontSize', fs,...
%                     'Position', s.centroid, ...
%                     'HorizontalAlignment', 'center', ...
%                     'VerticalAlignment', 'middle');
           end
            
            if( size(varargin,2)==0 )
                set(offScreenFig,'visible','on');
            elseif(size(varargin,2)==1)
                print(offScreenFig, '-dpng', sprintf('-r%d',dpi), varargin{1});
                fprintf('Wrote %s\n', varargin{1});
                close(offScreenFig);
            else
                close(offScreenFig);
                error('Too many variable arguments');
            end
         end
        
        

         function color = getRandColor()       
            color = [rand(1) rand(1) rand(1)];
            percept = [0.2126; 0.7152; 0.0722];          
            v = var(color); 
            lum = color*percept;        
            k=0;          
            while( (lum<0.5 || v<0.04) &&k<10 )
                 color = [rand(1) rand(1) rand(1)];
                 v = var(color); 
                 lum = color*percept;
                 k = k+1;               
            end       
         end
        
        function rgb = generateIntensityExtractionControlImage(seeds, baseImg)
            
            %transparency of the membrane color
            alpha = 0.5;
            
            r = baseImg;
            g = baseImg;
            b = baseImg;
            seedCount = size(seeds,2);
            for i = 1:seedCount
                s = seeds(i);
                seedX = s.houghCenterX;
                seedY = s.houghCenterY;
                radius = 1;
                
                color = CellXImgGen.getRandColor();
                
                cr = color(1);
                cg = color(2);
                cb = color(3);
                
                if(s.skipped==0)
                    start = max(1,seedX-radius);
                    e = min(size(baseImg,2), seedX+radius);
                    for pix=start:1:e
                        r(seedY, pix) = cr;
                        g(seedY, pix) = cg;
                        b(seedY, pix) = cb;
                    end
                    start = max(1,seedY-radius);
                    e = min(size(baseImg,1), seedY+radius);
                    for pix=start:1:e
                        r(pix, seedX) = cr;
                        g(pix, seedX) = cg;
                        b(pix, seedX) = cb;
                    end
                end
                ind = s.membranePixelListLindx;
                if(s.skipped == 0)
                    r(ind) = min(1, alpha*r(ind) + (1-alpha)*cr);
                    g(ind) = min(1, alpha*g(ind) + (1-alpha)*cg);
                    b(ind) = min(1, alpha*b(ind) + (1-alpha)*cb);
                end
            end
            rgb = cat(3, r, g, b);
        end
        
        
        

        
        
        
        function generateLineageTree(trackingInfoMatrix, varargin)
            
            %--construct tracking lines (linmat)
            nrOfTrackingLines = max(trackingInfoMatrix(:,4));
%            nrOfFrames = max(trackingInfoMatrix(:,1));
            
%             linmat = zeros(nrOfTrackingLines,nrOfFrames);
%             for k=1:nrOfTrackingLines
%                 frameInds = trackingInfoMatrix((trackingInfoMatrix(:,6)==k),1);
%                 segInds = trackingInfoMatrix((trackingInfoMatrix(:,6)==k),2);
%                 linmat(k,frameInds) = segInds';
%             end
            
            %---find for each tracking line (i.e main line), the set
            % of tracking lines are: either a) their mothers
            % or b) they are absorbed by them --> (i.e secondary line)
            mothersPlusAbsorbedPairs =[];
            lineEventInds = find(trackingInfoMatrix(:,7)>0 | trackingInfoMatrix(:,9)>0) ;
            for lev = 1:numel(lineEventInds)
                eventPosition =  lineEventInds(lev);
                
                if trackingInfoMatrix(eventPosition,7) >0
                    
                    secondaryLineIndx = trackingInfoMatrix(eventPosition,7) ;
                else
                    secondaryLineIndx = trackingInfoMatrix(eventPosition,9) ;
                end
                
                mainLineInds =  trackingInfoMatrix(eventPosition,4) ;
                
                mothersPlusAbsorbedPairs = [ mothersPlusAbsorbedPairs ;...
                    [mainLineInds  secondaryLineIndx ] ];
            end
            % append the non-appearing main lines and make their
            % secondary line = 0
            allMainLines = (1:nrOfTrackingLines)';
            if ~isempty(mothersPlusAbsorbedPairs)
               nonAppearingMainLineIndx = ~ismember(allMainLines,mothersPlusAbsorbedPairs(:,1));
            else
               nonAppearingMainLineIndx = ~ismember(allMainLines,[]);
            end
            nonAppearingMainLines =  allMainLines(nonAppearingMainLineIndx);
            
            mothersPlusAbsorbedPairs =[[ nonAppearingMainLines zeros(numel(nonAppearingMainLines),1)];mothersPlusAbsorbedPairs];
             mothersPlusAbsorbedPairs = unique( mothersPlusAbsorbedPairs,'rows');
            
            TrackIndexPositionInYaxis = zeros(nrOfTrackingLines,1);
            rowAvailability = ones(nrOfTrackingLines,1);
            pairAvailability = ones(size(mothersPlusAbsorbedPairs,1),1);
            returningPairIndex =  zeros(size(mothersPlusAbsorbedPairs,1),1);
            
            curmom = 0;
            firstmom = 0;
            fistMomChanged = 0;
            cnt=0;
            loopcnt =0;
            while any( ~(TrackIndexPositionInYaxis>0) )
                loopcnt=loopcnt+1;
                if loopcnt > 1e6
                    fprintf('failed to produce lineage plot')
                    break;
                end
                % get the current 'secondary line' indices
                pairIndicesWithCurMom = (mothersPlusAbsorbedPairs(:,2)==curmom | mothersPlusAbsorbedPairs(:,1)==curmom) ...
                    &  pairAvailability ==1 ;
                % find its available children
                childrenIndex = unique(mothersPlusAbsorbedPairs( pairIndicesWithCurMom,1));
                
                childrenIndex =  setdiff(childrenIndex( rowAvailability(childrenIndex)==1), curmom)  ;
                
                if curmom==firstmom && isempty( childrenIndex)
                    
                    avMoms = find(rowAvailability);
                    firstmom = avMoms(1);
                    curmom = avMoms(1);
                    
                   pairIndicesWithCurMom = (mothersPlusAbsorbedPairs(:,2)==curmom | mothersPlusAbsorbedPairs(:,1)==curmom) ...
                    &  pairAvailability ==1 ;
                
                   % find its available children
                   childrenIndex = unique(mothersPlusAbsorbedPairs( pairIndicesWithCurMom,1));
                
                   childrenIndex =  setdiff(childrenIndex( rowAvailability(childrenIndex)==1), curmom) ;
                   
                   % write it in the series
                    cnt=cnt+1;
                    TrackIndexPositionInYaxis(curmom) = cnt;
                    % and make this row unavailable for search
                    rowAvailability(curmom) = 0;
                   
                   fistMomChanged =1;

                    
                end
                
                % check if duaghters exist
                if ~isempty(childrenIndex) % if daughter exists
                    % take the last one
                    oldmom = curmom;
                    curmom =  childrenIndex(end);
                    
                    if cnt~=0
                        oldProcessedIndex =  find(pairIndexToProcess);
                    end
                    pairIndexToProcess = ismember(mothersPlusAbsorbedPairs,[curmom oldmom],'rows');
                    
                    if cnt==0 || fistMomChanged==1
                        
                        returningPairIndex(pairIndexToProcess) = 0;
                        fistMomChanged = 0;
                    else
                        
                        returningPairIndex(pairIndexToProcess) =  oldProcessedIndex;
                        
                    end
                    % write it in the series
                    cnt=cnt+1;
                    TrackIndexPositionInYaxis(curmom) = cnt;
                    % and make this row unavailable for search
                    rowAvailability(curmom) = 0;
                    pairAvailability(pairIndexToProcess )=0;
                else
                    
                    pairIndexToProcess = ismember(mothersPlusAbsorbedPairs,[curmom oldmom],'rows');
                    if returningPairIndex(pairIndexToProcess)~=0
                        
                        curmom = mothersPlusAbsorbedPairs( returningPairIndex(pairIndexToProcess),1);
                        oldmom = mothersPlusAbsorbedPairs( returningPairIndex(pairIndexToProcess),2);
                    else
                        curmom = firstmom;
                    end
                    
                end

            end
            
            if ~isempty(varargin)
                pf2A6=figure('DockControls','off','Visible','off');
            else
               pf2A6=figure;
            end
            
            YtickLabelVals = zeros(1,numel(TrackIndexPositionInYaxis));
            for trackInds = 1:nrOfTrackingLines
                
                hold on
                appearingFileSets = trackingInfoMatrix( trackingInfoMatrix(:,4)==trackInds,1);
                recoveredFileSets = appearingFileSets(find(trackingInfoMatrix( trackingInfoMatrix(:,4)==trackInds,5)));
                segmentationIndices = trackingInfoMatrix( trackingInfoMatrix(:,4)==trackInds,2);
                
                colorVec1 = 0.2+0.5*rand(1,3);
                
                % the plotting line will be the one denoted
                % from the rackIndexPositionInYaxis
                Yposition =TrackIndexPositionInYaxis(trackInds);
                YtickLabelVals(Yposition) = trackInds;
                TrackIndicesCur = repmat(Yposition,1,length(appearingFileSets));
                TrackIndicesCurRecov = repmat(Yposition,1,length(recoveredFileSets));
                
                plot(appearingFileSets,TrackIndicesCur,'.-','Color',colorVec1)
                hold on
                plot(recoveredFileSets, TrackIndicesCurRecov,'ro')
               
                % place segmentation indices on tracking line
                xc = appearingFileSets;
                yc =  TrackIndicesCur';
                for nrf = 1:numel(xc)
                  text(xc(nrf),yc(nrf),['\fontsize{8}{\color{black}' num2str( segmentationIndices(nrf)) '}' ],...
                        'HorizontalAlignment','center','VerticalAlignment','baseline');
                end
                
                % set limits and ticks
                set(gca,'YTick',(1:length(TrackIndexPositionInYaxis)),'YTickLabel',YtickLabelVals)
                ylim([0 nrOfTrackingLines+1])
                xlabel('file set number','FontSize',16)
                ylabel('cell tracking index','FontSize',16)
                box off
                
                %              locF3 = [targetfolder '/'];
                %              fnc = [locF3 'TreeResult'];
                %              print(pf2A6,'-dpng','-r1200',fnc);
                %              saveas(pf2A6,fnc,'fig');
                
                
            end
            
            % draw the connections
             
            for nrcp=1:numel(lineEventInds)
                curLineEvent = trackingInfoMatrix( lineEventInds(nrcp),:);
                
                frontlineIndx = TrackIndexPositionInYaxis(curLineEvent(4));
                frontlineSetFileIndx = curLineEvent(1);
                if  curLineEvent(9)==0
                    backlineIndx = TrackIndexPositionInYaxis(curLineEvent(7));
                    
                else
                    backlineIndx = TrackIndexPositionInYaxis(curLineEvent(9));
                    
                end
                backlineSetFileIndx =  frontlineSetFileIndx -1 ;
                hold on
                plot([frontlineSetFileIndx backlineSetFileIndx],[ frontlineIndx  backlineIndx],'k')
            end
            
            if ~isempty(varargin)
                print(pf2A6,'-dpng', '-r72',varargin{1});
                close(pf2A6);
            end
            
        end
        
        function generateIntermediateTrackPlots(trackingLines,trackingLinesCosts, filename)
           
            pf2A7=figure('DockControls','off','Visible','off');
            nrTrackingLines = size(trackingLines,1);
            % loop through the tracking lines 
            for tl = 1:nrTrackingLines
                colorVec1 = 0.2+0.5*rand(1,3);
                accTrackIndices = trackingLines(tl,:)>0;
                trackingIndices = find(accTrackIndices);
                segmentationIndices = trackingLines(tl,accTrackIndices);
                yaxisValue = repmat(tl, [1,numel( trackingIndices)]);
                
               hold on
               plot(trackingIndices , yaxisValue,'.-','Color',colorVec1, 'MarkerSize',10)
               % add the segmentation indices 
               for nrf = 1:numel(trackingIndices)
                   hold on
                   text(trackingIndices(nrf), yaxisValue(nrf),['\fontsize{16}{\color{black}' num2str(  segmentationIndices(nrf)) '}' ],...
                        'HorizontalAlignment','center','VerticalAlignment','baseline');
               end

               % add the costs
               if numel(trackingIndices)>1
                   trackingIndicesCosts = trackingIndices(1:end-1) + 0.5;
                   yaxisValueCost = yaxisValue(1:end-1);
                   curTrackingLinesCosts = trackingLinesCosts(tl,trackingIndices(1:end-1));
                    curTrackingLinesCosts = round(100* curTrackingLinesCosts)/100;
                   for nrf = 1:numel( trackingIndicesCosts)
                       
                       hold on
                       text(trackingIndicesCosts(nrf), yaxisValueCost(nrf),['\fontsize{12}{\color{blue}' num2str(curTrackingLinesCosts(nrf)) '}' ],...
                           'HorizontalAlignment','center','VerticalAlignment','baseline');
                   end
               end
               
               
               
            end
            ylim([0  nrTrackingLines+1])
            xlabel('file set number','FontSize',16)
            ylabel('cell tracking index','FontSize',16)
            box off
            print(pf2A7,'-dpng', '-r72',filename);
            close(pf2A7);
            
            
        end
        
        
        
        
    end
    
end
