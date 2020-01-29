/* * * * * * * * * * * * * * * * * * * * * * * * * * Oscillating Input  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This script is written in JavaScript and let the user specify a device property which should oscillate,
 * its amplitude, mean value and the oscillation period.
 * No jobs have to be specified by the user, he or she only has to change the parameters in the section
 * "Configuration" according to his or her needs.
 * 
 * This example script was written by Moritz Lang
 * and is licensed under the GNU GPL.
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

//============================= Configuration ===============================================
// Change the following lines to configure the script to your needs.
// Currently all values are set to undefined, which will produce an error when executed.

// Name of the device and the property which should oscillate (Strings).
device = undefined;
property = undefined;
// Minimal and maximal value of the device's property during oscillation (doubles).
minValue = undefined;
maxValue = undefined;
// Period length of the oscillation in ms (double)
periodLength = undefined;
// Initial phase at t=0s in rad (Signal is a sine, not a cosine) (double).
initialPhase = undefined;

//=========================== Initial Checks ================================================
if(device==undefined || property == undefined || minValue == undefined
	|| maxValue == undefined || periodLength == undefined || initialPhase == undefined)
{
	throw "Please open the script and set all values under the paragraph \"Configuration\" to their respective values.";
}
//=========================== The script ====================================================
// The following lines typically don't have to be changed.

// Initialize script in the first run
if(evaluationNumber == 0)
{
	initialTime = new Date();
}
//Get time difference since start of measurement
actualTime = new Date();
dt = actualTime.getTime() - initialTime.getTime();

// Calculate value of device setting
val = (Math.sin(2*Math.PI * dt / periodLength +  initialPhase) * 0.5 + 0.5) * (maxValue - minValue) + minValue;

// Set device property's value
microscope.getDevice(device).getProperty(property).setValue(val)
