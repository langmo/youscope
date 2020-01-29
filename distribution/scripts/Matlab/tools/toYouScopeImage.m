function imageEvent = toYouScopeImage(matlabImage)
    
    % Matlab saves image data in the other way round than the rest of
    % the world...
    matlabImage = matlabImage';
    
    if length(size(matlabImage)) == 2
        % Grayscale image
        colorImage = false;
        bytesPerPixel = 1;
        bitDepth = 8;
        
        
    else
        % Color image -> Create RGBA image
        colorImage = true;
        bytesPerPixel = 4;
        bitDepth = 8;
        
        % Set alpha to maximum (no transparency).
        alpha = ones(size(matlabImage, 1), size(matlabImage, 2)) * 255;
        
        bitShifts = [24, 16, 8, 0];
        
        packed      =               bitshift(uint32(alpha),                bitShifts(1));
        packed      = bitor(packed, bitshift(uint32(matlabImage(:, :, 1)), bitShifts(2)));
        packed      = bitor(packed, bitshift(uint32(matlabImage(:, :, 2)), bitShifts(3)));
        matlabImage = bitor(packed, bitshift(uint32(matlabImage(:, :, 3)), bitShifts(4)));
    end
    
    % Create YouScope image event.
    imageEvent = ch.ethz.csb.youscope.shared.ImageEvent(matlabImage(:), size(matlabImage, 1), size(matlabImage, 2), bytesPerPixel, bitDepth);
    if colorImage
        imageEvent.setBands(3);
    end
end
