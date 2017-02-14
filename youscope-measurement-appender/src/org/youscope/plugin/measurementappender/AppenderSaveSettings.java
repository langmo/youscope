package org.youscope.plugin.measurementappender;

import java.rmi.RemoteException;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.saving.SaveInformation;
import org.youscope.common.saving.SaveSettings;

/**
 * Standard save settings, e.g. the definition where images should be stored under which name.
 * @author mlang
 *
 */
public class AppenderSaveSettings extends ResourceAdapter<AppenderSaveSettingsConfiguration> implements SaveSettings
{

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -3600016559165429151L;
	
	private final SaveSettings encapsulatedSettings;
	private final static String EXTEND_POSTFIX = "_continued";
	/**
	 * Constructor.
	 * @param positionInformation Position information.
	 * @param configuration configuration of the save settings.
	 * @param encapsulatedSettings 
	 * @throws ConfigurationException
	 * @throws RemoteException 
	 */
	public AppenderSaveSettings(PositionInformation positionInformation, AppenderSaveSettingsConfiguration configuration, SaveSettings encapsulatedSettings)
					throws ConfigurationException, RemoteException {
		super(positionInformation, configuration, AppenderSaveSettingsConfiguration.TYPE_IDENTIFIER, AppenderSaveSettingsConfiguration.class, "appender save settings");
		this.encapsulatedSettings = encapsulatedSettings;
	}

	@Override
	public String getMeasurementConfigurationFilePath(SaveInformation saveInformation) throws ResourceException, RemoteException {
		String orgFile = encapsulatedSettings.getMeasurementConfigurationFilePath(saveInformation);
		String base = orgFile.substring(0, orgFile.lastIndexOf('.'));
		String extension = orgFile.substring(orgFile.lastIndexOf('.'));
		return base+EXTEND_POSTFIX+extension;
	}

	@Override
	public String getMicroscopeConfigurationFilePath(SaveInformation saveInformation) throws ResourceException, RemoteException {
		String orgFile = encapsulatedSettings.getMicroscopeConfigurationFilePath(saveInformation);
		String base = orgFile.substring(0, orgFile.lastIndexOf('.'));
		String extension = orgFile.substring(orgFile.lastIndexOf('.'));
		return base+EXTEND_POSTFIX+extension;
	}

	@Override
	public String getLogErrFilePath(SaveInformation saveInformation) throws ResourceException, RemoteException {
		return encapsulatedSettings.getLogErrFilePath(saveInformation);
	}

	@Override
	public String getLogOutFilePath(SaveInformation saveInformation) throws ResourceException, RemoteException {
		return encapsulatedSettings.getLogOutFilePath(saveInformation);
	}

	@Override
	public String getMeasurementBasePath(SaveInformation saveInformation) {
		return getConfiguration().getBaseFolder();
	}

	@Override
	public String getTableFilePath(SaveInformation saveInformation, String tableName) throws ResourceException, RemoteException {
		return encapsulatedSettings.getTableFilePath(saveInformation, tableName);
	}

	@Override
	public String getImageMetadataTableFilePath(SaveInformation saveInformation) throws ResourceException, RemoteException {
		return encapsulatedSettings.getImageMetadataTableFilePath(saveInformation);
	}

	@Override
	public String getImageFilePath(SaveInformation saveInformation, ImageEvent<?> event, String imageName) throws ResourceException, RemoteException 
	{
		// modify execution information of image
		ExecutionInformation orgInf = event.getExecutionInformation();
		if(orgInf != null && orgInf.getMeasurementStartTime() != saveInformation.getMeasurementStartTime()-getConfiguration().getPreviousRuntime())
		{
			ExecutionInformation newInf = new ExecutionInformation(saveInformation.getMeasurementStartTime()-getConfiguration().getPreviousRuntime(), orgInf.getMeasurementPauseDuration(), orgInf.getEvaluationNumber()+getConfiguration().getDeltaEvaluationNumber());
			for(long loopNumber : orgInf.getLoopNumbers())
				newInf = new ExecutionInformation(newInf, loopNumber);
			event.setExecutionInformation(newInf);
		}
		return encapsulatedSettings.getImageFilePath(saveInformation, event, imageName); 
	}

	@Override
	public String getImageExtension(SaveInformation saveInformation, ImageEvent<?> event, String imageName) throws ResourceException, RemoteException {
		return encapsulatedSettings.getImageExtension(saveInformation, event, imageName);
	}

	@Override
	public String getXMLInformationFilePath(SaveInformation saveInformation) throws ResourceException, RemoteException {
		String orgFile = encapsulatedSettings.getXMLInformationFilePath(saveInformation);
		String base = orgFile.substring(0, orgFile.lastIndexOf('.'));
		String extension = orgFile.substring(orgFile.lastIndexOf('.'));
		return base+EXTEND_POSTFIX+extension;
	}

	@Override
	public String getHTMLInformationFilePath(SaveInformation saveInformation)
			throws ResourceException, RemoteException {
		String orgFile = encapsulatedSettings.getHTMLInformationFilePath(saveInformation);
		String base = orgFile.substring(0, orgFile.lastIndexOf('.'));
		String extension = orgFile.substring(orgFile.lastIndexOf('.'));
		return base+EXTEND_POSTFIX+extension;
	}
	
}
