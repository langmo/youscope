/**
 * 
 */
package org.youscope.plugin.multicamerastream;

/**
 * @author langmo
 *
 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceType;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

class CameraSelectionDialog
{
	protected YouScopeClient advisor;
	YouScopeFrame frame;
	protected YouScopeServer server;
	
	protected String[] cameraDevices = new String[0];
	protected JCheckBox[] cameraCheckBoxes;
	
	CameraSelectionDialog(YouScopeFrame frame, YouScopeServer server, YouScopeClient advisor)
	{
		this.frame = frame;
		frame.setClosable(true);
		frame.setMaximizable(false);
		frame.setResizable(false);
		frame.setTitle("Choose Cameras");
		
		this.advisor = advisor;
		
		this.server = server;
		
		// Get cameras
		try
		{
			Device[] devices = server.getMicroscope().getDevices(DeviceType.CameraDevice);
			cameraDevices = new String[devices.length];
			for(int i=0; i<cameraDevices.length; i++)
			{
				cameraDevices[i] = devices[i].getDeviceID();
			}
		}
		catch (Exception e2)
		{
			advisor.sendError("Could not detect installed cameras.", e2);
			return;
		}		
		
		GridBagLayout elementsLayout = new GridBagLayout();
		JPanel elementsPanel = new JPanel(elementsLayout);
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
        addConfElement(new JLabel("Select cameras you want to image with:"), elementsLayout, newLineConstr, elementsPanel);
        
        GridBagLayout camerasLayout = new GridBagLayout();
		JPanel camerasPanel = new JPanel(camerasLayout);
		cameraCheckBoxes = new JCheckBox[cameraDevices.length];
		for(int i = 0; i < cameraDevices.length; i++)
		{
			cameraCheckBoxes[i] = new JCheckBox(cameraDevices[i], true);
			addConfElement(cameraCheckBoxes[i], camerasLayout, newLineConstr, camerasPanel);
		}
		addConfElement(new JPanel(), camerasLayout, StandardFormats.getBottomContstraint(), camerasPanel);

		JButton startImagingButton = new JButton("start imaging");
		startImagingButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					startButtonPressed();
				}
			});
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(startImagingButton, BorderLayout.SOUTH);
		contentPane.add(elementsPanel, BorderLayout.NORTH);
		contentPane.add(new JScrollPane(camerasPanel), BorderLayout.CENTER);
		frame.setContentPane(contentPane);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	protected void startButtonPressed()
	{
		Vector<String> cameras = new Vector<String>();
		for(int i = 0; i < cameraDevices.length; i++)
		{
			if(cameraCheckBoxes[i].isSelected())
				cameras.addElement(cameraDevices[i]);
		}
		frame.setVisible(false);
				
		// Start main window.
		@SuppressWarnings("unused")
		MultiStreamFrame multiStreamFrame = new MultiStreamFrame(frame.createFrame(), server, advisor, cameras.toArray(new String[cameras.size()]));
	}
	protected static void addConfElement(Component component, GridBagLayout layout,
            GridBagConstraints constr, JPanel panel)
    {
        layout.setConstraints(component, constr);
        panel.add(component);
    }
}
