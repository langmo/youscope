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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import org.youscope.addon.component.ComponentCreationException;
import org.youscope.client.MeasurementControl.MeasurementControlListener;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.uielements.ImageLoadingTools;

/**
 * @author langmo
 *
 */
class MeasurementControlManager extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 4104521637298528940L;
	private final JDesktopPane desktop;
	private boolean rightPanelShown = false;
	private JTabbedPane tabbedPane = new JTabbedPane();
	private final Image closeImage;
	public MeasurementControlManager(JDesktopPane desktop)
	{
		this.desktop = desktop;
		setLayout(new BorderLayout());
		add(desktop, BorderLayout.CENTER);
		closeImage = ImageLoadingTools.getResourceImage("icons/cross-button.png", "close measurement");
	}
	private void updateRightPanel()
	{
		if(tabbedPane.getTabCount() > 0)
			setShowRightPanel(true);
		else
			setShowRightPanel(false);
	}
	private void setShowRightPanel(boolean show)
	{
		if(show == rightPanelShown)
			return;
		rightPanelShown = show;
		
		removeAll();
		if(rightPanelShown)
		{
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, desktop, tabbedPane);
			splitPane.setBorder(new EmptyBorder(0,0,0,0));
			add(splitPane, BorderLayout.CENTER);
			
			splitPane.setResizeWeight(1.0);
		}
		else
		{
			add(desktop, BorderLayout.CENTER);
		}
		revalidate();
	}
	private class ButtonTabComponent extends JPanel 
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -3637602832226279650L;
		private JButton button;
		public ButtonTabComponent() 
		{
	        super(new BorderLayout(3, 2));
	        setOpaque(false);
	        JLabel label = new JLabel() 
	        {
	            /**
				 * Serial Version UID.
				 */
				private static final long	serialVersionUID	= 7412210442130056190L;

				@Override
				public String getText() 
	            {
	                int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
	                if (i != -1) 
	                {
	                    return tabbedPane.getTitleAt(i);
	                }
	                return null;
	            }
	        };
	        
	        add(label, BorderLayout.CENTER);
	        
	        //tab button
	        button = new TabButton();
	        add(button, BorderLayout.EAST);
	        //add more space to the top of the component
	        //setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	    }
		void addActionListener(ActionListener listener)
		{
			button.addActionListener(listener);
		}

	}
	private class TabButton extends JButton
	{
        /**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -5521205036123484263L;

		public TabButton() 
        {
            int size = 16;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("Close measurement");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
        }

         //we don't want to update UI for this button
        @Override
		public void updateUI() 
        {
        	// do nothing.
        }

        //paint the cross
        @Override
		protected void paintComponent(Graphics g) 
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) 
            {
                g2.translate(1, 1);
            }
            if(closeImage != null)
            	g2.drawImage(closeImage, 0, 0, this);
            else
            {
            	g2.setColor(Color.RED);
            	g2.drawString("X", 0, 16);
            }
            
            g2.dispose();
        }
    }
	private final static MouseListener buttonMouseListener = new MouseAdapter() {
        @Override
		public void mouseEntered(MouseEvent e) 
        {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) 
            {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        @Override
		public void mouseExited(MouseEvent e) 
        {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) 
            {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };


	private class MeasurementControlHolder implements MeasurementControlListener
	{
		private final MeasurementControl control;
		private YouScopeFrame frame = null;
		private JPanel tabContainer = null;
		public MeasurementControlHolder(Measurement measurement) throws RemoteException
		{
			control = new MeasurementControl(this, measurement);
			if((Boolean) PropertyProviderImpl.getInstance().getProperty(StandardProperty.PROPERTY_DOCK_MEASUREMENT_CONTROL))
			{
				setToDocked();
			}
			else
			{
				setToStandAloneFrame();
			}
		}
		@Override
		public void measurementControlClosed()
		{
			if(frame != null)
			{
				frame.setVisible(false);
				frame = null;
			}
			if(tabContainer != null)
			{
				// Remove tab of measurement.
				tabbedPane.remove(tabContainer);
				tabContainer = null;
				updateRightPanel();
			}
		}
		@Override
		public void dockWindow()
		{
			setToDocked();
			
		}
		@Override
		public void undockWindow()
		{
			setToStandAloneFrame();
			
		}
		private void setToStandAloneFrame()
		{
			if(frame != null)
				return;
			if(tabContainer != null)
			{
				// Remove tab of measurement.
				tabbedPane.remove(tabContainer);
				tabContainer = null;
				updateRightPanel();
			}
			frame = YouScopeFrameImpl.createTopLevelFrame();
			frame.setTitle(control.getName() + " - Measurement Control");
			frame.setResizable(true);
			frame.setClosable(true);
			frame.setMaximizable(false);
			frame.setSize(new Dimension(600, 400));
			
			JPanel contentPane = new JPanel(new BorderLayout());
			control.initializeWideLayout(contentPane);
			frame.setContentPane(contentPane);
			frame.addFrameListener(new YouScopeFrameListener()
				{
					@Override
					public void frameClosed()
					{
						if(frame != null)
							control.setClosed();
					}
		
					@Override
					public void frameOpened()
					{
						// Do nothing.
					}
				});
			frame.setVisible(true);
		}
		private void setToDocked()
		{
			if(tabContainer != null)
				return;
			if(frame != null)
			{
				// Close frame
				YouScopeFrame tempFrame = frame;
				frame = null;
				tempFrame.setVisible(false);
			}
			
			tabContainer = new JPanel();
			tabContainer.setOpaque(false);
			control.initializeTightLayout(tabContainer);
			tabbedPane.addTab(control.getName(), tabContainer);
			tabbedPane.setSelectedComponent(tabContainer);
			ButtonTabComponent buttonTabElement = new ButtonTabComponent();
			buttonTabElement.addActionListener(new ActionListener()
				{

					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						control.setClosed();
					}
				
				});
			tabbedPane.setTabComponentAt(tabbedPane.indexOfComponent(tabContainer), buttonTabElement);

			updateRightPanel();
		}
	}
	
	public void addMeasurement(Measurement measurement) throws RemoteException
	{
		@SuppressWarnings("unused")
		MeasurementControlHolder measurementControlHolder = new MeasurementControlHolder(measurement);
	}
	
	public Measurement addMeasurement(MeasurementConfiguration configuration) throws RemoteException, ConfigurationException, ComponentCreationException
	{
		Measurement measurement = YouScopeClientImpl.getServer().getMeasurementProvider().createMeasurement(configuration, new CallbackProviderImpl(new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer()));
		addMeasurement(measurement);
		return measurement;
	}
	
	
}
