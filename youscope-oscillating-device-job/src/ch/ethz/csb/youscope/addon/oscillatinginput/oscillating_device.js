/* * * * * * * * * * * * * * * * * * * * * * * * * * Oscillating Input  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* This script implements the oscillating functionality of the oscillating device job.
* It expect that an empty device job is initialized as job0,
* and that the variables "device", "property", "minValue", "maxValue", "periodLength" and "initialPhase" are set to
* the respective values.
*
* This  script was written by Moritz Lang
* and is licensed under the GNU GPL.
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

// Initialization of the script, done when it is executed for the first time.
if(evaluationNumber == 0)
{
	initialTime = new Date(); 
}

//Get time difference since start of measurement
actualTime = new Date();
dt = actualTime.getTime() - initialTime.getTime();

// Calculate value of device setting
val = (Math.sin(2*Math.PI * dt / periodLength +  initialPhase) * 0.5 + 0.5) * (maxValue - minValue) + minValue;

// Set device setting to this value.
jobs[0].clearDeviceSettings();
jobs[0].addDeviceSetting(device, property, val);

// Start device setting job
jobs[0].runJob(microscope);