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
package org.youscope.plugin.openbis;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class ConnectionConfigurationFrame
{
	private final Vector<ActionListener> configurationChangeListeners = new Vector<ActionListener>();
	private final YouScopeFrame frame;
	private final YouScopeClient client;
	
	public static final String SSH_USER_PROPERTY = "YouScope.OpenBIS.SSHUser";
	public static final String SSH_SERVER_PROPERTY = "YouScope.OpenBIS.SSHServer";
	public static final String SSH_PATH_PROPERTY = "YouScope.OpenBIS.SSHPath";
	
	private final JTextField sshUserField = new JTextField();
	private final JTextField sshServerField = new JTextField();
	private final JTextField sshDirectoryField = new JTextField();
	public ConnectionConfigurationFrame(YouScopeFrame frame, YouScopeClient client)
	{
		this.frame = frame;
		this.client = client;
		
		// Set frame properties
		frame.setTitle("Edit OpenBIS Connection");
		frame.setResizable(false);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		// Setup layout
		final GridBagConstraints newLineCnstr = StandardFormats.getNewLineConstraint();
		final GridBagLayout contentLayout = new GridBagLayout();
		final JPanel contentPanel = new JPanel(contentLayout);
		
		// Description
		JEditorPane descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>OpenBIS connection settings.</b></p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">YouScope connects to the OpenBIS server using a SSH connection.<br />"+
				"To be more precise, it uses rsync (see http://rsync.samba.org/) to synchronize the folders containing the measurement data with the OpenBIS server,<br />"+
				"using a secure SSH connection (via public/private SSH keys in the OpenSSH format). </p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">Please supply here the user name and server for the SSH connection, as well as the directory on the OpenBIS server "+
				"where incoming measurement data should be stored.<br />YouScope connects to the OpenBIS server using an URL in the form &lt;user&gt;@&lt;server&gt;:&lt;directory&gt;.</p>"+
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">For more information, please refer to the YouScope as well as the OpenBIS documentation.</p>" +
				"</html>");
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
		StandardFormats.addGridBagElement(descriptionScrollPane, contentLayout, newLineCnstr, contentPanel);
		
		// Data
		StandardFormats.addGridBagElement(new JLabel("SSH User:"), contentLayout, newLineCnstr, contentPanel);
		StandardFormats.addGridBagElement(sshUserField, contentLayout, newLineCnstr, contentPanel);
		StandardFormats.addGridBagElement(new JLabel("SSH Server:"), contentLayout, newLineCnstr, contentPanel);
		StandardFormats.addGridBagElement(sshServerField, contentLayout, newLineCnstr, contentPanel);
		StandardFormats.addGridBagElement(new JLabel("OpenBIS \"incoming-directory\" (unix-style path):"), contentLayout, newLineCnstr, contentPanel);
		StandardFormats.addGridBagElement(sshDirectoryField, contentLayout, newLineCnstr, contentPanel);
		
		// Save button
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				saveConfiguration();
				
				ConnectionConfigurationFrame.this.frame.setVisible(false);
			}
		});
		StandardFormats.addGridBagElement(saveButton, contentLayout, newLineCnstr, contentPanel);
		
		// Load settings
		loadConfiguration();
		
		// Set content in frame
		frame.setContentPane(contentPanel);
		frame.pack();
	}
	
	private void saveConfiguration()
	{
		client.getPropertyProvider().setProperty(SSH_USER_PROPERTY, sshUserField.getText());
		client.getPropertyProvider().setProperty(SSH_SERVER_PROPERTY, sshServerField.getText());
		client.getPropertyProvider().setProperty(SSH_PATH_PROPERTY, sshDirectoryField.getText());
		
		synchronized(configurationChangeListeners)
		{
			for(ActionListener listener :configurationChangeListeners)
			{
				listener.actionPerformed(new ActionEvent(ConnectionConfigurationFrame.this, 951, "OpenBIS configuration changed"));
			}
		}
	}
	
	private void loadConfiguration()
	{
		sshUserField.setText(client.getPropertyProvider().getProperty(SSH_USER_PROPERTY, ""));
		sshServerField.setText(client.getPropertyProvider().getProperty(SSH_SERVER_PROPERTY, ""));
		sshDirectoryField.setText(client.getPropertyProvider().getProperty(SSH_PATH_PROPERTY, ""));
	}
	
	public void addConfigurationChangeListener(ActionListener listener)
	{
		synchronized(configurationChangeListeners)
		{
			configurationChangeListeners.addElement(listener);
		}
	}
	public void removeConfigurationChangeListener(ActionListener listener)
	{
		synchronized(configurationChangeListeners)
		{
			configurationChangeListeners.removeElement(listener);
		}
	}
}
