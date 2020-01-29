/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
package org.youscope.common.saving;

import java.rmi.RemoteException;

import org.youscope.common.image.ImageEvent;
import org.youscope.common.resource.Resource;
import org.youscope.common.resource.ResourceException;

/**
 * Configuration of the folder structure defining how measurement data should be saved.
 * For each standard file, a path relative to the measurement base folder can be given. The measurement base folder, is an absolute 
 * path. It typically consists of a base path which the user can define, and a sub-folder with a name being some combination of the name of the measurement and the measurement start time.
 * All paths are given with file separators "/", not "\" or similar.
 * Example: The user selects that all measurements should be saved in "C:/measurements". The measurement base path ({@link #getMeasurementBasePath(SaveInformation)}) is constructed from this user chosen path, the measurement name (in this example "my_measurement") and the current time. 
 * The full base path returned by {@link #getMeasurementBasePath(SaveInformation)} would than be
 * "C:/measurementsmy_measurement/2016-08-16_17-43-15" (date in year-month-day_hour-minute-second convention). The image path (as returned by {@link #getImageFilePath}) of an image made in well B2, at iteration 5, with name "gfp" and file type tiff is e.g. given by "B2/gfp_time0005". 
 * Then, the full path of the image file stored will be
 * "C:/measurements/my_measurement/2016-08-16_17-43-15/B2/gfp_time0005.tif".
 * @author Moritz Lang
 *
 */
public interface SaveSettings extends Resource 
{    
    /**
     * Path of the XML file (with extension) where the measurement metadata, and the scope and channel settings are saved into. Typically "information.xml".
     * The path is relative to {@link #getMeasurementBasePath(SaveInformation)}.
     * @param saveInformation Immutable object containing basic information about the measurement, like its name and start time. This might be used, but typically isn't, in calculating the return value.
     * @return XML information file path
     * @throws ResourceException 
     * @throws RemoteException
     * @see #getHTMLInformationFilePath(SaveInformation) 
     */
    String getXMLInformationFilePath(SaveInformation saveInformation) throws ResourceException,RemoteException;
    
    /**
     * Path of the HTML file (with extension) where the measurement metadata, and the scope and channel settings are saved into. Typically "information.html".
     * The path is relative to {@link #getMeasurementBasePath(SaveInformation)}.
     * @param saveInformation Immutable object containing basic information about the measurement, like its name and start time. This might be used, but typically isn't, in calculating the return value.
     * @return XML information file path
     * @throws ResourceException 
     * @throws RemoteException 
     * @see #getXMLInformationFilePath(SaveInformation)
     */
    String getHTMLInformationFilePath(SaveInformation saveInformation) throws ResourceException,RemoteException;
    
    /**
     * Path of the file (with extension) where the measurement configuration should be saved into. Typically "configuration.csb". Note: by
     * convention, all measurement configurations should have the file extension "csb".
     * The path is relative to {@link #getMeasurementBasePath(SaveInformation)}.
     * @param saveInformation Immutable object containing basic information about the measurement, like its name and start time. This might be used, but typically isn't, in calculating the return value.
     * @return Measurement configuration file path.
     * @throws ResourceException 
     * @throws RemoteException 
     */
    String getMeasurementConfigurationFilePath(SaveInformation saveInformation) throws ResourceException,RemoteException;
    
    /**
     * Path of the file (with extension) where the microscope configuration should be saved into. Typically "YSConfig_Microscope.cfg".
     * The path is relative to {@link #getMeasurementBasePath(SaveInformation)}.
     * @param saveInformation Immutable object containing basic information about the measurement, like its name and start time. This might be used, but typically isn't, in calculating the return value.
     * @return Microscope configuration file path.
     * @throws ResourceException 
     * @throws RemoteException 
     */
    String getMicroscopeConfigurationFilePath(SaveInformation saveInformation) throws ResourceException,RemoteException;
    /**
     * Returns the file path for the error log file (with extension). Typically "measurement_err.txt".
     * The path is relative to {@link #getMeasurementBasePath(SaveInformation)}.
     * @param saveInformation Immutable object containing basic information about the measurement, like its name and start time. This might be used, but typically isn't, in calculating the return value.
     * @return Error file path.
     * @throws ResourceException 
     * @throws RemoteException 
     */
    String getLogErrFilePath(SaveInformation saveInformation) throws ResourceException,RemoteException;
    
    /**
     * Returns the file path for the standard output log file (with extension). Typically "measurement_log.txt".
     * The path is relative to {@link #getMeasurementBasePath(SaveInformation)}.
     * @param saveInformation Immutable object containing basic information about the measurement, like its name and start time. This might be used, but typically isn't, in calculating the return value.
     * @return Log file path.
     * @throws ResourceException 
     * @throws RemoteException 
     */
    String getLogOutFilePath(SaveInformation saveInformation) throws ResourceException,RemoteException;
    
    /**
     * Returns the name of the base folder/path where the measurement is saved, and relative to which all other paths are given. Typically
     * in the format "C:/measurementFolder/measurementName/year-month-day_hour-minute-second" or similar,
     * whereby "C:/measurementFolder/" (or an equivalence for POSIX systems) can be chosen by the user, while measurementName is the name of the measurement supplied by {@link SaveInformation#getMeasurementName()}. 
     * Note, that this path should be different every time the
     * measurement is started ({@link SaveInformation#getMeasurementStartTime()}), i.e. necessarily has to contain some time related part with a high enough granularity such that no two runs of the same
     * measurement will have the same time identifier (typically implies at least granularity down down to the minute, if not to the second).  
     * @param saveInformation Immutable object containing basic information about the measurement, like its name and start time. 
     * @return Base path of the measurement.
     * @throws ResourceException 
     * @throws RemoteException 
     */
    String getMeasurementBasePath(SaveInformation saveInformation) throws ResourceException,RemoteException;
    
    /**
     * Returns a file path (with extension) where a table with a given name should be saved. By convention, the file type should be "csv".
     * Note that, different to images which are stored in one file per image, tables are usually stored one file for every named table.
     * Identifiers when and where a given row of the table is produced is automatically added by YouScope.
     * The path is relative to {@link #getMeasurementBasePath(SaveInformation)}.
     * @param saveInformation Immutable object containing basic information about the measurement, like its name and start time. This might be used, but typically isn't, in calculating the return value. 
     * @param tableName User chosen table name.
     * @return table file path.
     * @throws ResourceException 
     * @throws RemoteException 
     */
    String getTableFilePath(SaveInformation saveInformation, String tableName) throws ResourceException,RemoteException;
    
    /**
     * Returns a file path (with extension) where the table storing metadata about the images should be stored. Typically "images.csv". By convention,
     * the file type should be "csv".
     * The path is relative to {@link #getMeasurementBasePath(SaveInformation)}.
     * @param saveInformation Immutable object containing basic information about the measurement, like its name and start time. This might be used, but typically isn't, in calculating the return value.
     * @return image table path.
     * @throws ResourceException 
     * @throws RemoteException 
     */
    String getImageMetadataTableFilePath(SaveInformation saveInformation) throws ResourceException,RemoteException;
    
    /**
     * Returns a file path (with extension) where an image with a given name should be saved. The extension (e.g. "tif" for "gfp_pos01_time003.tif") should usually match
     * the return value of {@link #getImageExtension(SaveInformation, ImageEvent, String)}.
     * Note that, images are stored in one file per image. Therefore, the image path should be different for images produced by the same image producer
     * at a different iteration. Therefore, the image creation information stored in the image event should be used to determine the path.
     * The path is relative to {@link #getMeasurementBasePath(SaveInformation)}.
     * @param saveInformation Immutable object containing basic information about the measurement, like its name and start time. This might be used, but typically isn't, in calculating the return value. 
     * @param event image event.
     * @param imageName user chosen name of the image.
     * @return image path, with extension.
     * @throws ResourceException 
     * @throws RemoteException 
     */
    String getImageFilePath(SaveInformation saveInformation, ImageEvent<?> event, String imageName) throws ResourceException,RemoteException;
    
    /**
     * Returns a extension/file type (without a dot, e.g. "tif" and not ".tif") of an image. Typically, this is the same as the extension of the file returned by
     * {@link #getImageFilePath(SaveInformation,ImageEvent, String)}. This extension is internally used to determine how the image should be saved, and should match
     * to a supported image type (for more, see YouScope server details).
     * @param saveInformation Immutable object containing basic information about the measurement, like its name and start time. This might be used, but typically isn't, in calculating the return value.
     * @param event image event.
     * @param imageName user chosen name of the image.
     * @return image extension, without a dot.
     * @throws ResourceException 
     * @throws RemoteException 
     */
    String getImageExtension(SaveInformation saveInformation, ImageEvent<?> event, String imageName) throws ResourceException,RemoteException;
}
