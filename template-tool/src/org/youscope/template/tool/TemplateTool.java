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
/**
 * 
 */
package org.youscope.template.tool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.Microscope;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;

/**
 * A template which can be used as a starting point for tool development.
 * A tool defines the user interface which is exposed to the user, as well as anything which should happen when pressing anything in the UI,
 * e.g. to manipulate the state of the microscope, to display an image, and similar.
 * Every tool has to implement {@link ToolAddonUI} specifying e.g. different layouts of the UI depending on if the UI is shown in an own frame,
 * or as a sub-element of another user interface. However, implementing this interface directly is typically rather complicated. For most tools it
 * is therefore sufficient to extend the adapter class {@link ToolAddonUIAdapter} instead of implementing {@link ToolAddonUI} directly. The
 * adapter class takes over most of the boilerplate code being the same for nearly all tools, such that one can concentrate on the "real functionality"
 * of the tool. However, it is still possible to implement {@link ToolAddonUI} directly if the tool has to do something different then most other tools...
 * 
 * @author Moritz Lang
 *
 */
public class TemplateTool extends ToolAddonUIAdapter implements YouScopeFrameListener
{
	/**
	 * The type identifier is a unique ID of your tool. No other tool or other element of YouScope should
	 * have the same type identifier as your tool, thus, change this identifier to something unique. The general notation is
	 * your_identifier DOT tool_identifier, in which your_identifier is e.g. SmithAnton, MyCompanyName, or LonleyProgrammer1972.
	 * tool_identifer is the name of your tool, e.g. FancyFocus, MySpecialDeviceController, or HelloWorldTool.
	 */
	public final static String TYPE_IDENTIFIER = "YouScope.Template.Tool";
	
	/**
	 * Returns the metadata of this tool. The metadata consists of a unique identifier for the tool, a human readable name of the tool, an array of names
	 * of folders under which the tool should be displayed in YouScope (could be empty to display it as a default tool), and similar. To not have to
	 * implement all functions of the interface {@link ToolMetadata} ourselves, we return an instance of the adapter class {@link ToolMetadataAdapter}, which does most of the
	 * boilerplate code for us.
	 * @return Metadata of tool.
	 */
	public static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Template Tool", new String[]{"Templates"}, 
				"Template to generate new tool addons for YouScope.",
				"icons/projection-screen-presentation.png");
	}
	
	/**
	 * Constructor. Provides the adapter class {@link ToolAddonUIAdapter} with all necessary information to provide the standard
	 * functionality which has to be exposed by every tool, such that we can concentrate on the fundamentals. 
	 * Do not initialize the UI elements here (do this in {@link #createUI()}). 
	 * @param client Interface to the YouScope client, e.g. allowing to open or close new windows, or permanently save settings.
	 * @param server Interface to the YouScope server, e.g. allowing access to the microscope.
	 * @throws AddonException 
	 */
	public TemplateTool(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server); 
	}
	@Override
	public java.awt.Component createUI()
	{
		/**
		 *  This is the place to setup the UI elements of the first window of your tool.
		 */
		
		// Title of the window, as well as basic configuration.
		setMaximizable(false);
		setResizable(true);
		setTitle("Template Tool");
			        
        // All UI elements should be placed inside a panel, typically referred to as content pane.
		// A {@link DynamicPanel} is a special kind of panel used nearly everywhere in YouScope. It simplifies positioning 
		// of UI elements such that they look "native" for YouScope.
		DynamicPanel contentPane = new DynamicPanel();
        // Let us add some content...
        JLabel label = new JLabel("Hello World");
        contentPane.add(label);
        JTextArea textArea = new JTextArea(
        		 "This is an example of a YouScope tool.\n"
        		+"A tool typically accesses the microscope directly,\n"
        		+"performing little tasks like changing the current position.\n"
        		+"Tools are not intended to produce data; use measurements,\n"
        		+"respectively jobs, for data collection instead.");
        // addFill means that the added element can take up extra space when the frame containing it is resized.
        contentPane.addFill(textArea);
        
        // Let's add a button to actually do something. In this case, to find out the camera name
        JButton button = new JButton("Find out camera name");
        contentPane.add(button);
        contentPane.add(new JLabel("Camera Name:"));
        final JTextField cameraNameField = new JTextField();
        button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				// Call a function to find out the camera name and to display it in the respective text field.
				try {
					findOutCameraName(cameraNameField);
				} catch (RemoteException e1) {
					// A remote exception can only appear when the YouScope client and the server run on different computers,
					// i.e. when the microscope is physically connected to one computer (the "server"), while the user interface to
					// control the microscope is displayed on another computer (the "client") communicating with the server over
					// a local network. Then, a remote exception can happen if the network connection breaks down.
					// We just display the error to the user.
					getClient().sendError("Error in connection to microscope.", e1);
				} catch (DeviceException e1) {
					// This device exception can appear if there is no camera attached (configured). Then, microscope.getCameraDevice() 
					// will throw an exception.
					getClient().sendError("No camera device attached.", e1);
				}
			}
		});
        contentPane.add(cameraNameField);
		
        // finally, let us add a listener (ourselves) which gets notified when the frame containing this tool is made visible, or when it is closed.
        getContainingFrame().addFrameListener(this);
        
        return contentPane;
	}
	
	private void findOutCameraName(JTextField textField) throws RemoteException, DeviceException
	{
		/**
		 *  In this function we find out the name of the current standard camera, and display it in
		 *  the provided text field.
		 */

		// First, we get a reference to the YouScope server. The server contains all "real" functionality of YouScope, e.g. the possibility
		// to access devices.
		YouScopeServer server = getServer();
		
		// Now, let's get a reference to the microscope object. This object allows to directly access and manipulate the state of
		// microscope devices.
		Microscope microscope = server.getMicroscope();
		
		// Now, let us ask for the default camera...
		CameraDevice camera = microscope.getCameraDevice();
		
		// and its name, respectively the ID of the camera.
		String cameraName = camera.getDeviceID();
		
		// finally, let us display this name to the user
		textField.setText(cameraName);
	}
	
	@Override
	public void frameClosed()
	{
		// Put in things here which should be done when frame containing tool closes, e.g. some cleanup...
	}

	@Override
	public void frameOpened()
	{
		// Put in things here which should be done when the frame containing this tool opens. This might e.g.
		// be automatically taking an image or similar.
	}
}
