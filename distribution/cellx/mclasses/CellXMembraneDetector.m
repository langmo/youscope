classdef CellXMembraneDetector < handle
    %CELLXMEMBRANEDETECTOR Summary of this class goes here
    %   Detailed explanation goes here
    
    properties (SetAccess = private)
        config;
        srcImage;
        gradientPhase;
        convolutionGradientMagnitude;
        convolutionImage;
        seed;
        NLinkSparseAdj;
        TLinkMatrix;
        mask;
        seedIndex;
        
        nWidth;
        nHeight;
        nEdgeCount;
        nVertexCount;
        nEdgeProperties;
        nEdgeListVertexLindx;
        NLinkLindx;
    end
    
    methods
        
        function this = CellXMembraneDetector(config)
            this.config = config;
            this.initNLinkMatrix();
        end
        
        function run(this, seed, srcImage, gradientPhase, seedIdx)
  
            this.seedIndex = seedIdx;
            
            this.init(seed, srcImage, gradientPhase);
            
            if(seed.isInvalid()) 
                return;
            end
            
            this.initTLinkMatrix();
            
            this.updateNLinkMatrix();
            
            this.computeGraphCutAndCellCharacteristic(0);
            
             if(this.config.debugLevel==3)
                figure;
                imagesc(this.mask);
                title(sprintf('Maxflow labeling before refinement (seed %d)',this.seedIndex));
            end
            
            if( this.config.isNonConvexRegionDetectionEnabled )
                
                this.detectLowEnergyRegionCenters();
                
                if( numel(this.seed.lowEnergyMembraneRegionCentersLindx) )
                    this.updateRayConvolutionImage();
                    this.initConvolutionGradientMagnitudeImage();
                    this.updateNLinkMatrix();
                    this.computeGraphCutAndCellCharacteristic(1);
                end
            end
            
            if(this.config.debugLevel==3)
                %this.showPerimeterPixels();
                figure;              
                imagesc(this.convolutionImage);
                title(sprintf('Refined convolution image (seed %d)',this.seedIndex));
            end
            
            CellXMembraneDetector.computeCellVolume(this.seed, this.mask, this.config.membraneErosionShape);
        end
        
        function showMask(this)
            imagesc(this.mask);
        end
        
    end % of properties section
    
    
    %
    % PRIVATE METHODS
    %
    methods (Access=private)
        
        
        function initTLinkMatrix(this)
            %fprintf('  Building T-link matrix\n');
            [h w] = size(this.convolutionImage);
            pixelVertexCount = h*w;
            xc = this.seed.cropCenterX;
            yc = this.seed.cropCenterY;
            r = (this.config.seedMaskRadiusFraction * this.seed.houghRadius)^2;
            
            [X,Y]= meshgrid(1:h,1:w);
            cellMask = (X-xc).^2 + (Y-yc).^2 <= r;
            
            T = zeros(pixelVertexCount,2);
            T(cellMask==1, 1) = Inf;
            
            borderWidth = this.config.getMaxFlowGraphBorderVertexCount();
            borderMask = zeros(h, w);
            borderMask(1:h,[1:borderWidth,end-borderWidth:end])=1;
            borderMask([1:borderWidth,end-borderWidth:end],1:w)=1;
            T(borderMask==1, 2) = Inf;
            
            this.TLinkMatrix = sparse(T);
        end
        
        function initNLinkMatrix(this)
            %fprintf('  Building N-link matrix\n');
            this.nWidth = 1+2*this.config.maximumCellLength;
            this.nHeight = this.nWidth;
            this.nVertexCount = this.nWidth*this.nHeight;
            
            [this.nEdgeListVertexLindx this.nEdgeProperties] = ...
                initGridGraph( ...
                this.nHeight, ...
                this.nWidth, ...
                this.config.maxFlowGridConnectivity, ...
                1);
            
            this.nEdgeCount = size(this.nEdgeListVertexLindx,1);
            u=double(this.nEdgeListVertexLindx(:,1));
            v=double(this.nEdgeListVertexLindx(:,2));
            this.NLinkLindx = sub2ind([this.nVertexCount this.nVertexCount], u, v);
            c0 = ones(this.nEdgeCount, 1);
            this.NLinkSparseAdj = sparse(u,v,c0,this.nVertexCount,this.nVertexCount);
        end
        
        function init(this, seed, srcImage, gradientPhase)
            this.srcImage      = srcImage;
            this.seed          = seed;
            this.gradientPhase = gradientPhase;
            l =  this.config.maximumCellLength;
            [this.convolutionImage maxVals] = computeRayConvolution( ...
                l, ...
                l, ...
                srcImage, ...
                this.config.membraneIntensityProfile, ...
                this.config.membraneLocation);
                   
            % ensure that more than 50% of rays have maxima > 0.25*maxAutoCorrelation
            % otherwise invalidate the seed for further processing
            r = sum( maxVals>this.config.getRayConvolutionValueThreshold() )/length(maxVals);
                       
            if( r<this.config.requiredFractionOfGoodRays )
                this.seed.setSkipped(1, 'Too many bad rays');
                return;
            end
            this.initConvolutionGradientMagnitudeImage();
        end
        
        function initConvolutionGradientMagnitudeImage(this)
            [convGrdX convGrdY] = gradient(this.convolutionImage);
            hfilt = fspecial('average',[2 2]);
            smoothConvGrdX = imfilter(convGrdX,hfilt);
            smoothConvGrdY = imfilter(convGrdY,hfilt);
            this.convolutionGradientMagnitude = sqrt(smoothConvGrdX.^2 + smoothConvGrdY.^2);
        end
        
        % this takes ~40% of execution time (for an image with 100 cells)
        function updateNLinkMatrix(this)
            %fprintf('  Updating N-link matrix\n');
            
            [h w] = size(this.convolutionImage);
            
            if( h ~= this.nHeight || w ~= this.nWidth )
                error('Dimensions of image changed! (was %d,%d) (is %d,%d)', this.nHeight, this.nWidth, h,w);
            end
            
            grdMinThr  = 0.5 * max(this.convolutionGradientMagnitude(:));
            
            convolutionValues  = this.convolutionImage(this.nEdgeListVertexLindx(:,1));
            gradPhaseValues    = this.gradientPhase(this.nEdgeListVertexLindx(:,1) );
            
            validPhaseIdx      = ~isnan(gradPhaseValues);
            validConvGrdIdx    =  this.convolutionGradientMagnitude( this.nEdgeListVertexLindx(:,1) ) > grdMinThr;
            
            validIdx = validPhaseIdx & validConvGrdIdx;
            
            kf = ones(this.nEdgeCount, 1);
            kf(validIdx) = cos(abs( this.nEdgeProperties(validIdx,2) - gradPhaseValues(validIdx) ));
            
            gp = exp( -(10.^convolutionValues) );
            
            Nf = pi/this.config.maxFlowGridConnectivity;
            
            numerator = Nf*this.nEdgeProperties(:,1).^2.*gp;
            
            denumerator = ( this.nEdgeProperties(:,1).^2.*gp + (ones(this.nEdgeCount,1)-gp).*this.nEdgeProperties(:,1).^2.*kf.^2  ).^(3/2);
            
            V = numerator./denumerator;
            
            this.NLinkSparseAdj(this.NLinkLindx) = V;
            
        end
        
        
        function computeGraphCutAndCellCharacteristic(this, isRefinement)
            %fprintf('  Computing graph cut\n');
            [flow, labels] = maxflow(this.NLinkSparseAdj, this.TLinkMatrix);
            %fprintf('    Flow value %f \n', flow);
            
            labels = ~reshape(labels, size(this.convolutionImage));
            p = regionprops(labels, 'all');
            % if the labeling returns multiple regions, find the region
            % that contains the seed centroid and define the mask.
            numberOfComponents = size(p, 1);
            if( numberOfComponents>1 )
                %fprintf('    Multiple components detected\n');
                p = CellXMembraneDetector.getRegionPropsWithHoughCenter(this.seed, p);
                tmpMask = false(size(this.convolutionImage));
                tmpMask(p.PixelIdxList) = true;
            else
                tmpMask = labels;
            end
            
            if(isRefinement)
                union = this.mask | tmpMask;
                unionArea = length(find(union));
                currentArea = this.seed.getCellArea();
                dA = unionArea-currentArea;
                
                if( dA/currentArea < this.config.maximumExpansionFraction )
                    %fprintf('    Mask refinement\n');
                    this.mask = union;
                    p = regionprops(union, 'all');
                else
                    %fprintf('   No mask refinement\n');
                    return;
                end
                
            else
                this.mask = tmpMask;
            end
            
            
            this.seed.centroid          = p.Centroid;
            this.seed.boundingBox       = p.BoundingBox;
            this.seed.eccentricity      = p.Eccentricity;
            this.seed.orientation       = p.Orientation;
            this.seed.equivDiameter     = p.EquivDiameter;
      
            this.seed.majorAxisLength    = p.MajorAxisLength;
            this.seed.minorAxisLength    = p.MinorAxisLength;
            this.seed.perimeter          = p.Perimeter;
            this.seed.cellPixelListLindx = p.PixelList;
            
            % compute perimeter-, membrane-, and cytosol pixel lists
            
            % detect pixels on the border of the labeled region
            this.seed.perimeterPixelListLindx = find( bwperim(this.mask, 8) );
            this.seed.perimeterPixelConvolutionValues = this.convolutionImage(this.seed.perimeterPixelListLindx);
            cytosolMask  = imerode(this.mask, this.config.membraneErosionShape);
            this.seed.cytosolPixelListLindx = find(cytosolMask);
            
            membraneMask = xor(this.mask,cytosolMask);
            this.seed.membranePixelListLindx = find(membraneMask);
        end
        
        
        function detectLowEnergyRegionCenters(this)
            dim = size(this.convolutionImage);
                        
            % compute a clockwise trace along the perimeter pixels.
            % try at most 5 different pixels if the algo fails
            % for some weird shapes, the algo may fail
            exception = [];
            failed = [];
            for i=1:min(5, length(this.seed.perimeterPixelListLindx) )
                failed=0;
                [traceStartX traceStartY] = ind2sub(dim, this.seed.perimeterPixelListLindx(i));
                try
                   traceCoordinates = bwtraceboundary(this.mask, [traceStartX traceStartY], 'S');
                catch err
                    fprintf('   Trace boundary failed at pixel %d,%d. Trying elsewhere ...\n', traceStartX, traceStartY);
                    failed = 1;
                    exception = err;
                end
            
                if( failed==0 )
                    break;
                end
            
            end
            
            if( failed~=0 ) 
                %skip this seed
                fprintf('Error: Cannot compute boundary trace for seed (%s)\n', exception.message);
                %rethrow(exception);
                % invalidate seed? - lets keep it for now
                return;
            end
            
            
            traceCoordinatesLindx = sub2ind(dim, traceCoordinates(:,1), traceCoordinates(:,2));
            % get the valueas along the trace
            membranePixelConvValues =  this.convolutionImage(traceCoordinatesLindx);
            
            minConvValue = 0.4 * max(this.convolutionImage(:));
            logicIdx = membranePixelConvValues<minConvValue;
            if( ~any(logicIdx) )
                % stop here if the membrane pixels have no low value regions
                %fprintf('  No low energy region in membrane detected\n');
                return;
            end
            
            logicCenters = findRegionCentersInCircularArray( ...
                logicIdx, ...
                this.config.minimumLengthOfLowEnergyMembraneRegion, ...
                this.config.maximumSmoothingDistance);
            
            this.seed.lowEnergyMembraneRegionCentersLindx = traceCoordinatesLindx(logicCenters);
            
            if( this.config.debugLevel==3 )
                this.showWeakMembraneRegionCenters(logicIdx,traceCoordinatesLindx);
            end
        end
        
        function updateRayConvolutionImage(this)
            dim = size(this.srcImage);
            [row, col] = ind2sub(dim, this.seed.lowEnergyMembraneRegionCentersLindx);
            n = numel(this.seed.lowEnergyMembraneRegionCentersLindx);
            %fprintf('  Computing %d ray convolution refinement(s)\n',n);
            for i=1:n
                %fprintf('Weak membrane center at %d,%d\n', col(i), row(i));
                convImg = computeRayConvolution( ...
                    col(i), ...
                    row(i), ...
                    this.srcImage, ...
                    this.config.membraneIntensityProfile, ...
                    this.config.membraneLocation);              
                this.convolutionImage = max( this.convolutionImage, convImg);
            end
            
        end
  
        function showWeakMembraneRegionCenters(this, logicIdx,traceCoordinatesLindx)
            [logicCenters, smoothed] = findRegionCentersInCircularArray( ...
                logicIdx, ...
                this.config.minimumLengthOfLowEnergyMembraneRegion, ...
                this.config.maximumSmoothingDistance);
            debugImage = this.convolutionImage;
            debugImage(traceCoordinatesLindx(smoothed))=2;
            debugImage(traceCoordinatesLindx(logicCenters))=3;
            
            figure;              
            imagesc(debugImage);
            title(sprintf('Smoothed weak membrane pixels and centers (seed %d)', this.seedIndex));
        end
        
        function showPerimeterPixels(this)
            debugImage = this.convolutionImage;
            debugImagegb = this.convolutionImage;
            debugImage(this.seed.perimeterPixelListLindx)=1;
            debugImagegb(this.seed.perimeterPixelListLindx)=0;
            rgb = cat(3, debugImage, debugImagegb, debugImagegb);
            imtool(rgb);
        end
        
        
    end % of methods section
    
    methods (Static)
    
     function computeCellVolume(seed, mask, membraneErosionShape)
            halfCellLayerCount = floor( seed.minorAxisLength/2 );
            erosionMask        = mask;
            erosionShape       = strel('disk', 1);
            membraneShape      = membraneErosionShape;
            totalVoxelCount    = 0;
            cytosolVoxelCount  = 0;
            cytosolMask        = imerode(erosionMask, membraneShape);
            for i=1:halfCellLayerCount
                if( mod(i,2) )
                    erosionMask = imerode(erosionMask, erosionShape);
                    cytosolMask = imerode(cytosolMask, erosionShape);
                end
                totalVoxelCount = totalVoxelCount + length(find(erosionMask==1));
                cytosolVoxelCount = cytosolVoxelCount + length(find(cytosolMask==1));
            end
            seed.cellVolume = 2*totalVoxelCount + seed.getCellArea();
            seed.cytosolVolume = 2*cytosolVoxelCount + seed.getCytosolArea();          
     end
    
        %
        % this is also used by the CellXSeedIntersectionResolver
        %
     function outProps = getRegionPropsWithHoughCenter(seed, inProps)       
         centroid = [seed.cropCenterX seed.cropCenterY];
         compIdx=0;
         numberOfComponents = size(inProps, 1);
         for i=1:numberOfComponents
             if( any(ismember(inProps(i).PixelList, centroid, 'rows')) )
                 compIdx=i;
                 break;
             end
         end
         if(compIdx~=0)
             outProps = inProps(compIdx);
         else
             error('None of the labeled regions contains the seed centroid');
         end
     end
     
       
    end
    
    
end % of class definition

