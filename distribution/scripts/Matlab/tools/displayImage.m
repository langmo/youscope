function displayImage(imageObject)
% displayImage(imageEvent)
% Displays the given microscope image, represented by an image event
% object.
%
% displayImage(imageData)
% Displays the given standard Matlab image

if strcmp(class(imageObject), 'ch.ethz.csb.youscope.shared.ImageEvent')
    imageObject = toMatlabImage(imageObject);
end
figure('Color', [0,0,0]);
if length(size(imageObject)) == 2
    % Grayscale image
    image(imageObject);
else
    % Color image
    image(double(imageObject)/255);
end
% Set colormap for grayscale images
if length(size(imageObject)) == 2
    colormap(gray(256)); 
end
axis off;
axis equal;

end