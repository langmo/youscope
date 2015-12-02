/**
 * 
 */
package ch.ethz.csb.youscope.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.uielements.DescriptionPanel;
import ch.ethz.csb.youscope.client.uielements.DeviceSettingsPanel;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.microscope.PixelSize;

/**
 * @author Moritz Lang
 *
 */
class ManageTabPixelSize extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1217549458836713214L;
	private final DeviceSettingsPanel deviceSettingsPanel;
	private final JList<String> pixelSizeSettingsField = new JList<String>();
	private final JFormattedTextField pixelSizeField = new JFormattedTextField(StandardFormats.getDoubleFormat());
	private boolean actualizing = false;
	private boolean somethingChanged = false;
	private boolean contentChanged = false;
	
	private String[] pixelSizeSettings = new String[0];
	private String currentPixelSizeSetting = null;
	
	private final JButton addPixelSizeButton;
    private final JButton deletePixelSizeButton;
	
    private final YouScopeFrame frame;
    
    ManageTabPixelSize(YouScopeFrame frame)
	{
    	this.frame = frame;
		pixelSizeSettingsField.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(e.getValueIsAdjusting() || actualizing || pixelSizeSettingsField.getSelectedIndex() < 0)
					return;
				showPixelSize(pixelSizeSettings[pixelSizeSettingsField.getSelectedIndex()]);
			}
		});
		pixelSizeSettingsField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		deviceSettingsPanel = new DeviceSettingsPanel(new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer(), true);
		deviceSettingsPanel.setEditable(false);
		deviceSettingsPanel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					if(actualizing || currentPixelSizeSetting == null)
						return;
					somethingChanged = true;
					contentChanged = true;
				}
			});
		
		// Buttons
		ImageIcon addPixelSizeIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Pixel Size Setting");
        ImageIcon deletePixelSizeIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Delete Pixel Size Setting");
        if (addPixelSizeIcon == null)
            addPixelSizeButton = new JButton("New");
        else
            addPixelSizeButton = new JButton(addPixelSizeIcon);
        addPixelSizeButton.setOpaque(false);
        addPixelSizeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	YouScopeFrame modalFrame = ManageTabPixelSize.this.frame.createModalChildFrame();
                	@SuppressWarnings("unused")
					PixelSizeNamingFrame pixelSizeNamingFrame = new PixelSizeNamingFrame(modalFrame);
                	modalFrame.setVisible(true);
                }
            });
        
        if (deletePixelSizeIcon == null)
            deletePixelSizeButton = new JButton("Delete");
        else
            deletePixelSizeButton = new JButton(deletePixelSizeIcon);
        deletePixelSizeButton.setOpaque(false);
        deletePixelSizeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	int row = pixelSizeSettingsField.getSelectedIndex();
					if(row < 0 || row >= pixelSizeSettings.length)
						return;
					String pixelSizeID = pixelSizeSettings[row];
					int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the pixel size setting " + pixelSizeID + " really be deleted?", "Delete Pixel Size Setting", JOptionPane. YES_NO_OPTION);
            		if(shouldDelete != JOptionPane.YES_OPTION)
            			return;
            		
					try
					{
						YouScopeClientImpl.getMicroscope().getPixelSizeManager().removePixelSize(pixelSizeID);
					}
					catch(Exception e1)
					{
						ClientSystem.err.println("Could not remove pixel size setting " + pixelSizeID + ".", e1);
					}
					currentPixelSizeSetting = null;
					initializeContent();
                }
            });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 2, 2));
        buttonPanel.setOpaque(false);
        for(int i=0; i<3;i++)
        {
        	JPanel emptyPanel = new JPanel();
	        emptyPanel.setOpaque(false);
	        buttonPanel.add(emptyPanel);
        }
        buttonPanel.add(addPixelSizeButton);
        buttonPanel.add(deletePixelSizeButton);
        
		JPanel mainPanel = new JPanel(new GridLayout(1,2,2,2));
		mainPanel.setOpaque(false);
		
		JPanel pixelSizeSelectionPanel = new JPanel(new BorderLayout());
		pixelSizeSelectionPanel.setOpaque(false);
		pixelSizeSelectionPanel.setBorder(new TitledBorder("Step 1: Select Pixel Size Setting"));
		pixelSizeSelectionPanel.add(new JScrollPane(pixelSizeSettingsField), BorderLayout.CENTER);
		pixelSizeSelectionPanel.add(buttonPanel, BorderLayout.SOUTH);
		mainPanel.add(pixelSizeSelectionPanel);
		
		JPanel pixelSizePanel = new JPanel(new GridLayout(1, 2, 2, 2));
		pixelSizePanel.setOpaque(false);
		pixelSizeField.setOpaque(false);
		pixelSizeField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(actualizing)
					return;
				somethingChanged = true;
				contentChanged = true;
			}
		});
		pixelSizePanel.add(new JLabel("Pixel size (μm)"));
		pixelSizePanel.add(pixelSizeField);
		
		DynamicPanel pixelSizeDefinitionPanel = new DynamicPanel();
		pixelSizeDefinitionPanel.setOpaque(false);
		pixelSizeDefinitionPanel.setBorder(new TitledBorder("Step 2: Configure Pixel Size Setting"));
		pixelSizeDefinitionPanel.add(pixelSizePanel);
		pixelSizeDefinitionPanel.add(new JLabel("<html>Device settings which have to be active for the given pixel size to apply:"));
		pixelSizeDefinitionPanel.addFill(deviceSettingsPanel);
		mainPanel.add(pixelSizeDefinitionPanel);
		
		DescriptionPanel descriptionPanel = new DescriptionPanel("For several tasks, like stitching, the size of a pixel in micro-meter must be known by YouScope.\n"
			+ "This size typically depends on certain device settings which change the magnification of the microscope, like the lenses used, and the physical pixel size of the camera (typically 6.45 micro meters).\n"
			+ "The pixel size is then calculated by taking the physical pixel size of the camera, and dividing it through the magnification which is active if the given set of device settings is active.");
		
		setOpaque(false);
		setLayout(new BorderLayout(5, 5));
		add(descriptionPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}
	
	private class PixelSizeNamingFrame
	{
		private final JTextField pixelSizeSettingIDField = new JTextField("");
		private final YouScopeFrame frame;
		PixelSizeNamingFrame(YouScopeFrame frame)
		{
			this.frame = frame;
			frame.setTitle("New Pixel Size Setting");
			frame.setResizable(false);
			frame.setClosable(true);
			frame.setMaximizable(false);
			
			JButton addButton = new JButton("Add Pixel Size Setting");
			addButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						String pixelSizeID = pixelSizeSettingIDField.getText();
						if(pixelSizeID.length() < 1)
						{
							JOptionPane.showMessageDialog(null, "The pixel size ID has to be at least one character long.", "Invalid Pixel Size Setting Name", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						try
						{
							YouScopeClientImpl.getMicroscope().getPixelSizeManager().addPixelSize(pixelSizeID);
						}
						catch(Exception e)
						{
							ClientSystem.err.println("Could not add pixel size setting.", e);
						}
						initializeContent();
						PixelSizeNamingFrame.this.frame.setVisible(false);
					}
				});
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						PixelSizeNamingFrame.this.frame.setVisible(false);
					}
				});
			
			JPanel elementsPanel = new JPanel(new GridLayout(1,2,2,2));
			elementsPanel.add(new JLabel("Pixel Size Setting Name:"));
			elementsPanel.add(pixelSizeSettingIDField);
			
			JPanel buttonsPanel = new JPanel(new GridLayout(1,2,2,2));
			buttonsPanel.add(cancelButton);
			buttonsPanel.add(addButton);
        	
		
			JPanel contentPane = new JPanel(new BorderLayout());
			contentPane.add(elementsPanel, BorderLayout.CENTER);
			contentPane.add(buttonsPanel, BorderLayout.SOUTH);
            frame.setContentPane(contentPane);
            frame.pack();
		}
	}
	private void showPixelSize(String pixelSizeID)
	{
		actualizing = true;
		if(currentPixelSizeSetting != null && somethingChanged)
		{
			try
			{
				PixelSize pixelSize = YouScopeClientImpl.getMicroscope().getPixelSizeManager().getPixelSize(currentPixelSizeSetting);
				pixelSize.setPixelSize(((Number)pixelSizeField.getValue()).doubleValue());
				pixelSize.setPixelSizeSettings(deviceSettingsPanel.getSettings());
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not set pixel size setting " + currentPixelSizeSetting + ".", e);
			}
		}
		somethingChanged = false;
		currentPixelSizeSetting = pixelSizeID;
		deviceSettingsPanel.clear();
		if(pixelSizeID == null)
		{
			pixelSizeField.setEditable(false);
			deviceSettingsPanel.setEditable(false);
		}
		else
		{
			pixelSizeField.setEditable(true);
			deviceSettingsPanel.setEditable(true);
			try
			{
				deviceSettingsPanel.setSettings(YouScopeClientImpl.getMicroscope().getPixelSizeManager().getPixelSize(pixelSizeID).getPixelSizeSettings());
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not get device settings for pixel size setting " + pixelSizeID + ".", e);
			}	
			try
			{
				pixelSizeField.setValue(YouScopeClientImpl.getMicroscope().getPixelSizeManager().getPixelSize(pixelSizeID).getPixelSize());
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not get pixel size for setting " + pixelSizeID + ".", e);
			}	
		}
		actualizing = false;
	}
	
	@Override
	public void initializeContent()
	{
		actualizing = true;
		Vector<String> pixelSizeVector = new Vector<String>();
		try
		{
			for(PixelSize pixelSize : YouScopeClientImpl.getMicroscope().getPixelSizeManager().getPixelSizes())
			{
					pixelSizeVector.addElement(pixelSize.getPixelSizeID());
			}
			
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not obtain pixel size setting IDs.", e);
			pixelSizeVector.clear();
		}
		pixelSizeSettings = pixelSizeVector.toArray(new String[pixelSizeVector.size()]);
		pixelSizeSettingsField.setListData(pixelSizeSettings);
		
		if(pixelSizeSettings.length > 0)
		{
			pixelSizeSettingsField.setSelectedIndex(0);
			showPixelSize(pixelSizeSettings[0]);
		}
		else
		{
			showPixelSize(null);
		}
		actualizing = false;
		
	}
	@Override
	public boolean storeContent()
	{
		showPixelSize(null);
		return contentChanged;
	}
}
