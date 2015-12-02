/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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

/**
 * @author langmo
 */
class ServerPortChooser extends JFrame
{
    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 5679261196754722042L;

    private int port = -1;

    protected JFormattedTextField portField = new JFormattedTextField(
            YouScopeServerImpl.REGISTRY_PORT);

    protected JPasswordField passwordField = new JPasswordField();

    ServerPortChooser(int lastPort, String lastErrorMessage)
    {
        super("Choose server port!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        try
        {
            setAlwaysOnTop(true);
        } catch (@SuppressWarnings("unused") SecurityException e)
        {
            // Do nothing.
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
        newLineConstr.insets = new Insets(5, 5, 5, 5);
        GridBagConstraints newLineConstrDense = (GridBagConstraints) newLineConstr.clone();
        newLineConstrDense.insets = new Insets(0, 0, 0, 0);
        JPanel contentPanel = new JPanel(layout);

        try
        {
            URL topImageURL =
                    getClass().getClassLoader().getResource(
                            "ch/ethz/csb/youscope/client/images/csb-logo-long.gif");
            if (topImageURL != null)
            {
                BufferedImage topImage = ImageIO.read(topImageURL);
                JLabel imageLabel = new JLabel(new ImageIcon(topImage));
                imageLabel.setHorizontalAlignment(SwingConstants.LEFT);
                imageLabel.setBackground(Color.WHITE);
                imageLabel.setOpaque(true);
                getContentPane().add(imageLabel, BorderLayout.NORTH);
            }
        } catch (@SuppressWarnings("unused") Exception e)
        {
            // Do nothing.
        }
        // Set tray icon image.
        final String TRAY_ICON_URL16 = "ch/ethz/csb/youscope/server/images/csb-logo-icon16.png";
        final String TRAY_ICON_URL32 = "ch/ethz/csb/youscope/server/images/csb-logo-icon32.png";
        final String TRAY_ICON_URL64 = "ch/ethz/csb/youscope/server/images/csb-logo-icon64.png";
        URL trayIconURL16 = getClass().getClassLoader().getResource(TRAY_ICON_URL16);
        URL trayIconURL32 = getClass().getClassLoader().getResource(TRAY_ICON_URL32);
        URL trayIconURL64 = getClass().getClassLoader().getResource(TRAY_ICON_URL64);
        Vector<Image> trayIcons = new Vector<Image>();
        if (trayIconURL16 != null)
            trayIcons.addElement((new ImageIcon(trayIconURL16, "tray icon")).getImage());
        if (trayIconURL32 != null)
            trayIcons.addElement((new ImageIcon(trayIconURL32, "tray icon")).getImage());
        if (trayIconURL64 != null)
            trayIcons.addElement((new ImageIcon(trayIconURL64, "tray icon")).getImage());
        if (trayIcons.size() > 0)
            this.setIconImages(trayIcons);

        if (lastErrorMessage != null)
        {
            // Display a message why last input was incorrect
            JTextArea errorArea = new JTextArea();
            errorArea.setEditable(false);
            errorArea.setText(lastErrorMessage);
            errorArea.setForeground(Color.RED);
            errorArea.setBorder(new LineBorder(Color.RED));
            addConfElement(errorArea, layout, newLineConstr, contentPanel);
        }

        // Panel to choose config file
        JTextArea explanationArea = new JTextArea();
        explanationArea.setEditable(false);
        explanationArea
                .setText("Please choose a port where the server should be available to clients.\n"
                        + "The port must be provided by any client connecting to the server.\n"
                        + "Please make sure that the port can be accessed, e.g. configure your firewall probably.\n"
                        + "The standard port for this program is "
                        + Integer.toString(YouScopeServerImpl.REGISTRY_PORT)
                        + ".\n\n"
                        + "Please also set a secure password (>= 8 characters, including numbers and special characters) to prevent\n"
                        + "unauthorizised access to the microscope.");
        explanationArea.setBorder(new LineBorder(Color.BLACK));
        addConfElement(explanationArea, layout, newLineConstr, contentPanel);

        GridBagLayout settingsLayout = new GridBagLayout();
        JPanel settingsPanel = new JPanel(settingsLayout);
        settingsPanel.setBorder(new TitledBorder("Server Settings"));
        addConfElement(new JLabel("Port:"), settingsLayout, newLineConstrDense, settingsPanel);
        portField.setValue(lastPort);
        addConfElement(portField, settingsLayout, newLineConstrDense, settingsPanel);
        addConfElement(new JLabel("Password:"), settingsLayout, newLineConstrDense, settingsPanel);
        addConfElement(passwordField, settingsLayout, newLineConstrDense, settingsPanel);
        addConfElement(settingsPanel, layout, newLineConstr, contentPanel);

        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        JButton startServerButton = new JButton("Start Server");
        ActionListener startServerListener = new ActionListener()
            {
                @Override
				public void actionPerformed(ActionEvent arg0)
                {
                    synchronized (ServerPortChooser.this)
                    {
                        port = ((Number) portField.getValue()).intValue();
                        ServerPortChooser.this.notifyAll();
                    }
                    ServerPortChooser.this.dispose();
                }
            };
        startServerButton.addActionListener(startServerListener);
        getRootPane().setDefaultButton(startServerButton);

        buttonsPanel.add(startServerButton);
        JButton cancelButton = new JButton("Exit");
        cancelButton.addActionListener(new ActionListener()
            {
                @Override
				public void actionPerformed(ActionEvent arg0)
                {
                    System.exit(0);
                }
            });
        buttonsPanel.add(cancelButton);

        buttonsPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        addWindowFocusListener(new WindowAdapter()
            {
                @Override
				public void windowGainedFocus(WindowEvent e)
                {
                    passwordField.requestFocusInWindow();
                }
            });

        pack();
        // Get the size of the default screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);

        setVisible(true);
    }

    protected static void addConfElement(Component component, GridBagLayout layout,
            GridBagConstraints constr, Container panel)
    {
        layout.setConstraints(component, constr);
        panel.add(component);
    }

    int getPort()
    {
        return port;
    }

    String getPassword()
    {
        return new String(passwordField.getPassword());
    }

    synchronized void waitForPort()
    {
        while (getPort() == -1)
        {
            try
            {
                wait();
            } catch (@SuppressWarnings("unused") InterruptedException e)
            {
                // Do nothing
            }
        }
    }
}
