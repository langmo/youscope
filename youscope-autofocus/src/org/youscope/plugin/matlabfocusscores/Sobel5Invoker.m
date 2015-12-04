%% Process image if available
if ~isempty(imageEvent)
	% Convert to Matlab image
	bytesPerPixel = imageEvent.getBytesPerPixel();
    maxValue = 2^imageEvent.getBitDepth()-1;
    imageType = ['uint', mat2str(8 * bytesPerPixel)];
    
    % create matrix out of image data
    image = reshape(typecast(imageEvent.getImageData(), imageType), imageEvent.getWidth(), imageEvent.getHeight());
    image = double(image)/maxValue;
			
	% Calculate score
	sobelMatrix = [1 2 1; 0 0 0; -1 -2 -1];
	sobelMatrix = conv2( [ 1 2 1 ]' * [1 2 1], sobelMatrix);
    H = conv2(image, sobelMatrix);
    V = conv2(image, sobelMatrix');
    score = sum(sum(H.^2 + V.^2)) / (size(H,1) * size(H, 2));
		
	% send score to YouScope
	focusSink.setScore(score);
else
	focusSink.setScore(-1);	
end