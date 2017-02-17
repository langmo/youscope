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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;

import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 */
class ServerChooserFrame extends JFrame
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 4159454766470673920L;

	
    private JFormattedTextField urlField;

    private JFormattedTextField portField = new JFormattedTextField(
            StandardFormats.getIntegerFormat());

    private volatile String serverUrl = null;

    private JPasswordField passwordField = new JPasswordField();

    ServerChooserFrame(String lastIP, int lastPort, String lastErrorMessage)
    {
        super("Choose Server");

        setResizable(false);
        setLayout(new BorderLayout(5, 5));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set to last URL
        try
        {
            MaskFormatter mfMyFormatter = new MaskFormatter("###.###.###.###");
            mfMyFormatter.setPlaceholderCharacter('0');

            urlField = new JFormattedTextField(mfMyFormatter);
            urlField.setValue("127.000.000.001");
        } catch (@SuppressWarnings("unused") ParseException e)
        {
            urlField = new JFormattedTextField("127.000.000.001");
        }
        urlField.setValue(lastIP);
        portField.setValue(lastPort);        
        
        // Set tray icon image.
		final String TRAY_ICON_URL16 = "org/youscope/client/images/icon-16.png";
		final String TRAY_ICON_URL32 = "org/youscope/client/images/icon-32.png";
		final String TRAY_ICON_URL96 = "org/youscope/client/images/icon-96.png";
		final String TRAY_ICON_URL194 = "org/youscope/client/images/icon-194.png";
		ArrayList<Image> trayIcons = new ArrayList<Image>(4);
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

        GridBagLayout elementsLayout = new GridBagLayout();
        JPanel elementsPanel = new JPanel(elementsLayout);
        GridBagConstraints newLineConstr = new GridBagConstraints();
        newLineConstr.fill = GridBagConstraints.HORIZONTAL;
        newLineConstr.gridwidth = GridBagConstraints.REMAINDER;
        newLineConstr.anchor = GridBagConstraints.NORTHWEST;
        newLineConstr.gridx = 0;
        newLineConstr.weightx = 1.0;
        GridBagConstraints newLineConstrMargin = (GridBagConstraints) newLineConstr.clone();
        newLineConstrMargin.insets = new Insets(5, 5, 5, 5);

        if (lastErrorMessage != null)
        {
            // Display a message why last input was incorrect
            JTextArea errorArea = new JTextArea();
            errorArea.setEditable(false);
            errorArea.setText(lastErrorMessage);
            errorArea.setForeground(Color.RED);
            errorArea.setBorder(new LineBorder(Color.RED));
            addConfElement(errorArea, elementsLayout, newLineConstrMargin, elementsPanel);
        }

        // Panel to choose config file
        JTextArea explanationArea = new JTextArea();
        explanationArea.setEditable(false);
        explanationArea
                .setText("Please choose the IP and the port where the server is accessable.\n"
                        + "Please make sure that the port can be accessed, e.g. configure your firewall probably.\n"
                        + "The standard port for this program is "
                        + Integer.toString(YouScopeClientImpl.REGISTRY_PORT) + ".\n"
                        + "If the server is on this computer, its IP is 127.000.000.001.");
        explanationArea.setBorder(new LineBorder(Color.BLACK));
        addConfElement(explanationArea, elementsLayout, newLineConstrMargin, elementsPanel);

        GridBagLayout serverURLLayout = new GridBagLayout();
        JPanel serverURLPanel = new JPanel(serverURLLayout);
        serverURLPanel.setBorder(new TitledBorder("Server Connection Details"));
        addConfElement(new JLabel("Server IP:"), serverURLLayout, newLineConstr, serverURLPanel);
        addConfElement(urlField, serverURLLayout, newLineConstr, serverURLPanel);
        addConfElement(new JLabel("Server Port:"), serverURLLayout, newLineConstr, serverURLPanel);
        addConfElement(portField, serverURLLayout, newLineConstr, serverURLPanel);
        addConfElement(new JLabel("Password:"), serverURLLayout, newLineConstr, serverURLPanel);
        addConfElement(passwordField, serverURLLayout, newLineConstr, serverURLPanel);
        addConfElement(serverURLPanel, elementsLayout, newLineConstrMargin, elementsPanel);

        // elementsPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
        add(elementsPanel, BorderLayout.CENTER);

        JButton connectButton = new JButton("Connect");
        getRootPane().setDefaultButton(connectButton);
        ActionListener connectListener = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    serverUrl = (String) urlField.getValue();
                    synchronized (ServerChooserFrame.this)
                    {
                        ServerChooserFrame.this.notifyAll();
                    }
                }
            };
        connectButton.addActionListener(connectListener);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    System.exit(0);
                }
            });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        buttonPanel.add(connectButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
        add(buttonPanel, BorderLayout.SOUTH);

        addWindowFocusListener(new WindowAdapter() 
        {
            @Override
			public void windowGainedFocus(WindowEvent e) 
            {
            	passwordField.requestFocusInWindow();
            }
        });
        
        // Init Layout.
        pack();
        // Get the size of the default screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
    }

    protected static void addConfElement(Component component, GridBagLayout layout,
            GridBagConstraints constr, JPanel panel)
    {
        layout.setConstraints(component, constr);
        panel.add(component);
    }

    // This function shows an URL chooser and returns the chosen URL.
    public void selectURL()
    {
        // Show a server chooser window.
        setVisible(true);

        // Wait until user pressed OK.
        synchronized (this)
        {
            while (serverUrl == null)
            {
                try
                {
                    wait();
                } catch (@SuppressWarnings("unused") InterruptedException e)
                {
                    return;
                }
            }
        }
        dispose();
    }

    public int getPort()
    {
        return ((Number) portField.getValue()).intValue();
    }

    public String getPassword()
    {
        return new String(passwordField.getPassword());
    }

    public String getURL()
    {
        return serverUrl;
    }
}
