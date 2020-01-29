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
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.CameraField;
import org.youscope.uielements.ChannelField;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.IntegerTextField;

class CustomizationTabLiveStream extends ManageTabElement
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 1706625732109422871L;

    private final CameraField cameraField;
    private final ChannelField channelField;
    private final DoubleTextField exposureField = new DoubleTextField();
    private final IntegerTextField imagingPeriodField = new IntegerTextField();
    private final JCheckBox autoStartField = new JCheckBox("Start LiveStream when opening.");
    private final JCheckBox increaseContrastField = new JCheckBox("Automatically increase contrast.");
    private final JCheckBox defaultField = new JCheckBox("Always use default channel configuration instead of last one.");
    private final DynamicPanel defaultPanel = new DynamicPanel();
    private final YouScopeClient client;
    CustomizationTabLiveStream(final YouScopeFrame frame)
    {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("LiveStream"));
        setOpaque(false);
        
        client = new YouScopeClientConnectionImpl();
        
        cameraField = new CameraField(client, YouScopeClientImpl.getServer());
		channelField = new ChannelField(client, YouScopeClientImpl.getServer());
		exposureField.setMinimalValue(0);
		imagingPeriodField.setMinimalValue(0);
		autoStartField.setOpaque(false);
		increaseContrastField.setOpaque(false);
		defaultField.setOpaque(false);
        
        DynamicPanel content = new DynamicPanel();
        content.add(autoStartField);
        content.add(defaultField);
        
        if(cameraField.isChoice())
        {
        	defaultPanel.add(new JLabel("Camera:"));
            defaultPanel.add(cameraField);
        }
        
        defaultPanel.add(new JLabel("Channel:"));
        defaultPanel.add(channelField);
        defaultPanel.add(new JLabel("Exposure (ms):"));
        defaultPanel.add(exposureField);
        defaultPanel.add(new JLabel("Imaging Period (ms):"));
        defaultPanel.add(imagingPeriodField);
        defaultPanel.add(increaseContrastField);
        content.add(defaultPanel);       
        content.addFillEmpty();
        add(content, BorderLayout.CENTER);
        
        defaultField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				defaultPanel.setVisible(defaultField.isSelected());
				frame.pack();
			}
		});
    }
    @Override
    public void initializeContent()
    {
    	boolean defaultSettings = (boolean) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_USE_DEFAULT_SETTINGS);
    	defaultField.setSelected(defaultSettings);
    	defaultPanel.setVisible(defaultSettings);
    	
    	autoStartField.setSelected((boolean) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_AUTOSTART));
    	exposureField.setValue(client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_EXPOSURE));
		imagingPeriodField.setValue(client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_PERIOD));
		cameraField.setCamera((String) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CAMERA));
		channelField.setChannel((String)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CHANNEL_GROUP), 
				(String)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CHANNEL));
		increaseContrastField.setSelected((boolean) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_IMAGE_PANEL_DEFAULT_AUTO_CONTRAST));
    }

    @Override
    public boolean storeContent()
    {
    	client.getPropertyProvider().setProperty(StandardProperty.PROPERTY_STREAM_USE_DEFAULT_SETTINGS, defaultField.isSelected());
    	client.getPropertyProvider().setProperty(StandardProperty.PROPERTY_STREAM_AUTOSTART, autoStartField.isSelected());
    	client.getPropertyProvider().setProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_EXPOSURE, exposureField.getValue().doubleValue());
		client.getPropertyProvider().setProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_PERIOD, imagingPeriodField.getValue().intValue());
		client.getPropertyProvider().setProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CAMERA, cameraField.getCameraDevice());
		client.getPropertyProvider().setProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CHANNEL_GROUP, channelField.getChannelGroup()); 
		client.getPropertyProvider().setProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CHANNEL, channelField.getChannel());
		client.getPropertyProvider().setProperty(StandardProperty.PROPERTY_IMAGE_PANEL_DEFAULT_AUTO_CONTRAST, increaseContrastField.isSelected());
        return false;
    }

}
