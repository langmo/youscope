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
package org.youscope.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.youscope.addon.microscopeaccess.MicroscopeConnectionFactory;
import org.youscope.uielements.ImageLoadingTools;

/**
 * @author langmo
 */
class MicroscopeConnectionTypeChooser extends JFrame
{
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID		= -1508841123883056644L;

	private JTextField						driverPathField			= new JTextField();

	private JComboBox<MicroscopeConnection>						connectionTypeField;

	private JPanel							folderPanel;

	private JLabel							folderLabel;

	private Vector<MicroscopeConnection>	microscopeConnections	= new Vector<MicroscopeConnection>();

	private volatile boolean				userInputFinished		= false;

	private JButton							connectButton;

	private JEditorPane						explanationArea;

	private class MicroscopeConnection
	{
		public final MicroscopeConnectionFactory	factory;

		public final String							connectionID;

		MicroscopeConnection(MicroscopeConnectionFactory factory, String connectionID)
		{
			this.factory = factory;
			this.connectionID = connectionID;
		}

		@Override
		public String toString()
		{
			return factory.getShortMicroscopeConnectionDescription(connectionID);
		}
	}

	public MicroscopeConnectionTypeChooser(String lastDriverPath, String lastConnectionType, Exception lastError)
	{
		super("Microscope Connection Type Configuration");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		try
		{
			setAlwaysOnTop(true);
		}
		catch(@SuppressWarnings("unused") SecurityException e)
		{
			// Do nothing.
		}

		// Get microscope connection types
		for(MicroscopeConnectionFactory factory : MicroscopeAccess.getMicroscopeConnectionFactories())
		{
			for(String connectionID : factory.getSupportedMicroscopeConnectionIDs())
			{
				microscopeConnections.addElement(new MicroscopeConnection(factory, connectionID));
			}
		}

		// Initialize layout
		getContentPane().setLayout(new BorderLayout());
		GridBagConstraints newLineConstr = new GridBagConstraints();
		GridBagLayout layout = new GridBagLayout();
		newLineConstr.fill = GridBagConstraints.HORIZONTAL;
		newLineConstr.gridwidth = GridBagConstraints.REMAINDER;
		newLineConstr.anchor = GridBagConstraints.NORTHWEST;
		newLineConstr.gridx = 0;
		newLineConstr.weightx = 1.0;
		newLineConstr.weighty = 0;
		// newLineConstr.insets = new Insets(5, 5, 5, 5);
		JPanel contentPanel = new JPanel(layout);
		contentPanel.setOpaque(true);
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(new EmptyBorder(0, 5, 0, 5));

		// Set tray icon image.
		final String TRAY_ICON_URL16 = "org/youscope/server/images/icon-16.png";
		final String TRAY_ICON_URL32 = "org/youscope/server/images/icon-32.png";
		final String TRAY_ICON_URL96 = "org/youscope/server/images/icon-96.png";
		final String TRAY_ICON_URL194 = "org/youscope/server/images/icon-194.png";
		ArrayList<Image> trayIcons = new ArrayList<Image>();
		Image trayIcon16 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL16, "tray icon");
		if(trayIcon16 != null)
			trayIcons.add(trayIcon16);
		Image trayIcon32 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL32, "tray icon");
		if(trayIcon32 != null)
			trayIcons.add(trayIcon32);
		Image trayIcon96 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL96, "tray icon");
		if(trayIcon96 != null)
			trayIcons.add(trayIcon96);
		Image trayIcon194 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL194, "tray icon");
		if(trayIcon194 != null)
			trayIcons.add(trayIcon194);
		if(trayIcons.size()>0)
			this.setIconImages(trayIcons);

		if(lastError != null)
		{
			// Display a message why last input was incorrect
			JEditorPane errorArea = new JEditorPane();
			errorArea.setEditable(false);
			errorArea.setContentType("text/html");

			String errorMessage = "<html><p style=\"font-size:small;color:EE2222;margin-top:0px;\"><b>Could not connect to the microscope.</p>" + "<p style=\"font-size:small;margin-top:8px;margin-bottom:0px\"><b>Detailed error desciption:</b></p>";
			Throwable error = lastError;
			for(; error != null; error = error.getCause())
			{
				errorMessage += "<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">";
				errorMessage += "<i>" + error.getClass().getSimpleName() + "</i>: " + error.getMessage().replace("\n", "<br />");
				errorMessage += "</p>";
			}
			errorArea.setText(errorMessage);

			JScrollPane errorScrollPane = new JScrollPane(errorArea);
			errorScrollPane.setPreferredSize(new Dimension(450, 115));
			addConfElement(errorScrollPane, layout, newLineConstr, contentPanel);
		}

		// Explanation what to do.
		explanationArea = new JEditorPane();
		explanationArea.setEditable(false);
		explanationArea.setContentType("text/html");
		JScrollPane explanationScrollPane = new JScrollPane(explanationArea);
		explanationScrollPane.setPreferredSize(new Dimension(450, 115));
		addConfElement(explanationScrollPane, layout, newLineConstr, contentPanel);

		// UI elements
		JLabel connectionTypeLabel = new JLabel("Connection Type:");
		connectionTypeField = new JComboBox<MicroscopeConnection>(microscopeConnections);
		connectionTypeLabel.setOpaque(false);
		connectionTypeField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				connectionTypeChanged((MicroscopeConnection)connectionTypeField.getSelectedItem());

			}
		});
		addConfElement(connectionTypeLabel, layout, newLineConstr, contentPanel);
		connectionTypeField.setOpaque(false);
		addConfElement(connectionTypeField, layout, newLineConstr, contentPanel);

		folderLabel = new JLabel("Driver Folder Path:");
		addConfElement(folderLabel, layout, newLineConstr, contentPanel);
		folderPanel = new JPanel(new BorderLayout(5, 0));
		folderPanel.setOpaque(false);
		if(lastDriverPath != null)
			driverPathField.setText(lastDriverPath);
		folderPanel.add(driverPathField, BorderLayout.CENTER);
		JButton openFolderChooser = new JButton("Edit");
		openFolderChooser.setOpaque(false);
		openFolderChooser.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				JFileChooser fileChooser = new JFileChooser(driverPathField.getText());
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fileChooser.showDialog(MicroscopeConnectionTypeChooser.this, "Select");

				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					driverPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		folderPanel.add(openFolderChooser, BorderLayout.EAST);
		addConfElement(folderPanel, layout, newLineConstr, contentPanel);

		getContentPane().add(contentPanel, BorderLayout.CENTER);
		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		buttonsPanel.setOpaque(true);
		buttonsPanel.setBackground(Color.WHITE);
		connectButton = new JButton("Connect");
		connectButton.setOpaque(false);
		connectButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				synchronized(MicroscopeConnectionTypeChooser.this)
				{
					userInputFinished = true;
					MicroscopeConnectionTypeChooser.this.notifyAll();
				}
				MicroscopeConnectionTypeChooser.this.dispose();
			}
		});
		buttonsPanel.add(connectButton);
		JButton cancelButton = new JButton("Exit");
		cancelButton.setOpaque(false);
		cancelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});
		buttonsPanel.add(cancelButton);

		buttonsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

		pack();
		// Get the size of the default screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
		getRootPane().setDefaultButton(connectButton);

		// Set first element as selected
		if(lastConnectionType != null)
		{
			for(MicroscopeConnection connection : microscopeConnections)
			{
				if(connection.connectionID.equals(lastConnectionType))
				{
					connectionTypeField.setSelectedItem(connection);
				}
			}
		}
		else if(connectionTypeField.getItemCount() > 0)
			connectionTypeField.setSelectedIndex(0);
		connectionTypeChanged((MicroscopeConnection)connectionTypeField.getSelectedItem());

		setVisible(true);
	}

	private void connectionTypeChanged(MicroscopeConnection connection)
	{
		if(connection == null)
		{
			connectButton.setEnabled(false);
			folderPanel.setVisible(false);
			folderLabel.setVisible(false);
			explanationArea.setText("<html><p style=\"font-size:small;margin-top:0px\"><b>Select connection type!</b></p><p style=\"font-size:small\">If not connection types are available, please visit http://www.youscope.org to download some.</p>");
		}
		else
		{
			connectButton.setEnabled(true);
			if(!connection.factory.needsDriverPath(connection.connectionID))
			{
				folderLabel.setVisible(false);
				folderPanel.setVisible(false);
			}
			else
			{
				folderLabel.setVisible(true);
				folderPanel.setVisible(true);
			}
			String message = "<html><p style=\"font-size:small;margin-top:0px;\"><b>Selected Connection Type:</b><br />" + connection.factory.getShortMicroscopeConnectionDescription(connection.connectionID) + "</p>";
			message += "<p style=\"font-size:small;text-align:justify;margin-top:8px;\"><b>Description / Instructions:</b><br />" + connection.factory.getMicroscopeConnectionDescription(connection.connectionID).replaceAll("\n", "<br />") + "</p></html>";
			explanationArea.setText(message);
		}
		pack();
		explanationArea.scrollRectToVisible(new Rectangle(1, 1, 10, 10));
	}

	public String getDriverPath()
	{
		return driverPathField.getText();
	}

	public String getConnectionType()
	{
		return ((MicroscopeConnection)connectionTypeField.getSelectedItem()).connectionID;
	}

	public synchronized void waitForUserInput()
	{
		userInputFinished = false;
		while(!userInputFinished)
		{
			try
			{
				wait();
			}
			catch(@SuppressWarnings("unused") InterruptedException e)
			{
				// Do nothing
			}
		}
	}

	private static void addConfElement(Component component, GridBagLayout layout, GridBagConstraints constr, Container panel)
	{
		layout.setConstraints(component, constr);
		panel.add(component);
	}
}
