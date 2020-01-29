classdef CellXSeed < handle
    %CELLXSEED Stores all the information about a seed/cell (e.g. coordinates, morphological properties, ...)
    %          Fluorescence information if any is stored separately in the fluoFeatureList.
    
    properties
        
        houghCenterX;
        houghCenterY;
        cropCenterX;
        cropCenterY;
        houghRadius;
        cropRegion;
        
        centroid;
        boundingBox;
        eccentricity;
        orientation;
        equivDiameter;
        majorAxisLength;
        minorAxisLength;
        perimeter;
        cellVolume;
        cytosolVolume;
        
        cellPixelListLindx;
        
        % the nonzero-pixels on the border to the unlabeled region
        perimeterPixelListLindx;
        
        % the permiterPixelList extended inwards with the membrane width
        membranePixelListLindx;
        
        % the labeled region without the membranePixelList
        cytosolPixelListLindx;
        
        lowEnergyMembraneRegionCentersLindx;
        perimeterPixelConvolutionValues;
        
        % fluo features
        fluoFeatureList;
        
        % source seeds - was a dynamic property before
        sourceSeeds;
    end
    
    properties (SetAccess=private, GetAccess=public)
        % this seed is valid if and only if skipped==0
        % if skipped!=0, skipReason shoul contain more information why the seed was excluded. 
        skipped = 0;
        skipReason = '';
    end
    
    methods
        
        %
        % CONSTRUCTOR
        %
        function this = CellXSeed(x, y, r)            
            if( nargin==3) % Constructor setting (x,y) coordinate and radius of seed
                this.houghCenterX = x;
                this.houghCenterY = y;
                this.houghRadius  = r;
            elseif( nargin==1 ) % Copy constructor ~ copies all values of the fields of structure x to the properties with the same names of this seed
                if( isstruct(x) || isobject(x) )
                    fns = fieldnames(x);
                    n = numel(fns);               
                    for k=1:n        
                        fn = char(fns(k));
                        try
                            this.(fn) = x.(fn);
                        catch                        %#ok<CTCH>
                            warning('CellX:CopyConstruct', ['Cannot set property ''',fn,'''']);
                        end
                    end                    
                else
                    error('Expected a structure or object for copy constructor (single arg)');
                end
            end
        end
        
        % add fluorescence features
        function addFluoFeatures(this, fluoFeatureObj)
            this.fluoFeatureList = [this.fluoFeatureList fluoFeatureObj];
        end
        
        
        function ret = isInvalid(this)
            ret = (this.skipped~=0);
        end
        
        % setters
        
        function setCenterOnCropImage(this, x, y, cropRegion)
            this.cropCenterX = x;
            this.cropCenterY = y;
            this.cropRegion  = cropRegion;
        end

        function setSkipped(this, val, reason)
            if(val==0)
                error('Skip value cannot be zero!');
            end
            this.skipped = val;
            this.skipReason = reason;
        end
        
        % getters
        
        function ret = getCellArea(this)
            ret = numel(this.cellPixelListLindx);
        end
        
        function ret = getMembraneArea(this)
            ret = numel(this.membranePixelListLindx);
        end
        
        function ret = getCytosolArea(this)
            ret = numel(this.cytosolPixelListLindx);
        end
        
        
        function ret = getNucleusRadius(this,config)
            %---- compute the nuclear area (in pixels)
            nuclearVolumeSize =config.nuclearVolumeFraction*this.cellVolume;
            % assuming that nucleus is a sphere (V = (4/3)*pi*r^3)
            ret = (3*nuclearVolumeSize/(4*pi))^(1/3);
        end
        
        
        function ret = getNucleusArea(this,config)
            nuclearMask = this.getNuclearMask(config);
            ret = sum(nuclearMask(:));
        end
        
        function ret = getNuclearMask(this,config)
            
            %---- compute the nuclear area (in pixels)
            nuclearVolumeSize =config.nuclearVolumeFraction*this.cellVolume;
            % assuming that nucleus is a sphere (V = (4/3)*pi*r^3)
            nuclearRadius = (3*nuclearVolumeSize/(4*pi))^(1/3);
            % grid size
            nx = 2*ceil(nuclearRadius)+1; x = 1:nx;
            ny = 2*ceil(nuclearRadius)+1; y = 1:ny;
            % make the grid
            [X,Y] = meshgrid(x,y);
            % define nuclear center
            centercoord = (nx+1)/2;
            c=[centercoord centercoord];
            
            % create nuclear circle:left side of cell in x axis
            ret = (X-c(1)).^2 + (Y-c(2)).^2  <= nuclearRadius^2 ;
            
        end
        
        % transform functions
        
        function transformBoundingBoxToImageCoordinates(this, extension)
            this.boundingBox(1) = this.boundingBox(1) + ...
                this.cropRegion(1) - 1 - extension;
            this.boundingBox(2) = this.boundingBox(2) + ...
                this.cropRegion(2) - 1 - extension;
        end
        
        function transformToImageCoordinates(this, imageSize, extension)
            
            this.centroid = this.centroid + ...
                this.cropRegion(1:2) - 1 - extension;
            
            if(numel(this.cellPixelListLindx)>0)
                iInd = this.cropRegion(2) + this.cellPixelListLindx(:,2) - extension - 1;
                jInd = this.cropRegion(1) + this.cellPixelListLindx(:,1) - extension - 1;
                this.cellPixelListLindx = sub2ind(imageSize, iInd, jInd);
            end
            
            if(numel(this.perimeterPixelListLindx)>0)
                this.perimeterPixelListLindx = this.getInputImagePixelCoordinates( ...
                    this.perimeterPixelListLindx, imageSize, extension);
            end
            
            if(numel(this.membranePixelListLindx)>0)
                this.membranePixelListLindx = this.getInputImagePixelCoordinates( ...
                    this.membranePixelListLindx, imageSize, extension);
            end
            if(numel(this.cytosolPixelListLindx)>0)
                this.cytosolPixelListLindx = this.getInputImagePixelCoordinates( ...
                    this.cytosolPixelListLindx, imageSize, extension);
            end
        end
        
    end
    
    methods (Access = private)
        
        function lind = getInputImagePixelCoordinates(this, linIdxList, inputImageSize, extension)
            cropSize = [this.cropRegion(4) this.cropRegion(3)];
            [i j] = ind2sub(cropSize, linIdxList);
            i = i + this.cropRegion(2) - 1 - extension;
            j = j + this.cropRegion(1) - 1 - extension;
            lind = sub2ind(inputImageSize, i, j);
        end
    end
    
end

