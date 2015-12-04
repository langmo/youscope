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
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.ParseException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;

import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 */
class ServerChooserFrame extends JFrame
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 4159454766470673920L;

    /**
	 * The tray icons.
	 */
	private static final String				TRAY_ICON_URL16						= "org/youscope/client/images/csb-logo-icon16.png";
	private static final String				TRAY_ICON_URL32						= "org/youscope/client/images/csb-logo-icon32.png";
	private static final String				TRAY_ICON_URL64						= "org/youscope/client/images/csb-logo-icon64.png";

	
    protected JFormattedTextField urlField;

    protected JFormattedTextField portField = new JFormattedTextField(
            StandardFormats.getIntegerFormat());

    private volatile String serverUrl = null;

    protected JPasswordField passwordField = new JPasswordField();

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

        // Get images
        String topImageFile = "org/youscope/client/images/csb-logo-long.gif";
        try
        {
            URL topImageURL = getClass().getClassLoader().getResource(topImageFile);
            if (topImageURL != null)
            {
                BufferedImage topImage = ImageIO.read(topImageURL);
                JLabel imageLabel = new JLabel(new ImageIcon(topImage));
                imageLabel.setHorizontalAlignment(SwingConstants.LEFT);
                imageLabel.setBackground(Color.WHITE);
                imageLabel.setOpaque(true);
                add(imageLabel, BorderLayout.NORTH);
            }
        } catch (@SuppressWarnings("unused") Exception e)
        {
            // Do nothing.
        }
        
        // Set tray icon
		Vector<Image> trayIcons = new Vector<Image>();
		ImageIcon trayIcon16 = ImageLoadingTools.getResourceIcon(TRAY_ICON_URL16, "tray icon");
		if(trayIcon16 != null)
			trayIcons.addElement(trayIcon16.getImage());
		ImageIcon trayIcon32 = ImageLoadingTools.getResourceIcon(TRAY_ICON_URL32, "tray icon");
		if(trayIcon32 != null)
			trayIcons.addElement(trayIcon32.getImage());
		ImageIcon trayIcon64 = ImageLoadingTools.getResourceIcon(TRAY_ICON_URL64, "tray icon");
		if(trayIcon32 != null)
			trayIcons.addElement(trayIcon64.getImage());
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
