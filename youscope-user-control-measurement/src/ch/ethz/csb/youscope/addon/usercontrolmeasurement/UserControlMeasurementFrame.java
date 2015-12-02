/**
 * 
 */
package ch.ethz.csb.youscope.addon.usercontrolmeasurement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.rmi.RemoteException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.uielements.ContinousMeasurementPanel;
import ch.ethz.csb.youscope.client.uielements.HistogramPlot;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.ImageEvent;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.microscope.Channel;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.tools.ImageConvertException;
import ch.ethz.csb.youscope.shared.tools.ImageTools;

/**
 * @author Moritz Lang
 *
 */
class UserControlMeasurementFrame extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID		= -1499077134453605763L;

	private static double				exposure				= 20.0;

	private static boolean			increaseContrast		= true;
	private static boolean			autoContrast			= true;

	private static String				lastChannelGroup			= "";
	private static String				lastChannel				= "";

	private JComboBox<String>					channelGroupField		= new JComboBox<String>();

	private JComboBox<String>					channelField			= new JComboBox<String>();

	private JFormattedTextField		exposureField			= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private ContinousMeasurementPanel	mainPanel;

	private JCheckBox					increaseContrastField	= new JCheckBox("Increase Contrast.", increaseContrast);
	private JCheckBox					autoContrastField		= new JCheckBox("Auto-Adjust Contrast.", autoContrast);

	private HistogramPlot				histogram				= new HistogramPlot();

	private final YouScopeClient	client;
	private final YouScopeServer				server;

	private final UserControlMeasurementCallbackImpl callback;
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @param callback the callback to contol.
	 * @throws RemoteException
	 * @throws InterruptedException
	 * @throws MicroscopeException
	 */
	public UserControlMeasurementFrame(YouScopeClient client, YouScopeServer server, UserControlMeasurementCallbackImpl callback) throws RemoteException, InterruptedException, MicroscopeException
	{
		this.client = client;
		this.server = server;
		this.callback = callback;

		// Get button images
		String rotateClockwiseFile = "icons/arrow-circle-225-left.png";
		String rotateCounterClockwiseFile = "icons/arrow-circle.png";
		String flipHorizontalFile = "icons/arrow-continue-000-top.png";
		String flipVerticalFile = "icons/arrow-continue-090.png";

		ImageIcon rotateClockwiseIcon = null;
		ImageIcon rotateCounterClockwiseIcon = null;
		ImageIcon flipHorizontalIcon = null;
		ImageIcon flipVerticalIcon = null;

		try
		{
			URL rotateClockwiseURL = getClass().getClassLoader().getResource(rotateClockwiseFile);
			if(rotateClockwiseURL != null)
				rotateClockwiseIcon = new ImageIcon(rotateClockwiseURL, "Rotate Clockwise");
			URL rotateCounterClockwiseURL = getClass().getClassLoader().getResource(rotateCounterClockwiseFile);
			if(rotateCounterClockwiseURL != null)
				rotateCounterClockwiseIcon = new ImageIcon(rotateCounterClockwiseURL, "Rotate Counter Clockwise");

			URL flipHorizontalURL = getClass().getClassLoader().getResource(flipHorizontalFile);
			if(flipHorizontalURL != null)
				flipHorizontalIcon = new ImageIcon(flipHorizontalURL, "Flip Horizontal");
			URL flipVerticalURL = getClass().getClassLoader().getResource(flipVerticalFile);
			if(flipVerticalURL != null)
				flipVerticalIcon = new ImageIcon(flipVerticalURL, "Flip Vertical");
		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			// Do nothing.
		}

		setLayout(new BorderLayout());

		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();

		GridBagConstraints newLineNotFillConstr = new GridBagConstraints();
		newLineNotFillConstr.gridwidth = GridBagConstraints.REMAINDER;
		newLineNotFillConstr.anchor = GridBagConstraints.NORTHWEST;
		newLineNotFillConstr.gridx = 0;
		newLineNotFillConstr.weightx = 0;

		GridBagLayout settingsLayout = new GridBagLayout();
		JPanel settingsPanel = new JPanel(settingsLayout);

		// Imaging Panel
		GridBagLayout imagingSettingsLayout = new GridBagLayout();
		JPanel imagingSettingsPanel = new JPanel(imagingSettingsLayout);
		imagingSettingsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Imaging"));
		StandardFormats.addGridBagElement(new JLabel("Channel Group:"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(channelGroupField, imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(new JLabel("Channel:"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(channelField, imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(new JLabel("Exposure (ms):"), imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(exposureField, imagingSettingsLayout, newLineConstr, imagingSettingsPanel);
		StandardFormats.addGridBagElement(imagingSettingsPanel, settingsLayout, newLineConstr, settingsPanel);

		// Contrast Panel
		GridBagLayout contrastSettingsLayout = new GridBagLayout();
		JPanel contrastSettingsPanel = new JPanel(contrastSettingsLayout);
		contrastSettingsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Contrast"));
		StandardFormats.addGridBagElement(increaseContrastField, contrastSettingsLayout, newLineConstr, contrastSettingsPanel);
		StandardFormats.addGridBagElement(autoContrastField, contrastSettingsLayout, newLineConstr, contrastSettingsPanel);
		increaseContrastField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				increaseContrast = increaseContrastField.isSelected();
				mainPanel.setIncreaseContrast(increaseContrast);
				histogram.setAutoAdjusting(increaseContrastField.isSelected() && autoContrastField.isSelected());
				autoContrastField.setVisible(increaseContrastField.isSelected());
			}
		});
		autoContrastField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				histogram.setAutoAdjusting(increaseContrastField.isSelected() && autoContrastField.isSelected());
			}
		});
		StandardFormats.addGridBagElement(histogram, contrastSettingsLayout, newLineConstr, contrastSettingsPanel);
		StandardFormats.addGridBagElement(contrastSettingsPanel, settingsLayout, newLineConstr, settingsPanel);

		// Rotate and Flip Panel
		JPanel rotateAndFlipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		rotateAndFlipPanel.setBorder(new TitledBorder(new EtchedBorder(), "Rotating and Flipping"));
		JButton rotateClockwise;
		if(rotateClockwiseIcon == null)
			rotateClockwise = new JButton("rotate clockwise");
		else
			rotateClockwise = new JButton(rotateClockwiseIcon);
		rotateClockwise.setMargin(new Insets(1, 1, 1, 1));
		rotateClockwise.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				mainPanel.rotateClockwise();
			}

		});
		rotateAndFlipPanel.add(rotateClockwise);
		JButton rotateCounterClockwise;
		if(rotateCounterClockwiseIcon == null)
			rotateCounterClockwise = new JButton("rotate counter clockwise");
		else
			rotateCounterClockwise = new JButton(rotateCounterClockwiseIcon);
		rotateCounterClockwise.setMargin(new Insets(1, 1, 1, 1));
		rotateCounterClockwise.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				mainPanel.rotateCounterClockwise();
			}

		});
		rotateAndFlipPanel.add(rotateCounterClockwise);
		JButton flipHorizontalButton;
		if(flipHorizontalIcon == null)
			flipHorizontalButton = new JButton("flip horizontal");
		else
			flipHorizontalButton = new JButton(flipHorizontalIcon);
		flipHorizontalButton.setMargin(new Insets(1, 1, 1, 1));
		flipHorizontalButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				mainPanel.flipHorizontal();
			}

		});
		rotateAndFlipPanel.add(flipHorizontalButton);
		JButton flipVerticalButton = new JButton("flip vertical");
		if(flipVerticalIcon == null)
			flipVerticalButton = new JButton("flip vertical");
		else
			flipVerticalButton = new JButton(flipVerticalIcon);
		flipVerticalButton.setMargin(new Insets(1, 1, 1, 1));
		flipVerticalButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				mainPanel.flipVertical();
			}

		});
		rotateAndFlipPanel.add(flipVerticalButton);
		StandardFormats.addGridBagElement(rotateAndFlipPanel, settingsLayout, newLineConstr, settingsPanel);

		// Load state
		loadChannelGroupNames();
		channelGroupField.setSelectedItem(lastChannelGroup);
		if(channelGroupField.getSelectedItem() != null)
			lastChannelGroup = channelGroupField.getSelectedItem().toString();
		loadChannels();
		channelField.setSelectedItem(lastChannel);
		if(channelField.getSelectedItem() != null)
			lastChannel = channelField.getSelectedItem().toString();

		exposureField.setValue(exposure);
		
		increaseContrastField.setSelected(increaseContrast);
		autoContrastField.setSelected(autoContrast);
		histogram.setAutoAdjusting(increaseContrast && autoContrast);
		autoContrastField.setVisible(increaseContrast);

		exposureField.addActionListener(new ConfigurationChangeListener());
		channelField.addActionListener(new ConfigurationChangeListener());
		channelGroupField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadChannels();
			}
		});

		// Load main panel
		mainPanel = new ContinousMeasurementPanel(client, server, null, 0, lastChannelGroup, lastChannel, exposure, increaseContrast)
		{

			/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= -1418017134453605763L;

			@Override
			public void newImage(ImageEvent event)
			{
				int[][] bins;
				double[] minMax;
				try
				{
					bins = ImageTools.getHistogram(event, Math.max(histogram.getWidth(), 100));
					histogram.setBins(bins);
					minMax = histogram.getMinMax();
				}
				catch(ImageConvertException e)
				{
					minMax = new double[] {0.0, 1.0};
					UserControlMeasurementFrame.this.client.sendError("Could not generate histogram for image.", e);
				}

				mainPanel.setCutoff((float)minMax[0], (float)minMax[1]);
				super.newImage(event);
			}
		};
		
		JButton snapImageButton = new JButton("Snap Image");
		snapImageButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				UserControlMeasurementFrame.this.callback.sendSnapImage();
			}
		});
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		rightPanel.add(settingsPanel, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPanel, new JScrollPane(rightPanel));
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation((splitPane.getMaximumDividerLocation() - 80));
		splitPane.setResizeWeight(1.0);
		add(splitPane, BorderLayout.CENTER);
		add(snapImageButton, BorderLayout.SOUTH);
	}

	public void newImage(ImageEvent event)
	{
		if(mainPanel != null)
			mainPanel.newImage(event);
	}
	private void loadChannelGroupNames()
	{
		String[] channelGroupNames = null;
		try
		{
			channelGroupNames = server.getMicroscope().getChannelManager().getChannelGroupIDs();
		}
		catch(Exception e)
		{
			client.sendError("Could not obtain config group names.", e);
		}

		if(channelGroupNames == null || channelGroupNames.length <= 0)
		{
			channelGroupNames = new String[] {""};
		}

		channelGroupField.removeAllItems();
		for(String configGroupName : channelGroupNames)
		{
			channelGroupField.addItem(configGroupName);
		}
	}

	private void loadChannels()
	{
		String[] channelNames = null;

		Object selectedGroup = channelGroupField.getSelectedItem();
		if(selectedGroup != null && selectedGroup.toString().length() > 0)
		{
			try
			{
				Channel[] channels = server.getMicroscope().getChannelManager().getChannels(selectedGroup.toString());
				channelNames = new String[channels.length];
				for(int i=0; i<channels.length; i++)
				{
					channelNames[i] = channels[i].getChannelID();
				}
			}
			catch(Exception e)
			{
				client.sendError("Could not obtain channel names of microscope.", e);
			}
		}

		if(channelNames == null || channelNames.length <= 0)
		{
			channelNames = new String[] {""};
		}

		channelField.removeAllItems();
		for(String channelName : channelNames)
		{
			channelField.addItem(channelName);
		}
	}

	private class ConfigurationChangeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			callback.sendChannelSettingsChanged();
		}
	}
	
	public void snappedImage()
	{
		mainPanel.setBackground(Color.BLACK);
		mainPanel.repaint();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(1500);
				}
				catch(@SuppressWarnings("unused") InterruptedException e)
				{
					// do nothing, just UI.
				}
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						mainPanel.setBackground(Color.LIGHT_GRAY);
						mainPanel.repaint();
					}
				});
			}
		}).start();
	}
	
	public String getCurrentChannel() throws RemoteException
	{
		return channelField.getSelectedItem().toString();
	}

	public String getCurrentChannelGroup() throws RemoteException
	{
		return channelGroupField.getSelectedItem().toString();
	}

	public double getCurrentExposure() throws RemoteException
	{
		return ((Number)exposureField.getValue()).doubleValue();
	}
}
