classdef CellXSeedIntersectionResolver < handle
    %CELLXSEEDINTERSECTIONRESOLVER Detects overlapping seed areas and
    %either merges, resolves, or invalidates these seeds.
    
    
    properties
        config;
        seeds;
        %image size
        dim;
        % 1D cell array with the common pixels of the i-th pair
        % (linear indices of the pixels)
        overlaps;
        % 1D array with the index of seed u of the i-th pair (u,v)
        overlapsU;
        % 1D array with the index of seed v of the i-th pair (u,v)
        overlapsV;
        
        % a binary mask to compute the new region properties of seeds
        updateMask;
        
        clusters;
        
        mergedSeeds = CellXSeed.empty(0,0);
        
        resolvedSeeds = CellXSeed.empty(0,0);
        
    end
    
    methods
        
        function obj = CellXSeedIntersectionResolver(config, seeds, dim)
            obj.config = config;
            obj.seeds  = seeds;
            obj.dim    = dim;
        end
        
        
        function run(this)
            
            fprintf('   Resolving intersections ...\n');
            
            this.initOverlap(1);
            n = numel(this.overlapsU);
            if(n>0)
                fprintf('   Merging cells (%d pair(s))\n', n);
                this.cluster();
                fprintf('    -> in %d cluster(s)\n', numel(this.clusters));
                this.merge();
                validator = CellXSeedValidator(this.config, this.mergedSeeds, this.dim);
                validator.checkMinorAxixsLength();
                this.seeds = [this.seeds this.mergedSeeds];
            end
            
            
            this.initOverlap(2);
            n = numel(this.overlapsU);
            if(n>0)
                fprintf('   Splitting cells (%d pair(s)) \n', n);
                this.cluster();
                fprintf('    -> in %d cluster(s)\n', numel(this.clusters));
                this.resolve();
            end
            
            
            this.initOverlap(3);
            n = numel(this.overlapsU);
            if(n>0)
                fprintf('   Resolving intersecting cell pairs (%d) \n', n);
                this.deleteSmallerCell();
            end
            
            validator = CellXSeedValidator(this.config, this.resolvedSeeds, this.dim);
            validator.checkMinorAxixsLength();
            this.seeds = [this.seeds this.resolvedSeeds];
            
            this.initOverlap(4);
            n = numel(this.overlapsU);
            if(n>0)
                
                fprintf('   Removing nonresolvable cells (%d pair(s))\n', n);
                this.cluster();
                fprintf('    -> in %d cluster(s)\n', numel(this.clusters));
                n = numel(this.clusters);
                for i=1:n
                    fprintf('    Processing cluster %d\n',i);
                    cn = numel(this.clusters{i});
                    for k = 1:cn
                        fprintf('      Removing seed %d\n', this.clusters{i}(k));
                        this.seeds(this.clusters{i}(k)).setSkipped(7, 'Intersecting area between thresholds');
                    end
                end
            end
            
            fprintf('   Finished intersection resolution\n');
        end
        
        % mode=1 : Find overlapping cells that should be merge
        %          i.e. at least one of them overlaps>overlap-thr
        % mode=2 : Find overlapping cells that should be split
        %          both of them are below the resolve-thr
        % mode=3 : Find overlapping cells where the smaller should be
        %          deleted
        %          i.e. exactly one of them is below the resolve-thr
        % else   : Find overlapping cells in between the split and merge
        %          thresholds
        %
        function initOverlap(this, mode)
            validSeedIdx = find([this.seeds.skipped]==0);
            n = numel(validSeedIdx);
            
            safeDist = 2*this.config.maximumCellLength;
            
            this.overlaps  = cell(n,1);
            this.overlapsU = zeros(n,1);
            this.overlapsV = zeros(n,1);
            c=1;
            for j = 1:n
                vj = validSeedIdx(j);
                for k = j:n
                    if( j~=k )
                        vk = validSeedIdx(k);
                        sj = this.seeds(vj);
                        sk = this.seeds(vk);
                        
                        dist = sqrt( ...
                            (sj.houghCenterX-sk.houghCenterX)^2 + ...
                            (sj.houghCenterY-sk.houghCenterY)^2);
                        
                        if(dist<safeDist)
                            common = intersect(sj.cellPixelListLindx, sk.cellPixelListLindx);
                            nc = numel(common);
                            if(nc>0)
                                if(mode==1)
                                    ru = nc/numel(sj.cellPixelListLindx);
                                    rv = nc/numel(sk.cellPixelListLindx);
                                    maxRatio = max(ru,rv);
                                    if( maxRatio>this.config.overlapMergeThreshold )
                                        this.overlaps{c}  = common;
                                        this.overlapsU(c) = vj;
                                        this.overlapsV(c) = vk;
                                        c = c+1;
                                    end
                                elseif(mode==2)
                                    ru = nc/numel(sj.cellPixelListLindx);
                                    rv = nc/numel(sk.cellPixelListLindx);
                                    
                                    if( ru < this.config.overlapResolveThreshold && ...
                                            rv < this.config.overlapResolveThreshold )
                                        this.overlaps{c}  = common;
                                        this.overlapsU(c) = vj;
                                        this.overlapsV(c) = vk;
                                        c = c+1;
                                    end
                                elseif(mode==3)
                                    ru = nc/numel(sj.cellPixelListLindx);
                                    rv = nc/numel(sk.cellPixelListLindx);
                                    minRatio = min(ru,rv);
                                    maxRatio = max(ru,rv);
                                    if( minRatio < this.config.overlapResolveThreshold && ...
                                            maxRatio > this.config.overlapResolveThreshold )
                                        this.overlaps{c}  = common;
                                        % save the surviving (larger) cell in the U array
                                        if(ru<rv)
                                            this.overlapsU(c) = vj;
                                            this.overlapsV(c) = vk;
                                        else
                                            this.overlapsU(c) = vk;
                                            this.overlapsV(c) = vj;
                                        end
                                        c = c+1;
                                    end
                                else
                                    this.overlaps{c}  = common;
                                    this.overlapsU(c) = vj;
                                    this.overlapsV(c) = vk;
                                    c = c+1;
                                end
                            end
                        end
                    end
                end
            end
            c = c-1;
            this.overlaps  = this.overlaps(1:c);
            this.overlapsU = this.overlapsU(1:c);
            this.overlapsV = this.overlapsV(1:c);
        end
        
        
        %
        % find clusters of connected seeds
        %
        function cluster(this)
            labels = unique([this.overlapsU this.overlapsV]);
            this.clusters = edgeJoin(labels, this.overlapsU, this.overlapsV);
        end
        
        
        
        function bb = computeCommonBoundingBox(this, seedIndices)
            if(numel(seedIndices)==0)
                error('Internal error: seed indices cannot be empty');
            end
            bb = this.seeds(seedIndices(1)).boundingBox;
            bb(3) = bb(1) + bb(3);
            bb(4) = bb(2) + bb(4);
            cn = numel(seedIndices);
            for k = 2:cn
                tmp = this.seeds(seedIndices(k)).boundingBox;
                tmp(3) = tmp(1) + tmp(3);
                tmp(4) = tmp(2) + tmp(4);
                bb(1:2) = min(bb(1:2), tmp(1:2));
                bb(3:4) = max(bb(3:4), tmp(3:4));
            end
            % round to pixel values
            bb = ceil(bb);
            % extend by 1 pixel if possible
            bb(1:2) = bb(1:2)-[1 1];
            bb(1:2) = max(bb(1:2),[1 1]);
            bb(3:4) = bb(3:4)+[1 1];
            bb(3:4) = min(bb(3:4), [this.dim(2) this.dim(1)]);
            bb(3) = bb(3)-bb(1);
            bb(4) = bb(4)-bb(2);
        end
        
        
        function merge(this)
            n = numel(this.clusters);
            this.mergedSeeds = CellXSeed.empty(n,0);
            for i=1:n
                fprintf('    Processing cluster %d\n',i);
                % compute the common bounding box
                cbb = this.computeCommonBoundingBox(this.clusters{i});
                % merge the seed areas
                this.updateMask = false(cbb(4), cbb(3));
                cn = numel(this.clusters{i});
                for k = 1:cn
                    fprintf('      Merging seed %d\n', this.clusters{i}(k));
                    s = this.seeds(this.clusters{i}(k));
                    s.setSkipped(5, sprintf('Merged into seed %d', numel(this.seeds)+i));
                    [iIdx jIdx] = ind2sub(this.dim, s.cellPixelListLindx);
                    iIdx = iIdx-cbb(2)+1;
                    jIdx = jIdx-cbb(1)+1;
                    
                    maIdx = sub2ind([cbb(4), cbb(3)], iIdx, jIdx);
                    this.updateMask(maIdx) = true;
                end
                % compute common center and radius
                hx = round(sum([this.seeds(this.clusters{i}).houghCenterX])/cn);
                hy = round(sum([this.seeds(this.clusters{i}).houghCenterY])/cn);
                hr = round(sum([this.seeds(this.clusters{i}).houghRadius])/cn);
                
                % create a new seed object for the merge result
                ms = CellXSeedIntersectionResolver.createNewSeed( ...
                    hx ,...
                    hy, ...
                    hr,...
                    cbb,...
                    this.updateMask, ...
                    this.dim, ...
                    this.config);
                
                %addprop(ms, 'sourceSeeds'); % Add a dynamic property
                %ms.sourceSeeds = this.clusters{i};
                this.mergedSeeds(i) = ms;
                
            end
        end
        
        
        function resolve(this)
            
            this.resolvedSeeds = CellXSeed.empty(2*numel(this.overlapsU), 0);
            resolvedSeedCounter = 1;
            
            n = numel(this.clusters);
            seed2cluster = zeros(numel(this.seeds),1);
            for i=1:n
                seed2cluster(this.clusters{i}) = i;
            end
            
            edgeCount = numel(this.overlapsU);
            % collect all pairwise intersections that belong to one cluster
            % i.e. the edges that defined the cluster
            % consider only one (U) end of an edge
            intersectionClusters = cell(n,1);
            for i=1:edgeCount
                cIdx = seed2cluster(this.overlapsU(i));
                intersectionClusters{cIdx}= [intersectionClusters{cIdx} i];
            end
            
            for i=1:n
                fprintf('    Processing cluster %d\n',i);
                edgeList = intersectionClusters{i};
                edgeCount = numel(edgeList);
                % collect the intersection pixels of all pairs in cluster i
                rPixelsGlob = [];
                for k = 1:edgeCount
                    rPixelsGlob = vertcat(rPixelsGlob, this.overlaps{edgeList(k)});
                end
                if( edgeCount>2 )
                    % some pixels might occur in several pair intersections
                    % to resolve, it is sufficient to consider each pixel once
                    rPixelsGlob = unique(rPixelsGlob);
                end
                
                cbb = this.computeCommonBoundingBox(this.clusters{i});
                
                rPixelsLoc = this.translateToBoundingBox(rPixelsGlob, cbb);
                
                pullPixelMaskLidx = this.computePullPixels(cbb, rPixelsLoc);
                
                [freePixels pullPixels] = this.computeSeedPullPixels( ...
                    this.clusters{i}, cbb, pullPixelMaskLidx, rPixelsGlob);
                
                
                sc = numel(pullPixels);
                distances = zeros(sc,1);
                % find nearest pullPixel for each rPixel
                pixelCount = numel(rPixelsLoc.lidx);
                assignment = zeros(pixelCount,1);
                for k = 1:pixelCount
                    px = rPixelsLoc.cols(k);
                    py = rPixelsLoc.rows(k);
                    for j=1:sc
                        distances(j) = min( (pullPixels{j}.cols-px).^2 + ...
                            (pullPixels{j}.rows-py).^2 );
                    end
                    x = find(distances==0);
                    if(numel(x)~=0)
                        fprintf('Found zero distance - this should never happen!\n');
                    end
                    
                    [minDistance winner] = min(distances);
                    x = find(distances==minDistance);
                    if(numel(x)==1)
                        assignment(k) = winner;
                        %else
                        %    fprintf('Exactly in between\n')
                        %    distances
                    end
                    
                end
                
                sc = numel(this.clusters{i});
                
                for k = 1:sc
                    fprintf('      Resolving seed %d\n', this.clusters{i}(k));
                    this.updateMask = false(cbb(4) , cbb(3));
                    this.updateMask(freePixels{k}.lidx) = true;
                    this.updateMask( rPixelsLoc.lidx(assignment==k) ) = true;
                    originalSeed = this.seeds( this.clusters{i}(k) );
                    originalSeed.setSkipped(6, ...
                        sprintf('Resolved seed in %d', numel(this.seeds)+resolvedSeedCounter));
                    
                    newSeed = CellXSeedIntersectionResolver.createNewSeed( ...
                        originalSeed.houghCenterX, ...
                        originalSeed.houghCenterY, ...
                        originalSeed.houghRadius, ...
                        cbb, ...
                        this.updateMask, ...
                        this.dim, ...
                        this.config);
                    
                    this.resolvedSeeds(resolvedSeedCounter) = newSeed;
                    resolvedSeedCounter = resolvedSeedCounter+1;
                end
                
                if( this.config.debugLevel>=4 )
                    debugMask =  zeros(cbb(4) , cbb(3));
                    for k = 1:sc
                        debugMask(freePixels{k}.lidx) = k/sc;
                        debugMask( rPixelsLoc.lidx(assignment==k) ) = (k-0.5*k/sc)/sc;
                    end
                    figure;
                    imagesc(debugMask);
                end
            end
        end
       
        
        
        %
        % processes the seed pairs, where 
        % seed A is > this.config.overlapResolveThreshold and
        % seed B is < this.config.overlapResolveThreshold     
        % 
        % step1: the pair is resolved, i.e. the pixels of the intersection
        %        area are assigned to the seed that is closest 
        %        (wrt the non intersecting pixels)
        %
        % step2: a new seed is created for the updated area of the larger
        %        seed
        %
        % step3: the two overlapping seeds are marked to be
        %        replaced by the new seed 
        % 
        function deleteSmallerCell(this)
            edgeCount = numel(this.overlapsU);
            for i=1:edgeCount
                % the two indices of the seeds,
                % the first one is the larger cell
                tseeds = [this.overlapsU(i) this.overlapsV(i)];
                cbb = this.computeCommonBoundingBox(tseeds);
                rPixelsLoc = this.translateToBoundingBox(this.overlaps{i}, cbb);
                pullPixelMaskLidx = this.computePullPixels(cbb, rPixelsLoc);
                [freePixels pullPixels] = this.computeSeedPullPixels( ...
                    tseeds, cbb, pullPixelMaskLidx, this.overlaps{i});
                sc = numel(pullPixels);
                distances = zeros(sc,1);
                % find nearest pullPixel for each rPixel
                pixelCount = numel(rPixelsLoc.lidx);
                assignment = zeros(pixelCount,1);
                for k = 1:pixelCount
                    px = rPixelsLoc.cols(k);
                    py = rPixelsLoc.rows(k);
                    for j=1:sc
                        distances(j) = min( (pullPixels{j}.cols-px).^2 + ...
                            (pullPixels{j}.rows-py).^2 );
                    end
                    x = find(distances==0);
                    if(numel(x)~=0)
                        fprintf('Found zero distance - this should never happen!\n');
                    end
                    [minDistance winner] = min(distances);
                    x = find(distances==minDistance);
                    if(numel(x)==1)
                        assignment(k) = winner;
                    end
                    
                end
                
                
                fprintf('   Resolving seed %d\n', tseeds(1));
                this.updateMask = false(cbb(4) , cbb(3));
                % write pixels of the larger cell in mask
                this.updateMask(freePixels{1}.lidx) = true;
                this.updateMask( rPixelsLoc.lidx(assignment==1) ) = true;
                originalSeed = this.seeds( tseeds(1) );
                originalSeed.setSkipped(6, ...
                    sprintf('Resolved seed in %d (large cell of a small/large pair (%d,%d))', numel(this.seeds)+numel(this.resolvedSeeds), tseeds(1), tseeds(2) )); 
                newSeed = CellXSeedIntersectionResolver.createNewSeed( ...
                        originalSeed.houghCenterX, ...
                        originalSeed.houghCenterY, ...
                        originalSeed.houghRadius, ...
                        cbb, ...
                        this.updateMask, ...
                        this.dim, ...
                        this.config);
                    
                this.resolvedSeeds(end+1) = newSeed;
                
                fprintf('   Removing seed %d\n', tseeds(2));
                originalSeed = this.seeds( tseeds(2) );
                originalSeed.setSkipped(6, ...
                     sprintf('Resolved seed in %d (small cell of a small/large pair (%d,%d))', numel(this.seeds)+numel(this.resolvedSeeds), tseeds(1), tseeds(2) ));

                if( this.config.debugLevel>=4 )
                    debugMask =  zeros(cbb(4) , cbb(3));
                    debugMask(freePixels{1}.lidx) = 0.1;
                    debugMask( rPixelsLoc.lidx(assignment==1) ) = 0.3;
                    debugMask(freePixels{2}.lidx) = 0.7;
                    debugMask( rPixelsLoc.lidx(assignment==2) ) = 0.9;
                    
                    figure;
                    imagesc(debugMask);
                end
            end
            
            
        end
        
      
        
        % creates a mask with a border of 2px around
        % the intersection area(s)
        % returns the linear indices of the pixels on the border
        function m = computePullPixels(this, cbb, rPixelsLoc)
            m = false(cbb(4), cbb(3));
            m(rPixelsLoc.lidx) = true;
            % create a pixel border of width 2 around the intersection area
            md = imdilate(m, strel('disk',2));
            % subtract the intersection area to get the pull pixel mask
            m = find(md-m);
        end
        
        
        %
        % freePixels: a cell array with the linear indices of pixels
        %             of a seed that is not in the intersection area,
        %             transformed to the common bounding box
        %
        % pullPixels: a cell array with the x,y coordinates of pull pixels
        %             i.e. the pixels of a seed that overlap with the
        %             pullPixelMask
        %             pullPixels{i} = [2 x n], where n is the number of
        %             pixels of the i-th seed.
        function [freePixels pullPixels] = computeSeedPullPixels(this, ...
                cluster, bb, pullPixelMaskLidx, rPixelsGlob)
            
            n = numel(cluster);
            freePixels = cell(n,1);
            pullPixels = cell(n,1);
            
            for i=1:n
                s = this.seeds(cluster(i));
                fp = setdiff(s.cellPixelListLindx, rPixelsGlob);
                fp = this.translateToBoundingBox(fp, bb);
                freePixels{i} = fp;
                ip = intersect(fp.lidx, pullPixelMaskLidx);
                [r c] = ind2sub([bb(4) bb(3)], ip);
                pixStr.rows = r;
                pixStr.cols = c;
                pixStr.lidx = ip;
                pullPixels{i} = pixStr;
            end
            
        end
        
        
        function ret = translateToBoundingBox(this, pixels, bb)
            [rows cols] = ind2sub(this.dim, pixels);
            ret.rows = rows-bb(2)+1;
            ret.cols = cols-bb(1)+1;
            ret.lidx = sub2ind([bb(4) bb(3)], ret.rows, ret.cols);
        end
        
        
    end
    
    
    methods(Static)
        
         % creates a new seed in image coordinates
        function s = createNewSeed(x, y, r, bb, updateMask, inputImageDimension, config)
            % compute the new region properties
            p = regionprops(updateMask, 'all');
            %mergeArea(round(p.Centroid(1)), round(p.Centroid(2))) = false;
            %figure;
            %imagesc(mergeArea);
            s = CellXSeed( x, y, r );     
            % transform the original hough center to the new bb
            s.setCenterOnCropImage(x-bb(1), y-bb(2), bb);
            
            numberOfComponents = size(p, 1);
            if( numberOfComponents>1 )
                p = CellXMembraneDetector.getRegionPropsWithHoughCenter(s, p);
                tmpMask = false(size(updateMask));
                tmpMask(p.PixelIdxList) = true;
            else
                tmpMask = updateMask;
            end
                 
            % create a new seed with the merged info

            s.centroid          = p.Centroid;
            s.boundingBox       = p.BoundingBox;
            s.eccentricity      = p.Eccentricity;
            s.orientation       = p.Orientation;
            s.equivDiameter     = p.EquivDiameter;
            s.majorAxisLength   = p.MajorAxisLength;
            s.minorAxisLength   = p.MinorAxisLength;
            s.perimeter         = p.Perimeter;
            s.cellPixelListLindx  = p.PixelList;
            s.perimeterPixelListLindx = find( bwperim(tmpMask, 8) );
            
            %tmp = find( bwperim(this.updateMask, 8) );
            
            cytosolMask  = imerode(tmpMask, config.membraneErosionShape);
            s.cytosolPixelListLindx = find(cytosolMask);
            membraneMask = xor(updateMask, cytosolMask);
            s.membranePixelListLindx = find(membraneMask);
            s.transformBoundingBoxToImageCoordinates(0);
            s.transformToImageCoordinates(inputImageDimension, 0);
            
            CellXMembraneDetector.computeCellVolume(s, tmpMask, config.membraneErosionShape);
            %[r c] = ind2sub([bb(4) bb(3)], tmp);
            %r = r + bb(2) -1;
            %c = c + bb(1) -1;
            %s.perimeterPixelListLindx = sub2ind(this.dim,r,c);
            
        end 
        
        
    end
    
    
    
end

