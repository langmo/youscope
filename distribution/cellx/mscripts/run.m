
clear; clc; close;


thisFile = mfilename('fullpath');
[folder, name] = fileparts(thisFile);
cd(folder);

addpath('../mclasses');
addpath('../mfunctions');
addpath('../mex');
addpath('../mex/maxflow');

% only for development
%makeMex;


% the first config file that was created with the gui :)
config = CellXConfiguration.fromXML('../data/img1config.xml');
config.setHoughTransformOnCLAHEImage(1);

config.check();

% config.setCalibrationMode(1);

fileSet = CellXFileSet(1, '../data/img1.tif');

cellXSegmenter = CellXSegmenter(config, fileSet);


%dbstop if error;
try
    cellXSegmenter.run();
catch exc
    CellXExceptionHandler.handleException(exc);
    return;
end

fprintf('done\n');

