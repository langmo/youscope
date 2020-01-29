function imageEvent = getExampleImageEvent(fileName)
orgImage = imread(fileName, 'tif')';
switch class(orgImage)
    case 'uint8'
        numBytes = 1;
    case 'uint16'
        numBytes = 2;
    otherwise
        error('CSB:ImageFormat', 'Only 1-2 bytes/pixel grayscale images allowed.');
end

imageEvent = ch.ethz.csb.youscope.shared.ImageEvent(orgImage(:), size(orgImage, 1), size(orgImage, 2), numBytes);
end