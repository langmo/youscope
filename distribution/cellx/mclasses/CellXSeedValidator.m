classdef CellXSeedValidator
    %CELLXSEEDVALIDATOR Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
        config;
        seeds;
        % the dimension of the raw input image (without extension)
        % required to detect the seeds on the boundary
        dim;
    end
    
    methods
        
        function this = CellXSeedValidator(config, seeds, imageDimension)
            this.config = config;
            this.seeds  = seeds;
            this.dim    = imageDimension;
        end
        
        
        function run(this)
            fprintf('   Validating cells ...\n');
            % checks and transforms the seed coordinates to image
            % coordinates
            fprintf('    Detecting cells on image border ...\n');
            this.checkImageBoundaryContact();
            fprintf('    Checking minor axis growth ...\n');
            this.checkMinorAxixsLength();
            fprintf('    Checking membrane convolution values ...\n');
            this.checkMembranePixels();
            fprintf('   Finished cell validation ...\n');
        end
        
        
        %
        % check membrane pixel convolution values
        %
        function checkMembranePixels(this)
                    
             thr =this.config.membraneConvolutionThresholdFraction*this.config.membraneReferenceCorrelationValue;
            
            fprintf('      Membrane pixel convolution value threshold: %f\n', thr);
            c=0;
            for i=1:numel(this.seeds)
                s =this.seeds(i);
                
                if(s.isInvalid())
                    continue;
                end
                
                fractionOfGoodPixels = sum(s.perimeterPixelConvolutionValues>thr)/...
                    length(s.perimeterPixelConvolutionValues);
                
                fprintf('      Seed %d, fraction of good membrane pixels: %f\n', i, fractionOfGoodPixels);
                
                if(fractionOfGoodPixels<this.config.requiredFractionOfAcceptedMembranePixels)
                    %fprintf('Skipped %d\n',i);
                    s.setSkipped(4, 'Not enough high value membrane pixels');
                    c = c+1;
                end
            end
            if(c~=0)
                fprintf('   -> Removed %d seed(s) because of weak membrane pixels\n', c);
            end
        end
        
        
        
        %
        % compare the minor axis length and hough transform radius
        %
        function checkMinorAxixsLength(this)
            c=0;
            for i=1:numel(this.seeds)
                s =this.seeds(i);
                if(s.isInvalid())
                    continue;
                end
                
                ratio = s.minorAxisLength/(2*s.houghRadius);
                
                if( ratio > this.config.maximumMinorAxisLengthHoughRadiusRatio )
                    s.setSkipped(3, sprintf('Minor axis length/(2*Hough radius)=%f > %f', ratio, this.config.maximumMinorAxisLengthHoughRadiusRatio));
                    c = c+1;
                end           
            end
            if(c~=0)
                fprintf('   -> Removed %d seed(s) because of minor axis growth\n', c);
            end
        end
        
        
        % remove seeds that hit the image boundary
        function checkImageBoundaryContact(this)
            c=0;
            for i=1:numel(this.seeds)
                s =this.seeds(i);
                if(s.isInvalid())
                    continue;
                end
                
                s.transformBoundingBoxToImageCoordinates(this.config.maximumCellLength);
                
                bb = s.boundingBox;
                
                d1x = bb(1);
                d1y = bb(2);
                d2x = this.dim(2) - (bb(1)+bb(3));
                d2y = this.dim(1) - (bb(2)+bb(4));
                
                
                minD = this.config.requiredDistanceToImageBoundary;
                isValid =  d1x >= minD & d1x > 0 & ...
                    d1y >= minD & d1y > 0 & ...
                    d2x >= minD & d2x > 0 & ...
                    d2y >= minD & d2y > 0;
                
                if ~isValid
                    s.setSkipped(2, 'Seed hits image boundary');
                    c = c+1;
                else
                    s.transformToImageCoordinates(this.dim, this.config.maximumCellLength);
                end          
            end
            
            if(c~=0)
                fprintf('   -> Removed %d seed(s) on the image boundary\n', c);
            end
        end
        

    end
    
end

