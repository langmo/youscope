#! /usr/bin/env python
# This is the Jython dropbox for microscope images created and automatically uploaded by YouScope
import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ImageMetadata
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from datetime import datetime
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations import ImageTransformationBuffer 
from ch.systemsx.cisd.openbis.generic.shared.basic.dto.api import ValidationException

class YouScopeImageDataConfig(SimpleImageDataConfig):
    def __init__(self, images):
        self.images = images

    def extractImageMetadata(self, imagePath):
        # Get image information from image.csv file (read in before).
		if not imagePath in self.images.keys(): 
		    raise Exception("Image '" + imagePath + "' is not listed in images.csv (%d files known). No metadata can be extracted." % len(YouScopeImageDataConfig.images))   
		image = self.images[imagePath]
		
		# Create a new metadata object for the image
		imageMetadata = ImageMetadata()            
		
		# We set the channel name to the YouScope job name (same channel can be imaged multiple times).
		imageMetadata.channelCode = image[0]
		
		# Set the well
		imageMetadata.well = image[4]
		
		# Set the tile
		# The OpenBIS tile is used to store the YouScope positionInformation.
		if len(image[5]) > 0:
		    positionInformation = image[5].split("-")
		    # We here only have one tile number, wheras the YouScope position information can be n-dimensional,
		    # with n abitrary. We therefore set the tile number such that always two digits represent a position.
		    pos = 0;
		    for position in positionInformation:
		        pos *= 100;
		        try:
		            pos += int(position) + 1
		        except ValueError:
		            raise Exception("Cannot parse position information '" + image[5] + "' for file '" + imagePath + "'.")
		    imageMetadata.tileNumber = pos    
		else:
		    imageMetadata.tileNumber = 1
		
		# Set the timepoint
		# The timepoint is set to the evaluation number of the imaging job of YouScope + 1
		# TODO: Check if maybe better to set to the time when the image was made?
		try:
		    time = int(image[1]) + 1
		except ValueError:
		    raise Exception("Cannot parse evaluation number (== timepoint) '" + image[1] + "' for file '" + imagePath + "'.")
		imageMetadata.timepoint = time
		imageMetadata.seriesNumber = time
		
		return imageMetadata
		
    def getAvailableChannelTransformations(self, channelCode):        
		buffer = ImageTransformationBuffer()        
		buffer.appendAllBitShiftsFor12BitGrayscale()               
		buffer.appendAutoRescaleGrayscaleIntensity(0, "Min/Max Stretch")  
		buffer.appendImageMagicConvert("-edge 1 -depth 12", "Edge detection")        
		return buffer.getTransformations() 
		
    def getTileGeometry(self, imageTokens, maxTileNumber):
	    # get maximal position information
	    positionInformation = {}
	    i = 0
	    while not maxTileNumber == 0:
	        positionInformation[i] = maxTileNumber % 100
	        maxTileNumber = int(maxTileNumber / 100)
	        i += 1
	        
	    # Set the highes position as first axes and a combination of all others as the second axes
	    ypos = positionInformation[len(positionInformation) - 1]
	    xpos = 1
	    i = len(positionInformation) - 2
	    while i >= 0:
	        xpos *= positionInformation[i]
	        i -= 1
	    
	    # Save maximal position information for later processing
	    self.maxPosInformation = positionInformation
	    
	    return Geometry.createFromRowColDimensions(ypos, xpos)
	    
    def getTileCoordinates(self, tileNumber, tileGeometry):
	    # get position information
	    positionInformation = {}
	    i = 0
	    while not tileNumber == 0:
	        positionInformation[i] = tileNumber % 100
	        tileNumber = int(tileNumber / 100)
	        i += 1
	        
	    # Set the highest position as first axes and a combination of all others as the second axes
	    ypos = positionInformation[len(positionInformation) - 1]
	    xpos = 0
	    i = len(positionInformation) - 2
	    while i > 0:
	        xpos += positionInformation[i]
	        xpos *= self.maxPosInformation[i]
	        i -= 1
	    xpos += positionInformation[0]
	    return Location(ypos, xpos)
# end class YouScopeImageDataConfig

###########################################################
# Read in the CSV file which defines the properties of all images.
###########################################################
def processImageListFile(imageListFile):
    if not os.path.exists(imageListFile): 
        raise Exception("YouScope image information file images.csv does not exist. Expected location: '" + imageListFile + "'.")

    file = open(imageListFile)
    headerLine = True
    images= {}
    measuredWells = {}
    for line in file.readlines():
        if headerLine:
            headerLine = False
            continue
        if len(line) < 3:
            continue
        tokens = line.split(";")
        # Save all image information in a map, identified by their filename.
        # Lateron used to associate an image with a well, position, channel, ...
        image = {};
        images[tokens[0]] = tokens[1:]
        # Save that the well contains measurement data.
        # Lateron associated with the plate's metadata, to visualize which wells were measured.
        measuredWells[tokens[5]] = True;
    file.close()
    return (images, measuredWells)

###########################################################
# This function will be executed when a measurement arrived.
###########################################################
if incoming.isDirectory():
    # TODO: Test if this is a plate measurement at all. If not, generate some alternative.
    
    # Read in the image definition file
    (images, measuredWells) = processImageListFile(incoming.getPath() + "/images.csv")
    
    # Extract space, project and experiment based on name of folder
    directoryInformation = incoming.getName().split("_")
    # The space is the same as user name in YouScope
    space = directoryInformation[0].upper()
    # The project maps to the YouScope project one to one.
    project = directoryInformation[1].upper()
    # Experiment = measurement name in YouScope
    experiment = directoryInformation[2].upper()
	
    # Create the space, project and experiment if it yet does not exist
    transaction = service.transaction()
    spaceHandle = transaction.getSpace(space)
    if spaceHandle == None:
        transaction.createNewSpace(space, None)
    projectHandle = transaction.getProject("/"+space + "/" + project)
    if projectHandle == None:
        projectHandle = transaction.createNewProject("/"+space + "/" + project)
        projectHandle.setDescription("YouScope generated project.")
    experimentHandle = transaction.getExperiment("/"+space + "/" + project + "/" + experiment)
    if experimentHandle == None:
        experimentHandle = transaction.createNewExperiment("/" + space + "/" + project + "/" + experiment, "STANDARD")
        #experimentHandle.setPropertyValue("DESCRIPTION", "YouScope measurement created on " + datetime.today().strftime('%Y-%m-%d'))
    # Find a name for a plate which is not yet used
    continueIteration = True
    i = 1
    while continueIteration:
        currentPlate = "PLATE%d" % i
        sampleHandle = transaction.getSample("/" + space + "/" + currentPlate)
        if sampleHandle == None:
            continueIteration = False;
        else:
            i += 1		    
    sampleHandle = 	transaction.createNewSample("/"+space + "/" + currentPlate, "PLATE")
    sampleHandle.setExperiment(experimentHandle)
    # TODO: Integrate other geometries
    sampleHandle.setPropertyValue("$PLATE_GEOMETRY", "96_WELLS_8x12")
    transaction.commit()
	
	# Create new dataset information for image data.
    imageDataset = YouScopeImageDataConfig(images)
    imageDataset.setRawImageDatasetType()
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setGenerateHighQualityThumbnails(True)
    # Images were produced by YouScope using JAI (Java Advanced Imaging), so let's use it to display them, too.
    imageDataset.setImageLibrary("JAI")
    imageDataset.setMaxThumbnailWidthAndHeight(512)
    imageDataset.setUseImageMagicToGenerateThumbnails(False)
    imageDataset.setMicroscopyData(True)
    imageDataset.setPlate(space, currentPlate)
    
    factory.registerImageDataset(imageDataset, incoming, service) 
    
    ################################################################################################
    # When we came until here, the measurement images were saved. Now generate a folder in the metadata dropbox.
    # (Metadata can not be saved using the same dropbox as image data).
    ################################################################################################
    
    # First, find out the respective dropbox name
    metadataDropboxName = incoming.getParentFile().getName() + "-analysis"
    metadataDropbox = incoming.getParentFile().getParent() + "/" + metadataDropboxName
    if not os.path.exists(metadataDropbox):
        raise Exception("Metadata dropbox '" + metadataDropbox + "' does not exist. To save metadata created by YouScope in OpenBIS, create the respective dropbox.")
    
    # Create a new folder there with the plate where the data should be associated as the name
    metadataFolder = metadataDropbox + "/" + space + "_" + currentPlate
    os.mkdir(metadataFolder)
    
    # Write measured wells as a csv file
    wellFile = open(metadataFolder + "/measured-wells.csv", 'w')
    wellFile.write("Measured Wells\n")
    for well in measuredWells.keys():
        wellFile.write(well + "\n")
    wellFile.close()    

    # Create marker file to start processing of the metadata
    open(metadataDropbox + '/.MARKER_is_finished_' + space + "_" + currentPlate, 'w').close() 	

	