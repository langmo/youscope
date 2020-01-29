% This function tests the cell detection algorithm
imageEvent = getExampleImageEvent('eColiExample.tif');

[cellPositions, detectionImage] = cellDetection(imageEvent, 50, 2000, 0.01, 0.5);
colorImage = cellDetectionToImage(imageEvent, cellPositions, 50, 0.5, true);
displayImage(colorImage);

displayImage(label2rgb(detectionImage));