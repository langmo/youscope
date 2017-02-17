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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.microscope.MeasurementProcessingListener;

/**
 * @author langmo
 */
class MeasurementQueueFrame
{
    protected JPanel queueListPanel = new JPanel(new BorderLayout(5, 5));

    protected MeasurementProcessingListener measurementProcessingListener;

    protected YouScopeFrame									frame;
    
    public MeasurementQueueFrame(YouScopeFrame frame)
    {
    	this.frame = frame;
		frame.setTitle("Measurement Manager");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);

        class QueueListener extends UnicastRemoteObject implements MeasurementProcessingListener
        {
            /**
             * Serializable Version UID.
             */
            private static final long serialVersionUID = 7222363361481761850L;

            QueueListener() throws RemoteException
            {
                super();
            }

            @Override
            public void measurementQueueChanged() throws RemoteException
            {
                actualizeQueueList();
            }

            @Override
            public void currentMeasurementChanged() throws RemoteException
            {
                actualizeQueueList();
            }

			@Override
			public void measurementProcessingStopped() throws RemoteException 
			{
				// do nothing.
			}

        }
        try
        {
            measurementProcessingListener = new QueueListener();
        } catch (RemoteException e2)
        {
            ClientSystem.err.println("Could not create microscope queue listener.", e2);
        }

        frame.addFrameListener(new YouScopeFrameListener()
            {
                @Override
                public void frameClosed()
                {
                    try
                    {
                        YouScopeClientImpl.getServer().removeMeasurementProcessingListener(
                                measurementProcessingListener);
                    } catch (RemoteException e)
                    {
                        ClientSystem.err.println("Could not remove microscope queue listener.", e);
                    }
                }

                @Override
                public void frameOpened()
                {
                    try
                    {
                        YouScopeClientImpl.getServer().addMeasurementProcessingListener(measurementProcessingListener);
                    } catch (RemoteException e)
                    {
                        ClientSystem.err.println("Could not add microscope queue listener.", e);
                    }
                    actualizeQueueList();
                    MeasurementQueueFrame.this.frame.pack();
                }
            });

        JPanel elementsPanel = new JPanel(new BorderLayout(5, 5));

        elementsPanel.add(new JLabel("All currently running or queued Measurements."),
                BorderLayout.NORTH);
        queueListPanel.setPreferredSize(new Dimension(500, 300));
        elementsPanel.add(new JScrollPane(queueListPanel), BorderLayout.CENTER);

        JButton addJobButton = new JButton("Close");
        addJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    MeasurementQueueFrame.this.frame.setVisible(false);
                }
            });

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(elementsPanel, BorderLayout.CENTER);
        contentPane.add(addJobButton, BorderLayout.SOUTH);
        frame.setContentPane(contentPane);
        frame.pack();
    }

    protected void actualizeQueueList()
    {
        queueListPanel.removeAll();

        try
        {
            Vector<QueueListElement> listElements = new Vector<QueueListElement>();
            Measurement currentMeasurement =
                    YouScopeClientImpl.getServer().getCurrentMeasurement();
            if (currentMeasurement != null)
                listElements.addElement(new QueueListElement(currentMeasurement, true));
            Measurement[] queueElements =
                    YouScopeClientImpl.getServer().getMeasurementQueue();
            for (Measurement measurement : queueElements)
            {
                if (measurement == null)
                    continue;
                listElements.addElement(new QueueListElement(measurement, false));
            }
            if (listElements.size() > 0)
            {
                JPanel elementsPanel = new JPanel(new GridLayout(listElements.size(), 1, 5, 5));
                for (QueueListElement element : listElements)
                {
                    elementsPanel.add(element);
                }
                queueListPanel.add(elementsPanel, BorderLayout.NORTH);
            } else
                queueListPanel.add(new JLabel("No running or queued measurements.", SwingConstants.CENTER));
            queueListPanel.validate();
            queueListPanel.repaint();
        } catch (RemoteException e)
        {
            ClientSystem.err.println("Could not create List of currently queued measurements.", e);
        }
    }

    protected class QueueListElement extends JPanel
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = 1174327936893842317L;

        protected Measurement measurement;

        private boolean isRunning;

        QueueListElement(Measurement measurement, boolean isRunning) throws RemoteException
        {
            super(new BorderLayout());
            this.isRunning = isRunning;
            this.measurement = measurement;
            String text = measurement.getName() + (isRunning ? " (running)" : " (queued)");
            if (isRunning)
                text = "<b>" + text + "</b>";
            text = "<html>" + text + "</html>";
            JLabel nameLabel = new JLabel(text);

            add(nameLabel, BorderLayout.CENTER);
            JButton showButton = new JButton("show");
            
            showButton.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent arg0)
                    {
                    	YouScopeClientImpl.addMeasurement(QueueListElement.this.measurement);
                    }
                });
        
            JButton stopButton = new JButton("stop");
            if (isRunning)
            {
                stopButton.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent arg0)
                        {
                            try
                            {
                                QueueListElement.this.measurement.stopMeasurement(false);
                            } catch (RemoteException | MeasurementException e)
                            {
                                ClientSystem.err.println("Could not stop measurement.", e);
                            }
                        }
                    });
            } else
            {
                stopButton.setVisible(false);
            }
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
            buttonPanel.add(showButton);
            buttonPanel.add(stopButton);
            add(buttonPanel, BorderLayout.EAST);
        }

        boolean isRunning()
        {
            return isRunning;
        }
    }
}
