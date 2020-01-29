function matlabImage = toMatlabImage(imageEvent)
    % Get image metadata
    bytesPerPixel = imageEvent.getBytesPerPixel();
    maxValue = 2^imageEvent.getBitDepth()-1;
    imageType = ['uint', mat2str(8 * bytesPerPixel)];
    
    % create matrix out of image data
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
    
    % Convert to uint8
    if strcmpi(class(matlabImage), 'uint8') 
        % Filetype already the right one...
    elseif strcmpi(class(matlabImage), 'uint16')
        %matlabImage = cast(matlabImage / (intmax('uint16') / uint16(intmax('uint8'))), 'uint8');   
        matlabImage = uint8(matlabImage / (maxValue / uint16(intmax('uint8'))));   
    elseif strcmpi(class(matlabImage), 'uint32')
        matlabImage = cast(matlabImage / (maxValue / uint32(intmax('uint8'))), 'uint8');   
    elseif(strcmpi(class(matlabImage), 'int8'))
        matlabImage = int16(matlabImage);
        matlabImage(matlabImage<0) = int16(intmax('uint8')) + 1 + matlabImage(matlabImage<0);
        matlabImage = cast(matlabImage, 'uint8');
    elseif(strcmpi(class(matlabImage), 'int16'))
        matlabImage = int32(matlabImage);
        matlabImage(matlabImage<0) = int32(intmax('uint16')) + 1 + matlabImage(matlabImage<0);
        matlabImage = cast(matlabImage / (int32(intmax('uint16')) / int32(intmax('uint8'))), 'uint16');
    else
        error('MicroscopeAnalysis:ImageTypeNotSupported', 'Only support 8 and 16 bit grayscale images.')
    end
end
