#! /usr/bin/env python
# This is the Jython dropbox for microscope metadata created and automatically uploaded by YouScope
# The dropbox is indirectly activated through the images dropbox, which copies the respective data in this dropbox and then
# activate this script. This separation is necessary since the data dropbox and the metadata dropbox have to be logically separated under OpenBIS.
import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ImageMetadata
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from datetime import datetime
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations import ImageTransformationBuffer 
from java.lang import IllegalArgumentException

#################################################
# definition/readout of the measured well feature.
#################################################
def defineMeasuredWellsFeature(featuresBuilder, measuredWellsPath):
    # define a new feature representing the measured wells
    measuredWellsFeature = featuresBuilder.defineFeature("MEASURED_WELLS")
    measuredWellsFeature.setFeatureLabel("Metadata")
    measuredWellsFeature.setFeatureDescription("Measured Wells")
    
    # Read out measured wells	
    file = open(measuredWellsPath)
    headerLine = True
    for line in file.readlines():
        if headerLine:
            headerLine = False
            continue
        if len(line) <= 0: # Last line
            break
        try:
            measuredWellsFeature.addValue(line.strip("\n"), "True")
        except IllegalArgumentException, exception:
            raise Exception("Invalid well '" + line + "' in file '" + measuredWellsPath + "' (%s)." % exception.toString())
        
    file.close()
			
#################################################
# Code called for incoming metadata.
# Following variables are defined:
# incoming    : java.io.File
# factory     : ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDataSetHandler
# state       : ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState  (?)
#             : or ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDataSetHandler.JythonPlateDatasetFactory 
# factory     : ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler.JythonObjectFactory  (?) 
#################################################
if incoming.isDirectory():
    # Extract space and plate name based on name of folder
    directoryInformation = incoming.getName().split("_")
    space = directoryInformation[0].upper()
    plate = directoryInformation[1].upper()
    transaction = service.transaction()
    plateHandle = transaction.getSample("/" + space + "/" + plate)
    if plateHandle == None:
        raise Exception("Plate '/" + space + "/" + plate + "' to associate metadata to does not exist.")
    
    # Create metadata for imaged wells based on CSV file.
    measuredWellsPath = incoming.getPath() + "/measured-wells.csv"
    featuresBuilder = factory.createFeaturesBuilder()
    defineMeasuredWellsFeature(featuresBuilder, measuredWellsPath)
    analysisRegistrationDetails = factory.createFeatureVectorRegistrationDetails(featuresBuilder, incoming)
    transaction = service.transaction()
    analysisDataset = transaction.createNewDataSet(analysisRegistrationDetails)
    
    # set plate to which the dataset should be associated
    analysisDataset.setSample(plateHandle)
    
    # Move files to measurement
    transaction.moveFile(measuredWellsPath, analysisDataset)
    
    # Commit the transaction    
    transaction.commit()
    
    # Delete now empty folder
    os.rmdir(incoming.getPath()) 