package ch.ethz.csb.youscope.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.shared.YouScopeVersion;

class AboutFrame
{
    /**
     * URL of the image displayed on top of every frame.
     */
    private static final String                 BACKGROUND_URL       = "ch/ethz/csb/youscope/client/images/splash.jpg";
    
    AboutFrame(final YouScopeFrame frame)
    {
    	final YouScopeVersion version = new YouScopeVersion("youscope-client");
        frame.setTitle("YouScope "+ version.getFullVersion());
        frame.setResizable(false);
        frame.setClosable(true);
        frame.setMaximizable(false);
        frame.setMargins(0, 0, 0, 0);

        final Dimension totalSize = new Dimension(600, 361);
        
        final ImageIcon backgroundImage = ImageLoadingTools.getResourceIcon(BACKGROUND_URL, "Logo");        
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
					    	g.drawImage(backgroundImage.getImage(), 0, 0, this);
					}
        	
        		};
        		contentPane.setOpaque(true);
        
        JTextPane  description = new JTextPane();
        
        description.setContentType("text/html");
        description.setText("<html><p style=\"margin:0px;padding:0px;font-weight:bold;\">"
                +"YouScope "+ version.getFullVersion() + "<br />"
                + "Created by Moritz Lang and contributers.<br /><br />"
                + "(c) Copyright Moritz Lang, contributors and others 2009-2015.<br/>"
                + "All rights reserved.<br /><br />"
                + "If you have remarks or questions,<br />"
                + "visit: <a href=\"http://www.youscope.org\">www.youscope.org</a><br />"
                + "or contact: <a href=\"mailto;contact@youscope.org\">contact@youscope.org</a>"
                + "</p></html>");
        
        description.setEditable(false);
        description.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.5f));
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
