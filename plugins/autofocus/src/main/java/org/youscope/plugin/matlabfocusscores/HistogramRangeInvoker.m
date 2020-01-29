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
	sorted = sort(image(:));
    dist = round(length(sorted) * 0.001);
    if(dist < 1)
        dist = 1;
    end
    score = sorted(end+1-dist)-sorted(dist);
		
	% send score to YouScope
	focusSink.setScore(score);
else
	focusSink.setScore(-1);	
end