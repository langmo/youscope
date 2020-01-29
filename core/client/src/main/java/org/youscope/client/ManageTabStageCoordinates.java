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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.youscope.common.microscope.StageDevice;
import org.youscope.uielements.DescriptionPanel;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class ManageTabStageCoordinates extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -2558452206635225814L;

	private final GridBagConstraints	newLineConstr = StandardFormats.getNewLineConstraint();
	private final GridBagConstraints	bottomConstr = StandardFormats.getBottomContstraint();
	
	private final JLabel stageChooserLabel = new JLabel("Select stage:");
	private final JComboBox<String> stageChooser = new JComboBox<String>();
	private final JComboBox<String> xIncreasesField = new JComboBox<String>(new String[]{"Not transpose x-value.", "Transpose x-value."});
	private final JComboBox<String> yIncreasesField = new JComboBox<String>(new String[]{"Not transpose y-value.", "Transpose y-value."});
	private final JFormattedTextField	unitField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private String lastStage = null;
	private boolean loadingData = false;
	private boolean somethingChanged = false;
	ManageTabStageCoordinates()
	{
		setLayout(new BorderLayout(5, 5));
		setOpaque(false);
		
		// Configuration elements
		GridBagLayout elementsLayout = new GridBagLayout();
		JPanel elementsPanel = new JPanel(elementsLayout);
		elementsPanel.setOpaque(false);
		elementsPanel.setBorder(new TitledBorder("Setup of Stage Coordinate System"));
		xIncreasesField.setOpaque(false);
		yIncreasesField.setOpaque(false);
		stageChooser.setOpaque(false);
		stageChooser.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent arg0)
				{
					if(arg0.getStateChange() == ItemEvent.DESELECTED)
					{
						// Do nothing.
					}
					else
					{
						selectStage(arg0.getItem().toString());
					}
				}
			});
		
		unitField.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!loadingData && lastStage != null)
				{
					somethingChanged = true;
					try
					{
						StageDevice stageDevice = YouScopeClientImpl.getMicroscope().getStageDevice(lastStage);
						stageDevice.setUnitMagnifier(((Number) unitField.getValue()).doubleValue());
					}
					catch(Exception e1)
					{
						ClientSystem.err.println("Could not store coordinate system for stage " + lastStage + ".", e1);
					}
					
				}
			}
			
		});
		xIncreasesField.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!loadingData && lastStage != null)
				{
					somethingChanged = true;
					try
					{
						StageDevice stageDevice = YouScopeClientImpl.getMicroscope().getStageDevice(lastStage);
						stageDevice.setTransposeX(xIncreasesField.getSelectedIndex() == 1);
					}
					catch(Exception e1)
					{
						ClientSystem.err.println("Could not store coordinate system for stage " + lastStage + ".", e1);
					}
					
				}
			}
			
		});
		yIncreasesField.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!loadingData && lastStage != null)
				{
					somethingChanged = true;
					try
					{
						StageDevice stageDevice = YouScopeClientImpl.getMicroscope().getStageDevice(lastStage);
						stageDevice.setTransposeY(yIncreasesField.getSelectedIndex() == 1);
					}
					catch(Exception e1)
					{
						ClientSystem.err.println("Could not store coordinate system for stage " + lastStage + ".", e1);
					}
					
				}
			}
			
		});

		StandardFormats.addGridBagElement(stageChooserLabel, elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(stageChooser, elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(new JLabel("Direction of the x-coordinate:"), elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(xIncreasesField, elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(new JLabel("Direction of the y-coordinate:"), elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(yIncreasesField, elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(new JLabel("<html>Not all stages measure distances in um.<br/>Enter here how many um correspond to<br />one microscope specific unit (1 unit = X microns).<br />Values are typically simple rational numbers,<br />most often ­but not always­ the number 1.0:</html>"),	elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(unitField, elementsLayout, newLineConstr, elementsPanel);
		JPanel emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		StandardFormats.addGridBagElement(emptyPanel, elementsLayout, bottomConstr, elementsPanel);
		
		// Descriptive image
		Image coordinateIcon = ImageLoadingTools.getResourceImage("org/youscope/client/images/right-hand-rule.jpg", "Right Hand Rule");
		if(coordinateIcon != null)
		{
			class ImageField extends JComponent
		    {
		        /**
		         * Serial Version UID.
		         */
		        private static final long serialVersionUID = 3857578873912009521L;
		        private final Image image;
		        public ImageField(Image image)
		        {
		        	this.image  = image;
		        	setBorder(new LineBorder(Color.BLACK, 1));
		        	setOpaque(false);
		        }
		        @Override
		        public Dimension getPreferredSize()
		        {
		        	final int prefWidth = 80;
		        	return new Dimension(prefWidth, prefWidth/image.getWidth(this) * image.getHeight(this));
		        }
		        @Override
		        public synchronized void paintComponent(Graphics grp)
		        {
		            double imageWidth = image.getWidth(this);
		            double imageHeight = image.getHeight(this);
		            if (getWidth() / imageWidth > getHeight() / imageHeight)
		            {
		                imageWidth = imageWidth * getHeight() / imageHeight;
		                imageHeight = getHeight();
		            } else
		            {
		                imageHeight = imageHeight * getWidth() / imageWidth;
		                imageWidth = getWidth();
		            }

		            // draw the image
		            grp.drawImage(image, (int) (getWidth() - imageWidth) / 2,
		                    (int) (getHeight() - imageHeight) / 2, (int) imageWidth, (int) imageHeight,
		                    this);
		        }
		    }
			
			//coordinateIcon.setImage(coordinateIcon.getImage().getScaledInstance(300,-1, Image.SCALE_DEFAULT));
			JPanel imagePanel = new JPanel(new BorderLayout());
			imagePanel.setOpaque(true);
			imagePanel.setBackground(Color.WHITE);
			JEditorPane legendPane = new JEditorPane();
			legendPane.setContentType("text/html");
			legendPane.setText("<html><p style=\"font-size:small\"><b>Figure 1:</b> Visualization of typical stage coordinate systems. The YouScope coordinate system is defined by the increasing numbers of the microplate wells. Thus, the upper native stage coordinate system (e.g. Nikon) requires to transpose the x-direction, the lower one (e.g. Leica) to transpose the y-direction.</p></html>");
			legendPane.setEditable(false);
			imagePanel.add(new ImageField(coordinateIcon), BorderLayout.CENTER);
			imagePanel.add(legendPane, BorderLayout.SOUTH);
			
			DescriptionPanel descriptionPanel = new DescriptionPanel("Description", "Depending on the manufacturer, the coordinate system of and seldomly also the units used by stages can be different (see Figure 1).\nYouScope needs to know the coordinate system of the stage to e.g. iterate through the wells of a microplate appropriately.\n"
					+ "To get to know which coordinate system your stage is using, start the \"Stage and Focus Position\" tool of YouScope. Both, the X and the Y position of the stage should increase when moving the stage e.g. from well A1 to B2 of a microplate. If this is not the case, transpose the respective coordinate(s).\n"
					+ "Please re-check if the change to the coordinate system was correct by again moving the stage from well A1 to B2. If correct, the coordinates should now increase.");
			
			JScrollPane scrollPane = new JScrollPane(descriptionPanel);
			scrollPane.setPreferredSize(new Dimension(400, 150));
			add(scrollPane, BorderLayout.NORTH);
			add(imagePanel, BorderLayout.CENTER);
			add(elementsPanel, BorderLayout.EAST);
		}
	}
	
	private synchronized void selectStage(String stage)
	{
		loadingData = true;
		lastStage = stage;
		if(stage != null)
		{
			xIncreasesField.setEnabled(true);
			yIncreasesField.setEnabled(true);
			unitField.setEditable(true);
			
			try
			{
				StageDevice stageDevice = YouScopeClientImpl.getMicroscope().getStageDevice(stage);
				xIncreasesField.setSelectedIndex(stageDevice.isTransposeX() ? 1 : 0);
				yIncreasesField.setSelectedIndex(stageDevice.isTransposeY() ? 1 : 0);
				unitField.setValue(stageDevice.getUnitMagnifier());
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not load coordinate system for stage " + lastStage + ".", e);
			}
		}
		else
		{
			xIncreasesField.setEnabled(false);
			yIncreasesField.setEnabled(false);
			unitField.setEditable(false);
		}
		loadingData = false;
	}
	@Override
	public synchronized void initializeContent()
	{
		lastStage = null;
		String[] names;
		try
		{
			StageDevice[] stages = YouScopeClientImpl.getMicroscope().getStageDevices();
			names = new String[stages.length];
			for(int i=0; i<stages.length; i++)
			{
				names[i] = stages[i].getDeviceID();
			}
		}
		catch (Exception e2)
		{
			ClientSystem.err.println("Could not get installed stages.", e2);
			names = new String[0];
		}
		stageChooser.removeAllItems();
		for(String name : names)
			stageChooser.addItem(name);
		
		if(stageChooser.getItemCount() > 0)
			stageChooser.setSelectedIndex(0);
	}

	@Override
	public boolean storeContent()
	{
		return somethingChanged;
	}
}
