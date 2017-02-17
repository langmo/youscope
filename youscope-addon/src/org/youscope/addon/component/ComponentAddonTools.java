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
package org.youscope.addon.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.util.TextTools;

/**
 * Set of helpful functions to simplify configuration addon creation.
 * @author Moritz Lang
 *
 */
public class ComponentAddonTools {

	/**
	 * Use static methods only
	 */
	private ComponentAddonTools() 
	{
		// static methods only.
	}
	
	/**
	 * Creates a frame containing the error messages of a {@link ConfigurationException}. A ConfigurationException is not thrown to signal an error
	 * in YouScope, but to signal to the user that the current state of a configuration is invalid. Thus, it should not be treated as an error.
	 * This function should only be called when the user actually tried to finish a configuration, or tried to get to
	 * a part of the configuration which cannot be accessed while another part is not corrected.
	 * 
	 * The newly create frame is yet not shown. Call {@link YouScopeFrame#setVisible(boolean)} to show frame. Typically, one also calls
	 * {@link YouScopeFrame#addModalChildFrame(YouScopeFrame)} on the parent frame before, with the returned frame as argument.
	 * @param configurationException The configuration exception which occurred.
	 * @param client The YouScope client
	 * @return The newly created configuration error frame (yet not visible).
	 */
	public static YouScopeFrame displayConfigurationInvalid(ConfigurationException configurationException, YouScopeClient client)
	{
		final YouScopeFrame frame = client.createFrame();
		JPanel errorPanel = new JPanel(new BorderLayout());
		errorPanel.add(new JLabel("<html><b>The current configuration is invalid!</b><br />Correct the following errors before proceeding:</html>"), BorderLayout.NORTH);
		
		JEditorPane errorText = new JEditorPane("text/html", "<html>"+TextTools.toHTML(configurationException.getMessage())+"</html>")
		{
			 /**
			 * Serial Version UID.
			 */
			private static final long serialVersionUID = -1842427972878213691L;

			@Override
			public Dimension getPreferredScrollableViewportSize() {
			        return getPreferredSize();
			    }

			    @Override
				public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			       return 10;
			    }

			    @Override
				public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			        return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
			    }

			    @Override
				public boolean getScrollableTracksViewportWidth() {
			        return true;
			    }

			    @Override
				public boolean getScrollableTracksViewportHeight() {
			        return false;
			    }
		};
		errorText.setEditable(false);
		//errorText.setBackground(Color.WHITE);
		//errorText.setForeground(Color.BLACK);
		errorText.setOpaque(true);
		JScrollPane scrollPane = new JScrollPane(errorText);
		scrollPane.getViewport().setBackground(errorText.getBackground());
		errorPanel.add(scrollPane, BorderLayout.CENTER);
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		});
		errorPanel.add(closeButton, BorderLayout.SOUTH);
		frame.setContentPane(errorPanel);
		frame.setTitle("Invalid Configuration");
		frame.setSize(new Dimension(400,300));
		return frame;
	}

}
