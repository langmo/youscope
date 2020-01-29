classdef CellXImageIO
    %CELLXIMAGEIO CellX methods for image IO

    methods (Static)
               
        %
        % loads an image and converts it to gray scale if it has 3 channels
        % gray conversion   : p = (R+G+B)/3
        % variable argument : crop region
        %                     [x, y, width-1, height-1]
        %                     (this is how imcrop defines crop regions)
        %
        function img = loadToGrayScaleImage(file, varargin)
            
            crop = [];
            if( size(varargin,2)>1 )
                error 'Variable argument list too long';
            elseif ( size(varargin,2)==1 )
                crop = varargin{1};
            end
            
            img = double(imread( file ));
            dimensionCount = length(size(img));
            % check if image is truecolor(RGB) image
            if( dimensionCount==3 && size(img,3)==3 )
                fprintf('Converting ''%s'' to grayscale\n', file);
                img = ( ...
                    img(:,:,1) + ...
                    img(:,:,2) + ...
                    img(:,:,3) ...
                    )/3;
            elseif( dimensionCount~=2 )
                error('Image import error (unknown image format)\n');
            end
            
            % reduce to crop region if defined
            if( ~isempty(crop) )
                img = imcrop(img, crop);
            end
            
        end
        
        %
        % normalize to 0-1 range
        %
        function ret = normalize(img)
            minimumBitValue = min(img(:));
            maximumBitValue = max(img(:));
            variableBitRange = maximumBitValue - minimumBitValue;
            ret = ( img - minimumBitValue ) / variableBitRange;
        end
        
        
                  
        function ret = extendImageWithNoise(img, borderWidth, background)     
            width = size(img,2);
            height = size(img,1);     
            ret = background(1) * ones(height+2*borderWidth,width+2*borderWidth) + background(2)*randn(height+2*borderWidth,width+2*borderWidth);
            ret(borderWidth+1:height+borderWidth, borderWidth+1:width+borderWidth) = img;
        end
        
        
        %
        % fluo image correction with denoised flat field image
        %
        function ret = getFluoFlatFieldCorrection(flatFieldImage, fluoImage, denoiseParams)
            [ffimage_smoothed, ~] = wiener2(flatFieldImage, denoiseParams);
            ffimage_smoothed_mean =  mean(ffimage_smoothed(:));
            ret =  ffimage_smoothed_mean*fluoImage./ffimage_smoothed;
            
        end
    end
    
end

