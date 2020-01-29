function matlabImage = toMatlabImage(imageEvent)
    % Get image metadata
    bytesPerPixel = imageEvent.getBytesPerPixel();
    maxValue = 2^imageEvent.getBitDepth()-1;
    imageType = ['uint', mat2str(8 * bytesPerPixel)];
    
    % create matrix out of image data
    % Transpose since Matlab uses other image representation than YouScope
    matlabImage = reshape(typecast(imageEvent.getImageData(), imageType), imageEvent.getWidth(), imageEvent.getHeight())';
    
    % Flip dimensions if necessary
    if imageEvent.isTransposeY()
        matlabImage = flipud(matlabImage);
    end
    if imageEvent.isTransposeX()
        matlabImage = fliplr(matlabImage);
    end
    if imageEvent.isSwitchXY()
        matlabImage = matlabImage';
    end
    
    % Convert to double in [0,1]
    if strcmpi(class(matlabImage), 'uint8') 
        matlabImage = double(matlabImage) / maxValue;
    elseif strcmpi(class(matlabImage), 'uint16')
        matlabImage = double(matlabImage) / maxValue;
    elseif strcmpi(class(matlabImage), 'uint32')
        matlabImage = double(matlabImage) / maxValue;
    elseif(strcmpi(class(matlabImage), 'int8'))
        matlabImage = int16(matlabImage);
        matlabImage(matlabImage<0) = int16(intmax('uint8')) + 1 + matlabImage(matlabImage<0);
        matlabImage = double(matlabImage) / maxValue;
    elseif(strcmpi(class(matlabImage), 'int16'))
        matlabImage = int32(matlabImage);
        matlabImage(matlabImage<0) = int32(intmax('uint16')) + 1 + matlabImage(matlabImage<0);
        matlabImage = double(matlabImage) / maxValue;
    else
        error('MicroscopeAnalysis:ImageTypeNotSupported', 'Only support 8 and 16 bit grayscale images.')
    end
end
