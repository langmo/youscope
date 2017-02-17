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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.YouScopeVersion;
import org.youscope.uielements.ImageLoadingTools;

class AboutFrame
{
    /**
     * URL of the image displayed on top of every frame.
     */
    private static final String                 BACKGROUND_URL       = "org/youscope/client/images/splash.jpg";
    
    AboutFrame(final YouScopeFrame frame)
    {
        frame.setTitle("YouScope "+ YouScopeVersion.getFullVersion());
        frame.setResizable(false);
        frame.setClosable(true);
        frame.setMaximizable(false);
        frame.setMargins(0, 0, 0, 0);

        final Dimension totalSize = new Dimension(600, 361);
        
        final Image backgroundImage = ImageLoadingTools.getResourceImage(BACKGROUND_URL, "Logo");        
        final JPanel contentPane = new JPanel(null)
        		{

					@Override
					public Dimension getPreferredSize() {
						return totalSize;
					}
					/**
					 * Serial Version UID
					 */
					private static final long serialVersionUID = -1054038275505649084L;
					@Override
					protected void paintComponent(Graphics g) {

					    super.paintComponent(g);
					    if(backgroundImage != null)
					    	g.drawImage(backgroundImage, 0, 0, this);
					}
        	
        		};
		contentPane.setOpaque(true);
        
        JTextPane  description = new JTextPane()
		{
        	/**
			 * Serial Version UID.
			 */
			private static final long serialVersionUID = -4666535620525040910L;

			@Override
			protected void paintComponent(Graphics g) 
        	{
        		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
        		g.fillRect(0, 0, getWidth(), getHeight());
        		super.paintComponent(g);
        	}

		};
        
        description.setContentType("text/html");
        description.setText("<html><p style=\"margin:0px;padding:0px;font-weight:bold;color: #000000;\">"
                +"YouScope "+ YouScopeVersion.getFullVersion() + "<br />"
                + "Created by Moritz Lang and contributers.<br /><br />"
                + "(c) Copyright Moritz Lang, contributors and others 2009-2016.<br/>"
                + "All rights reserved.<br /><br />"
                + "If you have remarks or questions,<br />"
                + "visit: <a href=\"http://www.youscope.org\">www.youscope.org</a><br />"
                + "or contact: <a href=\"mailto;contact@youscope.org\">contact@youscope.org</a>"
                + "</p></html>");
        
        description.setEditable(false);
        description.setOpaque(false);
        contentPane.add(description);
        Dimension preferredSize = description.getPreferredSize();
        description.setBounds((totalSize.width - preferredSize.width )/2, (totalSize.height-preferredSize.height)/2, preferredSize.width, preferredSize.height);
        
        
        JButton okButton = new JButton("Close");
        okButton.addActionListener(new ActionListener()
        {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    frame.setVisible(false);
                }
            });
        okButton.setOpaque(false);
        contentPane.add(okButton);
        preferredSize = okButton.getPreferredSize();
        okButton.setBounds(totalSize.width-preferredSize.width -5, totalSize.height-preferredSize.height -5, preferredSize.width, preferredSize.height);
        
        frame.setContentPane(contentPane);
        frame.pack();
    }
}
