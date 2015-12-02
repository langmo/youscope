/**
 * 
 */
package ch.ethz.csb.youscope.addon.positioncontrol;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Point2D;
import java.text.ParseException;
import java.util.Formatter;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.YouScopeFrameListener;
import ch.ethz.csb.youscope.client.addon.tool.ToolAddon;
import ch.ethz.csb.youscope.client.uielements.DoubleTextField;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.microscope.Device;

/**
 * @author langmo
 *
 */
class PositionControl implements ToolAddon, Runnable, YouScopeFrameListener
{
	private final YouScopeServer server;
	private final YouScopeClient client;
	private YouScopeFrame						frame;
	
	private final String xPositionText = "Current x position: ";
	private final String yPositionText = "Current y position: ";
	private final String focusPositionText = "Current focus position: ";
	private JLabel xPositionField = new JLabel(xPositionText + "unknown");
	private JLabel yPositionField = new JLabel(yPositionText + "unknown");
	private JLabel focusPositionField = new JLabel(focusPositionText + "unknown");

	private final static double INITIAL_MOVE_STEP_SIZE = 10;
	private final static double INITIAL_FOCUS_STEP_SIZE = 10;
	
	private DoubleTextField 		moveStepField			= new DoubleTextField(INITIAL_MOVE_STEP_SIZE);
	
	private JSlider					moveStepSlider			= new JSlider(0, 50, 10);
	
	private DoubleTextField 		focusingStepField			= new DoubleTextField(INITIAL_FOCUS_STEP_SIZE);
	
	private JSlider					focusingStepSlider		= new JSlider(0, 40, 10);
	
	private JComboBox<String> focusDevicesField = new JComboBox<String>();
	
	private volatile boolean isChangingMove = false;
	
	private volatile boolean isChangingFocus = false;
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 */
	public PositionControl(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setClosable(true);
		frame.setMaximizable(false);
		frame.setResizable(false);
		frame.setTitle("Stage and Focus Position");
		
		frame.startInitializing();
		(new Thread(new FrameInitializer())).start();
	}
	
	private double toSliderUnits(double moveStep)
	{
		return 10.0 * Math.log10(10.0 * moveStep);
	}
	
	private double fromSliderUnits(double sliderStep)
	{
		return 0.1 * Math.pow(10.0, sliderStep / 10.0);
	}
	
	private class FrameInitializer implements Runnable
	{
		@Override
		public void run()
		{
			moveStepSlider.setValue((int)toSliderUnits(INITIAL_MOVE_STEP_SIZE));
			focusingStepSlider.setValue((int)toSliderUnits(INITIAL_FOCUS_STEP_SIZE));
			
			// Grid Bag Layouts
			GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
			GridBagConstraints bottomContstr = StandardFormats.getBottomContstraint();
			GridBagConstraints newLineNotFillConstr = new GridBagConstraints();
			newLineNotFillConstr.gridwidth = GridBagConstraints.REMAINDER;
			newLineNotFillConstr.anchor = GridBagConstraints.NORTHWEST;
			newLineNotFillConstr.gridx = 0;
			newLineNotFillConstr.weightx = 0;
	        			
			// Load icons
			ImageIcon southButtonIcon = ImageLoadingTools.getResourceIcon("bonus/icons-24/arrow-270.png", "Move South");
			ImageIcon westButtonIcon = ImageLoadingTools.getResourceIcon("bonus/icons-24/arrow-180.png", "Move West");
			ImageIcon northButtonIcon = ImageLoadingTools.getResourceIcon("bonus/icons-24/arrow-090.png", "Move North");
			ImageIcon eastButtonIcon = ImageLoadingTools.getResourceIcon("bonus/icons-24/arrow.png", "Move East");
			ImageIcon focusInIcon = ImageLoadingTools.getResourceIcon("icons/arrow-step-out.png", "Zoom In");
			ImageIcon focusOutIcon = ImageLoadingTools.getResourceIcon("icons/arrow-step.png", "Zoom Out");
			
			// Focusing / PFS Panel
	        //GridBagLayout rightLayout = new GridBagLayout();
			//JPanel rightPanel = new JPanel(rightLayout);
			GridBagLayout focusingLayout = new GridBagLayout();
			JPanel focusingPanel = new JPanel(focusingLayout);
			focusingPanel.setBorder(new TitledBorder(new EtchedBorder(), "Focus Position"));
			StandardFormats.addGridBagElement(focusPositionField, focusingLayout, newLineConstr, focusingPanel);
			StandardFormats.addGridBagElement(new JLabel("Focus Device:"), focusingLayout, newLineConstr, focusingPanel);
			StandardFormats.addGridBagElement(focusDevicesField, focusingLayout, newLineConstr,	focusingPanel);
			
			focusingStepSlider.setPaintTicks(true);
			focusingStepSlider.setSnapToTicks(true);
			focusingStepSlider.setMinorTickSpacing(1);
			focusingStepSlider.setMajorTickSpacing(10);
			focusingStepSlider.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent arg0)
				{
					if(isChangingFocus)
						return;
					isChangingFocus = true;
					double focusStep = focusingStepSlider.getValue();
					double focusingStepSize = fromSliderUnits(focusStep);
					if(focusingStepSize > 2)
						focusingStepSize = Math.round(focusingStepSize);
					else
						focusingStepSize = Math.round(10.0 * focusingStepSize) / 10.0;
					focusingStepField.setValue(focusingStepSize);
					isChangingFocus = false;
				}
			});
			
			
			focusingStepField.setMinimalValue(0.0);
			class FocusingStepListener implements ActionListener, FocusListener
			{

				@Override
				public void focusGained(FocusEvent e) {
					// do nothing
					
				}

				@Override
				public void focusLost(FocusEvent e) {
					doCommit();
					
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					doCommit();
					
				}
				
				private void doCommit()
				{
					if(isChangingFocus)
						return;
					try {
						focusingStepField.commitEdit();
					} catch (@SuppressWarnings("unused") ParseException e) {
						// do nothing.
					}
					isChangingFocus = true;
					double focusingStepSize = focusingStepField.getValue();
					focusingStepSlider.setValue((int)toSliderUnits(focusingStepSize));
					isChangingFocus = false;
				}
			}
			FocusingStepListener focusingStepListener = new FocusingStepListener();
			focusingStepField.addActionListener(focusingStepListener);
			focusingStepField.addFocusListener(focusingStepListener);
			
			StandardFormats.addGridBagElement(new JLabel("Focus Step Size:"), focusingLayout, newLineConstr, focusingPanel);
			JPanel focusStepPanel = new JPanel(new BorderLayout());
			focusStepPanel.add(focusingStepField, BorderLayout.CENTER);
			focusStepPanel.add(new JLabel("microns"), BorderLayout.EAST);
			StandardFormats.addGridBagElement(focusingStepSlider, focusingLayout, newLineConstr, focusingPanel);
			StandardFormats.addGridBagElement(focusStepPanel, focusingLayout, newLineConstr, focusingPanel);
			
			StandardFormats.addGridBagElement(new JLabel("Move Focus:"), focusingLayout, newLineConstr, focusingPanel);
			JPanel focusingControlPanel = new JPanel(new FlowLayout(
					FlowLayout.LEFT, 5, 5));
			JButton focusInButton;
			if (focusInIcon == null)
				focusInButton = new JButton("+");
			else
				focusInButton = new JButton(focusInIcon);
			focusInButton.setMargin(new Insets(1, 1, 1, 1));
			focusInButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					changeFocus(focusingStepField.getValue());
				}
			});
			focusingControlPanel.add(focusInButton);
			JButton focusOutButton;
			if (focusOutIcon == null)
				focusOutButton = new JButton("+");
			else
				focusOutButton = new JButton(focusOutIcon);
			focusOutButton.setMargin(new Insets(1, 1, 1, 1));
			focusOutButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					changeFocus(-focusingStepField.getValue());
				}
			});
			focusingControlPanel.add(focusOutButton);
			StandardFormats.addGridBagElement(focusingControlPanel, focusingLayout, newLineConstr, focusingPanel);
			StandardFormats.addGridBagElement(new JPanel(), focusingLayout, bottomContstr, focusingPanel);

			// Position Control Panel
			GridBagLayout positionPanelLayout = new GridBagLayout();
			JPanel positionPanel = new JPanel(positionPanelLayout);
			positionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Stage Position"));
			StandardFormats.addGridBagElement(xPositionField, positionPanelLayout, newLineConstr, positionPanel);
	        StandardFormats.addGridBagElement(yPositionField, positionPanelLayout, newLineConstr, positionPanel);
			StandardFormats.addGridBagElement(new JLabel("Move Step Size:"), positionPanelLayout, newLineConstr, positionPanel);
			moveStepSlider.setPaintTicks(true);
			moveStepSlider.setSnapToTicks(true);
			moveStepSlider.setMinorTickSpacing(1);
			moveStepSlider.setMajorTickSpacing(10);
			moveStepSlider.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent arg0)
				{
					if(isChangingMove)
						return;
					isChangingMove = true;
					double moveStep = moveStepSlider.getValue();
					double moveStepSize = fromSliderUnits(moveStep);//0.1 * Math.pow(10.0, moveStep / 10.0);
					if(moveStepSize > 2)
						moveStepSize = Math.round(moveStepSize);
					else
						moveStepSize = Math.round(10.0 * moveStepSize) / 10.0;
					moveStepField.setValue(moveStepSize);
					isChangingMove = false;
				}
			});
			
			moveStepField.setMinimalValue(0.0);
			class MoveStepListener implements ActionListener, FocusListener
			{

				@Override
				public void focusGained(FocusEvent e) {
					// do nothing
					
				}

				@Override
				public void focusLost(FocusEvent e) {
					doCommit();
					
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					doCommit();
					
				}
				
				private void doCommit()
				{
					if(isChangingMove)
						return;
					try {
						moveStepField.commitEdit();
					} catch (@SuppressWarnings("unused") ParseException e) {
						// do nothing.
					}
					isChangingMove = true;
					double moveStepSize = moveStepField.getValue();
					moveStepSlider.setValue((int)toSliderUnits(moveStepSize));
					isChangingMove = false;
				}
			}
			MoveStepListener moveStepListener = new MoveStepListener();
			moveStepField.addActionListener(moveStepListener);
			moveStepField.addFocusListener(moveStepListener);
			
			JPanel moveStepPanel = new JPanel(new BorderLayout());
			moveStepPanel.add(moveStepField, BorderLayout.CENTER);
			moveStepPanel.add(new JLabel("microns"), BorderLayout.EAST);
			StandardFormats.addGridBagElement(moveStepSlider, positionPanelLayout, newLineConstr, positionPanel);
			StandardFormats.addGridBagElement(moveStepPanel, positionPanelLayout, newLineConstr, positionPanel);

			StandardFormats.addGridBagElement(new JLabel("Move Stage:"), positionPanelLayout, newLineConstr, positionPanel);
			JPanel positionControlPanel = new JPanel(new GridLayout(3, 3));
			positionControlPanel.add(new JPanel());
			JButton northButton;
			if (northButtonIcon == null)
				northButton = new JButton("^");
			else
				northButton = new JButton(northButtonIcon);
			northButton.setMargin(new Insets(1, 1, 1, 1));
			northButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setRelativePosition(0, -moveStepField.getValue());
				}
			});
			positionControlPanel.add(northButton);
			positionControlPanel.add(new JPanel());
			JButton westButton;
			if (westButtonIcon == null)
				westButton = new JButton("<");
			else
				westButton = new JButton(westButtonIcon);
			westButton.setMargin(new Insets(1, 1, 1, 1));
			westButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setRelativePosition(-moveStepField.getValue(), 0);
				}
			});
			positionControlPanel.add(westButton);
			positionControlPanel.add(new JPanel());
			JButton eastButton;
			if (westButtonIcon == null)
				eastButton = new JButton(">");
			else
				eastButton = new JButton(eastButtonIcon);
			eastButton.setMargin(new Insets(1, 1, 1, 1));
			eastButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setRelativePosition(+moveStepField.getValue(), 0);
				}
			});
			positionControlPanel.add(eastButton);
			positionControlPanel.add(new JPanel());
			JButton southButton;
			if (southButtonIcon == null)
				southButton = new JButton("_");
			else
				southButton = new JButton(southButtonIcon);
			southButton.setMargin(new Insets(1, 1, 1, 1));
			southButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setRelativePosition(0, +moveStepField.getValue());
				}
			});
			positionControlPanel.add(southButton);
			positionControlPanel.add(new JPanel());
			StandardFormats.addGridBagElement(positionControlPanel, positionPanelLayout, newLineNotFillConstr, positionPanel);
			StandardFormats.addGridBagElement(new JPanel(), positionPanelLayout, bottomContstr, positionPanel);

			// Close button
	        JButton addJobButton = new JButton("Close");
	        addJobButton.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent e)
	                {
	                	PositionControl.this.frame.setVisible(false);
	                }
	            });
	        
	        // Load focus devices
	        loadFocusDevices();
	        
	        // Querying of microscope for current position
	        frame.addFrameListener(PositionControl.this);
	        if(frame.isVisible())
	        {
	        	Thread thread = new Thread(PositionControl.this);
	        	thread.start();
	        }
	        
	        
	        // Combine all elements
	        JPanel contentPane = new JPanel(new BorderLayout());
	        contentPane.add(positionPanel, BorderLayout.WEST);
	        contentPane.add(focusingPanel, BorderLayout.EAST);
	        contentPane.add(addJobButton, BorderLayout.SOUTH);
			
	        // End initializing
	        frame.setContentPane(contentPane);
	        frame.pack();
			frame.endLoading();
		}
	}
	
	@Override
    public void run()
    {
        while (frame.isVisible())
        {
            try
            {
                Point2D.Double currentPosition = server.getMicroscope().getStageDevice().getPosition();
                Formatter formatter = new Formatter();
                xPositionField.setText(xPositionText + formatter.format("%2.2f um", currentPosition.x));
                formatter.close();
                formatter = new Formatter();
                yPositionField.setText(yPositionText + formatter.format("%2.2f um", currentPosition.y));
                formatter.close();
                
                if (focusDevicesField.getItemCount() > 0)
    			{
    				double focusPosition = server.getMicroscope().getFocusDevice(focusDevicesField.getSelectedItem().toString()).getFocusPosition();
    				formatter = new Formatter();
    				focusPositionField.setText(focusPositionText + formatter.format("%2.2f um", focusPosition));
    				formatter.close();
    			}
                else
                {
                	focusPositionField.setText(focusPositionText + "unknown");
                }
            } 
            catch (Exception e)
            {
                client.sendError("Could not obtain current microscope position. Stopping querring.", e);
                return;
            } 
            try
            {
                Thread.sleep(500);
            } 
            catch (@SuppressWarnings("unused") InterruptedException e)
            {
                return;
            }
        }
    }
	
	@Override
	public void frameClosed()
	{
		// Do nothing.
	}

	@Override
	public void frameOpened()
	{
		Thread thread = new Thread(this, "Current Position Poller");
        thread.start();
	}
	
	private void setRelativePosition(double dx, double dy)
	{
		try
		{
			server.getMicroscope().getStageDevice().setRelativePosition(dx, dy);
		}
		catch (Exception e)
		{
			client.sendError("Could not set stage position", e);
		}
	}

	private void changeFocus(double value)
	{
		try
		{
			if (focusDevicesField.getItemCount() > 0)
			{
				server.getMicroscope().getFocusDevice(focusDevicesField.getSelectedItem().toString()).setRelativeFocusPosition(value);
			}
		}
		catch (Exception e)
		{
			client.sendError("Could not set focus position", e);
		}
	}
	private void loadFocusDevices()
	{
    	String[] focusDevices = null;
    	try
		{
    		Device[] devices =server.getMicroscope().getFocusDevices();
    		focusDevices = new String[devices.length];
    		for(int i=0; i<devices.length; i++)
    		{
    			focusDevices[i] = devices[i].getDeviceID();
    		}
		}
		catch (Exception e)
		{
			client.sendError("Could not obtain focus device names.", e);
			focusDevices = null;
		}
		
		if (focusDevices == null || focusDevices.length <= 0)
        {
			focusDevices = new String[]{""};
        }
		
		focusDevicesField.removeAllItems();
		for(String focusDevice : focusDevices)
		{
			focusDevicesField.addItem(focusDevice);
		}
	}
}
