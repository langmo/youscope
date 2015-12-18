package org.youscope.common.saving;

import org.youscope.common.image.ImageEvent;
import org.youscope.common.resource.Resource;

/**
 * Configuration of the folder structure defining how measurement data should be saved.
 * For each standard file, a path relative to the measurement base folder can be given. The measurement base folder, is an absolute 
 * path. It typically consists of a base path which the user can define, and a sub-folder with a name being some combination of the name of the measurement and the measurement start time.
 * All paths are given with file separators "/", not "\" or similar.
 * Example: The user selects that all measurements should be saved in "C:/measurements". The measurement base path ({@link #getMeasurementBasePath(String, long)}) is constructed from this user chosen path, the measurement name (in this example "my_measurement") and the current time. 
 * The full base path returned by {@link #getMeasurementBasePath(String, long)} would than be
 * "C:/measurementsmy_measurement/2016-08-16_17-43-15" (date in year-month-day_hour-minute-second convention). The image path (as returned by {@link #getImageFilePath(ImageEvent, String)}) of an image made in well B2, at iteration 5, with name "gfp" and file type tiff is e.g. given by "B2/gfp_time0005". 
 * Then, the full path of the image file stored will be
 * "C:/measurements/my_measurement/2016-08-16_17-43-15/B2/gfp_time0005.tif".
 * @author Moritz Lang
 *
 */
public interface SaveSettings extends Resource 
{    
    /**
     * Path of the XML file (with extension) where the scope and channel settings are saved into. Typically "scope_settings.xml".
     * The path is relative to {@link #getMeasurementBasePath(String, long)}.
     * @return Scope settings file path
     */
    String getScopeSettingsFilePath();
    
    /**
     * Path of the file (with extension) where the measurement configuration should be saved into. Typically "configuration.csb". Note: by
     * convention, all measurement configurations should have the file extension "csb".
     * The path is relative to {@link #getMeasurementBasePath(String, long)}.
     * @return Measurement configuration file path.
     */
    String getMeasurementConfigurationFilePath();
    
    /**
     * Path of the file (with extension) where the microscope configuration should be saved into. Typically "YSConfig_Microscope.cfg".
     * The path is relative to {@link #getMeasurementBasePath(String, long)}.
     * @return Microscope configuration file path.
     */
    String getMicroscopeConfigurationFilePath();
    /**
     * Returns the file path for the error log file (with extension). Typically "measurement_err.txt".
     * The path is relative to {@link #getMeasurementBasePath(String, long)}.
     * @return Error file path.
     */
    String getLogErrFilePath();
    
    /**
     * Returns the file path for the standard output log file (with extension). Typically "measurement_log.txt".
     * The path is relative to {@link #getMeasurementBasePath(String, long)}.
     * @return Log file path.
     */
    String getLogOutFilePath();
    
    /**
     * Returns the name of the base folder/path where the measurement is saved, and relative to which all other paths are given. Typically
     * in the format "C:/measurementFolder/measurementName/year-month-day_hour-minute-second" or similar,
     * whereby "C:/measurementFolder/" (or an equivalence for POSIX systems) can be chosen by the user, while measurementName is the name of the measurement supplied to this method. 
     * Note, that this path should be different every time the
     * measurement is started, i.e. necessarily has to contain some time related part with a high enough granularity such that no two runs of the same
     * measurement will have the same time identifier (typically implies at least granularity down down to the minute, if not to the second).  
     * @param measurementName Name of the measurement.
     * @param timeMs Time in ms when measurement was started. See {@link System#currentTimeMillis()}.
     * @return Base path of the measurement.
     */
    String getMeasurementBasePath(String measurementName, long timeMs);
    
    /**
     * Returns a file path (with extension) where a table with a given name should be saved. By convention, the file type should be "csv".
     * Note that, different to images which are stored in one file per image, tables are usually stored one file for every named table.
     * Identifiers when and where a given row of the table is produced is automatically added by YouScope.
     * The path is relative to {@link #getMeasurementBasePath(String, long)}. 
     * @param tableName User chosen table name.
     * @return table file path.
     */
    String getTableFilePath(String tableName);
    
    /**
     * Returns a file path (with extension) where the table storing metadata about the images should be stored. Typically "images.csv". By convention,
     * the file type should be "csv".
     * The path is relative to {@link #getMeasurementBasePath(String, long)}.
     * @return image table path.
     */
    String getImageMetadataTableFilePath();
    
    /**
     * Returns a file path (with extension) where an image with a given name should be saved. It is important that this path should not define
     * the extension of the image.
     * Note that, images are stored in one file per image. Therefore, the image path should be different for images produced by the same image producer
     * at a different iteration. Therefore, the image creation information stored in the image event should be used to determine the path.
     * The path is relative to {@link #getMeasurementBasePath(String, long)}. 
     * @param event image event.
     * @param imageName user chosen name of the image.
     * @return image path, with extension.
     */
    String getImageFilePath(ImageEvent<?> event, String imageName);
    
    /**
     * Returns a extension/file type (without a dot, e.g. "tif" and not ".tif") of an image. Typically, this is the same as the extension of the file returned by
     * {@link #getImageExtension(ImageEvent, String)}. This extension is internally used to determine how the image should be saved, and should match
     * be a supported image type (for more, see YouScope server details).
     * @param event image event.
     * @param imageName user chosen name of the image.
     * @return image extension, without a dot.
     */
    String getImageExtension(ImageEvent<?> event, String imageName);
}
