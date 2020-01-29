/* * * * * * * * * * * * * * * * * * * * * * * * * * Oscillating Input  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This JavaScript script uses a cell detection algorithm to detect cells in an image. Then, it centers the cells with the stage, zooms into the cells (changes the magnification), adjusts the focus, takes several images, and finally zooms out.
 * 
 * This script was written by Moritz Lang
 * and is licensed under the GNU GPL.
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


//=========================== Initial Checks ================================================
if(jobs.length < 2)
{
	throw "The Cell Hunter scripting job needs two sub-jobs (currently " + java.lang.Integer.toString(jobs.length) + " jobs): one cell detection job and one job defining the imaging protocol for detected cells.";
}

//============================= Configuration ===============================================
// Microscope setup
numPixelsX = 1024; // without binning
numPixelsY = 1344; // without binning
rawPixelSize = 6.45; // um, without binning
objectiveDeviceID = "TINosePiece";

// Setup of the different zoom levels (0=scanning, 1=in depth picture)
binning0 = 2;
binning1 = 1;
magnification0 = 10;
magnification1 = 40;
objectiveID0 = "1-PlanApo  10x na 0.45";
objectiveID1 = "2-PlanApoVC  20x na 0.75";


// Detection configuration:
// A cell is assumed positive if fluorescence(2)/fluorescence(1) < threshold
threshold = 0.3;

// don't know
iterationMagnificationString = "";


// transform variables to needed
rawPixelSize0 = rawPixelSize * binning0;
rawPixelSize1 = rawPixelSize * binning1;
numPixelsX0 = numPixelsX / binning0;
numPixelsX1 = numPixelsX / binning1;
numPixelsY0 = numPixelsY / binning0;
numPixelsY1 = numPixelsY / binning1;


//=========================== The script ====================================================
// The following lines typically don't have to be changed.

// Initialize script in the first run
if(evaluationNumber == 0)
{
	var javaClassPath = JavaImporter(Packages.ch.ethz.csb.youscope.shared.TableDataAdapter,
		java.lang);
	with(javaClassPath)
	{
		var tableListener = new TableDataAdapter();
		jobs[0].addTableDataListener(tableListener);
	}
}

// run detection algorithm
jobs[0].executeJob(microscope);

// Get detected cells
tableData = tableListener.getLastTableData();
tableHeaders = tableListener.getLastColumnHeaders();

// zoom in every cell
if(tableData != undefined && tableData.length > 0)
{
	// Output found cell table.
	//messageSink.println(java.lang.Integer.toString(tableData.length) + " cells found.");
	//for(i=0; i<tableData.length; i++)
	//{
	//	text = "Cell " + java.lang.Integer.toString(i+1) + ": ";
	//	for(j=0; j<tableData[i].length; j++)
	//	{
	//		if(j> 0)
	//			text += ", ";
	//		text += tableHeaders[j] + ": " + tableData[i][j];
	//	}
	//	messageSink.println(text);
	//}

	// Check if at least one cell is positive
	foundOne = false;
	for(i=0; i<tableData.length; i++)
	{
		relativeFluor1 = undefined;
		relativeFluor2 = undefined;
		for(j=0; j<tableData[i].length; j++)
		{
			if(tableHeaders[j].equals("relative-fluorescence1"))
				relativeFluor1 = java.lang.Double.parseDouble(tableData[i][j]);
			else if(tableHeaders[j].equals("relative-fluorescence2"))
				relativeFluor2 = java.lang.Double.parseDouble(tableData[i][j]);
		}
		if(relativeFluor1 == undefined || relativeFluor2 == undefined)
			continue;
		// check if we found a searched cell...
		if(relativeFluor2 / relativeFluor1 < threshold)
		{
			foundOne = true;
			break;
		}
	}	
	if(foundOne)
	{
		// Get current position
		zeroPosition = microscope.getStageDevice().getPosition();
		zeroFocus = microscope.getFocusDevice().getFocusPosition();

		// Deactivate PFS
		microscope.getAutoFocusDevice().setEnabled(false);
		
		// change magnification
		microscope.getStateDevice(objectiveDeviceID).setState(objectiveID1);	

		// change binning
		microscope.getCameraDevice().getProperty('Binning').setValue(binning1);
	
		// Iterate and go to every position
		for(i=0; i<tableData.length; i++)
		{
			xPixels = undefined;
			yPixels = undefined;
			relativeFluor1 = undefined;
			relativeFluor2 = undefined;
			for(j=0; j<tableData[i].length; j++)
			{
				if(tableHeaders[j].equals("cell.center.x"))
					xPixels = java.lang.Double.parseDouble(tableData[i][j]);
				else if(tableHeaders[j].equals("cell.center.y"))
					yPixels = java.lang.Double.parseDouble(tableData[i][j]);
				else if(tableHeaders[j].equals("relative-fluorescence1"))
					relativeFluor1 = java.lang.Double.parseDouble(tableData[i][j]);
				else if(tableHeaders[j].equals("relative-fluorescence2"))
					relativeFluor2 = java.lang.Double.parseDouble(tableData[i][j]);
			}
			if(xPixels == undefined || xPixels < 0 || xPixels > numPixelsX
				|| yPixels == undefined || yPixels < 0 || yPixels > numPixelsY
				|| relativeFluor1 == undefined || relativeFluor2 == undefined)
				continue;

			// check if we found a searched cell...
			if(relativeFluor2 / relativeFluor1 > threshold)
				continue;

			// Calculate distance from center
			deltaPixelsX = xPixels - numPixelsX0/2;
			deltaPixelsY = yPixels - numPixelsY0/2;
			deltaX = deltaPixelsX * rawPixelSize0 / magnification0;
			deltaY = deltaPixelsY * rawPixelSize0 / magnification0;

			// Center cells
			microscope.getStageDevice().setPosition(zeroPosition.x + deltaX - 1, zeroPosition.y + deltaY);
	
			// evaluate image protocol
			jobs[1].executeJob(microscope);
		}

		// change binning
		microscope.getCameraDevice().getProperty('Binning').setValue(binning0);

		// change back magnification
		microscope.getStateDevice(objectiveDeviceID).setState(objectiveID0);	

		// Go back to original position
		microscope.getStageDevice().setPosition(zeroPosition.x, zeroPosition.y);
		microscope.getFocusDevice().setFocusPosition(zeroFocus);

		// Activate PFS
		microscope.getAutoFocusDevice().setEnabled(true);
	}
}
else
{
	//messageSink.println("Table data undefined.");

	//if(tableHeaders != undefined)
	//{
	//	for(i=0; i<tableHeaders.length; i++)
	//	{
	//		messageSink.println("Header: " + tableHeaders[i]);
	//	}
	//}
	//else
	//{
	//	messageSink.println("Table headers undefined.");
	//}
}
