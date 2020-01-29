%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% This script is written in MatlabScript and let the user specify a device property which should oscillate,
% its amplitude, mean value and the oscillation period.
% No jobs have to be specified by the user, he or she only has to change the parameters in the section
% "Configuration" according to his or her needs.
% 
% This example script was written by Moritz Lang
% and is licensed under the GNU GPL.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%============================= Configuration ===============================================
% Set the following lines to the values suiting your purpose.
% The currently set values (all parameters set to empty arrays) will produce an error.

% Name of the device and the property which should oscillate (String)
device = [];
property = [];
% Minimal and maximal value of the device's property during oscillation (doubles).
minValue = [];
maxValue = [];
% Period length of the oscillation in s (doubles)
periodLength = []; 
% Initial phase at t=0s in rad (Signal is a sine, not a cosine) (double)
initialPhase = [];

%=========================== Initial Checks ================================================
if isempty(device) || isempty(property) || isempty(minValue) ...
	|| isempty(maxValue) || isempty(periodLength) || isempty(initialPhase)
	error('CSB:ParametersUndefined', 'Some of the parameters are undefined. Edit the script file and set all parameters in the configuration paragraph!')
end

%=========================== The script ====================================================
% The following lines typically don't have to be changed.
% Initialize script in the first run
if evaluationNumber == 0
	initialTime = clock();
end
%Get time difference since start of measurement
actualTime = clock();
dt = etime(actualTime, initialTime);
% Calculate value of device setting
val = (sin(2*pi * dt / periodLength +  initialPhase) * 0.5 + 0.5) * (maxValue - minValue) + minValue;
% Set device setting to this value.
microscope.getDevice(device).getProperty(property).setValue(val);
