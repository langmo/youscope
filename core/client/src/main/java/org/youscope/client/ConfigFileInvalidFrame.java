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
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.youscope.clientinterfaces.YouScopeFrame;

/**
 * @author langmo
 *
 */
class ConfigFileInvalidFrame
{
	private final YouScopeFrame frame;
	public ConfigFileInvalidFrame(YouScopeFrame frame, String message)
	{
		this.frame = frame;
		frame.setTitle("Configuration File Compliance");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		// Display a message why last config file was only partly correct
		JEditorPane errorArea = new JEditorPane();
		errorArea.setEditable(false);
		errorArea.setContentType("text/html");

		String errorMessage = "<p style=\"font-size:small;margin-top:0px;margin-bottom:0px\">";
		errorMessage += message.replace("\n", "<br />");
		errorMessage += "</p></html>";
		errorArea.setText(errorMessage);

		JScrollPane errorScrollPane = new JScrollPane(errorArea);
		errorScrollPane.setPreferredSize(new Dimension(450, 115));
		
		JButton editConfigurationButton = new JButton("Edit Configuration");
		editConfigurationButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					ConfigFileInvalidFrame.this.frame.setVisible(false);
					
					YouScopeFrame newFrame = YouScopeFrameImpl.createTopLevelFrame(); 
					@SuppressWarnings("unused")
					ManageFrame manageFrame = new ManageFrame(newFrame);
					newFrame.setVisible(true);
				}
			});
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(new JLabel("Loaded configuration file might only partly support YouScope settings:"), BorderLayout.NORTH);
		contentPane.add(errorScrollPane, BorderLayout.CENTER);
		contentPane.add(editConfigurationButton, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);
		frame.pack();
	}
}
