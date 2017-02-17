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
package org.youscope.plugin.continuationmeasurement;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.MeasurementContext;
import org.youscope.common.MessageListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.saving.SaveInformation;
import org.youscope.common.saving.SaveSettings;

class EncapsulatedSaveSettings extends UnicastRemoteObject implements SaveSettings {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 4562689142733565577L;
	private final SaveSettings encapsulatedSettings;
	private final String measurementFolder;
	private final static String EXTEND_POSTFIX = "_continued";
	public EncapsulatedSaveSettings(SaveSettings encapsulatedSettings, String measurementFolder) throws RemoteException 
	{
		this.encapsulatedSettings = encapsulatedSettings;
		this.measurementFolder = measurementFolder;
	}

	@Override
	public String getTypeIdentifier() throws RemoteException {
		return encapsulatedSettings.getTypeIdentifier();
	}

	@Override
	public void initialize(MeasurementContext measurementContext) throws ResourceException, RemoteException {
		encapsulatedSettings.initialize(measurementContext);
	}

	@Override
	public void uninitialize(MeasurementContext measurementContext) throws ResourceException, RemoteException {
		encapsulatedSettings.uninitialize(measurementContext);
	}

	@Override
	public boolean isInitialized() throws RemoteException {
		return encapsulatedSettings.isInitialized();
	}

	@Override
	public void addMessageListener(MessageListener writer) throws RemoteException {
		encapsulatedSettings.addMessageListener(writer);
	}

	@Override
	public void removeMessageListener(MessageListener writer) throws RemoteException {
		encapsulatedSettings.removeMessageListener(writer);
	}

	@Override
	public PositionInformation getPositionInformation() throws RemoteException {
		return encapsulatedSettings.getPositionInformation();
	}

	@Override
	public String getName() throws RemoteException {
		return encapsulatedSettings.getName();
	}

	@Override
	public void setName(String name) throws RemoteException, ComponentRunningException {
		encapsulatedSettings.setName(name);
	}

	@Override
	public UUID getUUID() throws RemoteException {
		return encapsulatedSettings.getUUID();
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
		return measurementFolder;
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
