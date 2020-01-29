function imageEvent = takeImage(youscopeServer, channelGroup, channel, exposure, cameraID)
% takeImage(youscopeServer, channelGroup, channel, exposure, camera)
% Takes an image with the standard camera in the given channel with the given exposure. Returns the
% YouScope image event object, from which the image data can be extracted.
% See toMatlabImage for more details.
%
% takeImage(..., camera)
% Specifies the device ID of the camer with which the image should be taken.

%% Take image
if ~exist('cameraID', 'var') || isempty(cameraID)
    camera = youscopeServer.getMicroscope().getCameraDevice();
else
    camera = youscopeServer.getMicroscope().getCameraDevice(cameraID);
end
imageEvent = camera.makeImage(channelGroup, channel, exposure);

end
