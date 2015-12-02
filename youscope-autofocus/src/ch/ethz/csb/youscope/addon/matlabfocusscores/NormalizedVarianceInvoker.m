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
	mu = mean(mean(image));
	score = sum(sum((image - mu).^2)) / (size(image,1) * size(image, 2) * mu);
		
	% send score to YouScope
	focusSink.setScore(score);
else
	focusSink.setScore(-1);	
end