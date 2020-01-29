classdef CellXSegmenter < handle
    %CellImageSegmenter Encapsulates methods and data of image segmentation
    
    properties
        
        % CellXConfiguration instance with the calibration/parameters
        config;
        
        % CellXFileSet instance with the input/output filenames
        fileSet;
        
        % CellXMembraneDetector instance with maxflow graph for membrane
        % detection
        membraneDetector;
        
        % images
        image;
        imageCLAHE;
        imageExtended;
        imageGradientPhase;
        
        % crop images
        currentCropImage;
        currentCropGradientPhase;
        currentConvolutionImage;
        currentSeed;
        
        % array with CellXSeed handle objects
        seeds;
    end
    
    
    %
    % PUBLIC METHODS
    %
    methods
             
        % constructor 
        function obj = CellXSegmenter(config, fileSet)
            obj.config = config;
            obj.fileSet = fileSet;
            if( config.calibrationMode~=1 )
                obj.membraneDetector = CellXMembraneDetector(config);
            end
        end
               
        function run(this)      
            tic;
            fprintf(' Segmenting cells ...\n');
            
            this.loadImages();
            
            try
                this.computeHoughTransform();
            catch exc
                rethrow(exc);
            end
            
            if( this.config.calibrationMode==1 )
                return;
            end 
            
            this.detectMembranes();           
            validator = CellXSeedValidator(this.config, this.seeds, size(this.image));       
            validator.run();
            ir = CellXSeedIntersectionResolver(this.config, this.seeds, size(this.image));
            ir.run();            
            this.seeds = ir.seeds;                     
            if( this.config.debugLevel > 0 )
                img = CellXImgGen.generateSegmenterDebugImage(this.seeds, this.image);
                imtool(img);
            end  
            fprintf(' Finished cell segmentation\n');
            t=toc;
            fprintf(' Elapsed time: %4.2fs\n', t);
        end
               
        function vSeeds = getDetectedCells(this)
            vIdx = [this.seeds.skipped]==0;
            vSeeds = this.seeds(vIdx);
        end
          
        
        function ret = getInputImageDimension(this)
            ret = size(this.image);
        end
    end
    
    
    
    
    %
    % PRIVATE METHODS
    %
    methods (Access = protected)
        
        function loadImages(this)
           this.image = CellXImageIO.loadToGrayScaleImage( ...
                    this.fileSet.oofImage, ...
                    this.config.cropRegionBoundary);

            this.image = CellXImageIO.normalize( this.image );
            %this.image = wiener2( this.image , this.config.wiener2params );
            
            if( this.config.requiresCLAHE)
                
                
                dim = size(this.image);
                
                tiles = round(dim/this.config.claheBlockSize);
                tiles = max(tiles, [2 2]);

                this.imageCLAHE = adapthisteq(  this.image, ...
                    'NumTiles', tiles, ...
                    'Range', 'original', ...
                    'ClipLimit', this.config.claheClipLimit);
            end
            
            if( this.config.calibrationMode==1 )
                return;
            end
            
            if( this.config.isGraphCutOnCLAHE )
                background = this.computeBackground(this.imageCLAHE);
                this.imageExtended = CellXImageIO.extendImageWithNoise(this.imageCLAHE, this.config.maximumCellLength, background);
            else
                
                background = this.computeBackground(this.image);
                this.imageExtended = CellXImageIO.extendImageWithNoise(this.image, this.config.maximumCellLength, background);
                
            end
            [grdX grdY] = gradient(this.imageExtended);
            this.imageGradientPhase = atan(abs(grdX./grdY));
            
        end
    
        function computeHoughTransform(this)
            tic;
            fprintf('   Circular Hough transform\n');
            try
            
            if(this.config.isHoughTransformOnCLAHE)
                this.seeds = CircularHough_Grd(this.imageCLAHE, ...
                    this.config.seedRadiusLimit, ...
                    this.config.houghTransformGradientThreshold, ...
                    this.config.getAccumulationSmoothingWindowSize(), ...
                    1, ...
                    this.config.getHoughTransformAccumulationArraySmoothingFilter, ...
                    this.config.seedSensitivity,...
                    this.config.maximumNumberOfCentroids);
            else
                this.seeds = CircularHough_Grd(this.image, ...
                    this.config.seedRadiusLimit, ...
                    this.config.houghTransformGradientThreshold, ...
                    this.config.getAccumulationSmoothingWindowSize(), ...
                    1, ...
                    this.config.getHoughTransformAccumulationArraySmoothingFilter, ...
                    this.config.seedSensitivity, ...
                    this.config.maximumNumberOfCentroids);
            end
            
            catch exc
                rethrow(exc);
            end
            
            fprintf('    Found %d seed(s)\n', numel(this.seeds));
            t=toc;
            fprintf('   Elapsed time: %4.2fs\n', t);
        end
        
        function initCurrentCrops(this)
            l =  this.config.maximumCellLength;
            cropRegion = [this.currentSeed.houghCenterX this.currentSeed.houghCenterY 2*l 2*l];
            this.currentCropImage = imcrop(this.imageExtended, cropRegion);
            this.currentCropGradientPhase = imcrop(this.imageGradientPhase, cropRegion);
            cropRegion(3:4) = cropRegion(3:4)+1;
            this.currentSeed.setCenterOnCropImage(l,l,cropRegion);
        end 
        
        function detectMembranes(this)         
            seedCount = numel(this.seeds);
            for i = 1:seedCount
                fprintf('   Processing seed %d\n', i);
                this.currentSeed = this.seeds(i);
                this.initCurrentCrops();
                this.membraneDetector.run(...
                    this.currentSeed, ...
                    this.currentCropImage, ...
                    this.currentCropGradientPhase, ...
                    i);
                if( this.config.debugLevel>1 )
                    disp(this.seeds(i));
                end
            end
        end
        
        function ret = computeBackground(this, image)
            tic;
            fprintf('   Computing segmentation image background \n');
            range = this.config.intensityClassesCount;
            if( length(range)>1 )
                range = range(1):range(2);
            end
            mm = CellXMixtureModel(range);
            mm.computeMixtureModel(image);
            [~,idx] = max(mm.getMixingProportions());
            ret = [mm.getMeanValues(idx) mm.getStdValues(idx)];
            t = toc;
            fprintf('   Elapsed time: %4.2fs\n', t);
        end
        
    end
    
    
    
end

