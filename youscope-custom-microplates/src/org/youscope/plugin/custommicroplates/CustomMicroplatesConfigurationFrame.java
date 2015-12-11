/**
 * 
 */
package org.youscope.plugin.custommicroplates;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.Microplate;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 *
 */
class CustomMicroplatesConfigurationFrame
{
	private YouScopeFrame									frame;
	
	private JFormattedTextField					widthField					= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField					heightField					= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField					numXField					= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField					numYField					= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JTextField microplateNameField 									= new JTextField();
		
	private Microplate microplateType;
	private Vector<ActionListener> listeners = new Vector<ActionListener>();
	private YouScopeClient client;
	
	CustomMicroplatesConfigurationFrame(YouScopeClient client, YouScopeFrame frame)
	{
		this(client, frame, null);
	}
	CustomMicroplatesConfigurationFrame(YouScopeClient client, YouScopeFrame frame, Microplate microplateType)
	{
		this.frame = frame;
		this.microplateType = microplateType;
		this.client = client;
		
		frame.setTitle("Custom Microplate Configuration");
		frame.setResizable(false);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		GridBagLayout elementsLayout = new GridBagLayout();
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
        JPanel elementsPanel = new JPanel(elementsLayout);
        StandardFormats.addGridBagElement(new JLabel("Short Description of microplate:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(microplateNameField, elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(new JLabel("Number of wells horizontal/vertical:"), elementsLayout, newLineConstr, elementsPanel);
        JPanel numberPanel = new JPanel(new GridLayout(1, 2, 0, 3));
        numberPanel.add(numXField);
        numberPanel.add(numYField);
        StandardFormats.addGridBagElement(numberPanel, elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(new JLabel("Distance wells horizontal/vertical (in μm):"), elementsLayout, newLineConstr, elementsPanel);
        JPanel distancePanel = new JPanel(new GridLayout(1, 2, 0, 3));
        distancePanel.add(widthField);
        distancePanel.add(heightField);
        StandardFormats.addGridBagElement(distancePanel, elementsLayout, newLineConstr, elementsPanel);
        
		JButton closeButton = new JButton("Save");
		closeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	String microplateName = microplateNameField.getText();
                	if(microplateName.length()<3)
                	{
                		JOptionPane.showMessageDialog(null, "Microplate Description must be longer than three characters.", "Could not initialize microplate type", JOptionPane.INFORMATION_MESSAGE);
                		return;
                	}
                	int numWellsX = ((Number)numXField.getValue()).intValue();
                	int numWellsY = ((Number)numYField.getValue()).intValue();
                	double wellWidth  = ((Number)widthField.getValue()).doubleValue();
                	double wellHeight = ((Number)heightField.getValue()).doubleValue();

                	String microplateID;
                	if(CustomMicroplatesConfigurationFrame.this.microplateType != null)
                	{
                		microplateID = CustomMicroplatesConfigurationFrame.this.microplateType.getMicroplateID();
                		try
                		{
                			CustomMicroplatesManager.deleteMicroplateTypeDefinition(CustomMicroplatesConfigurationFrame.this.microplateType);
                		}
                		catch(Exception e1)
                		{
                			CustomMicroplatesConfigurationFrame.this.client.sendError("Could not delete old microplate type definition.", e1);
                		}
                	}
                	else
                	{
                		microplateID = "custommicroplate_" + microplateName + ".xml";
                	}
                	CustomMicroplatesConfigurationFrame.this.microplateType = new CustomMicroplateType(numWellsX, numWellsY, wellWidth, wellHeight, microplateID, microplateName);
                	
                	try
					{
						CustomMicroplatesManager.saveMicroplateTypeDefinition(CustomMicroplatesConfigurationFrame.this.microplateType);
					}
					catch(IOException e1)
					{
						CustomMicroplatesConfigurationFrame.this.client.sendError("Could not save microplate type definition.", e1);
						return;
					}
                	
                    CustomMicroplatesConfigurationFrame.this.frame.setVisible(false); 
                    for(ActionListener listener : listeners)
                    {
                    	listener.actionPerformed(new ActionEvent(this, 155, "Microplate type created or edited."));
                    }
                }
            });
		
		// Load data
        if(microplateType != null)
        {
        	microplateNameField.setText(microplateType.getMicroplateName());
        	widthField.setValue(microplateType.getWellWidth());
        	heightField.setValue(microplateType.getWellHeight());
        	numXField.setValue(microplateType.getNumWellsX());
        	numYField.setValue(microplateType.getNumWellsY());
        }
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(elementsPanel, BorderLayout.CENTER);
        contentPane.add(closeButton, BorderLayout.SOUTH);
        frame.setContentPane(contentPane);
        frame.pack();
	}
	
	public void addActionListener(ActionListener listener)
	{
		listeners.add(listener);
	}
	public void removeActionListener(ActionListener listener)
	{
		listeners.remove(listener);
	}
}
