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
package org.youscope.plugin.zslides;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.StandardFormats;

/**
 * UI element to add a range of values to the z-stack.
 * @author Moritz Lang
 *
 */
class AddRangeFrame extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8939548701643750868L;
	private final ZSlidesJobConfigurationAddon parent;
	private final YouScopeFrame frame;
	private final GridBagLayout layout = new GridBagLayout();
	private JFormattedTextField				upperField		= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private JFormattedTextField				stepField		= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private JFormattedTextField				lowerField		= new JFormattedTextField(StandardFormats.getDoubleFormat());
	AddRangeFrame(ZSlidesJobConfigurationAddon parent, YouScopeFrame frame)
	{
		this.parent = parent;
		this.frame = frame;
		
		frame.setTitle("Add Range");
		frame.setResizable(false);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		setLayout(layout);
		
		JEditorPane descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>Adds a range of z-stack values.</b></p>" +
		"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">Define the lower and the upper value for the range, as well as the step size in which the z-stack should be executed.</p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">A total number of floor((upper-lower) / stepSize) + 1 elements will be added.</p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">If the upper value is smaller than the lower value, no element will be added. If the range (upper-lower) is not an integer multiple of the step size, the upper element will not be added.</p>" +
				"</html>");
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
		descriptionScrollPane.setPreferredSize(new Dimension(350, 150));
		StandardFormats.addGridBagElement(descriptionScrollPane, layout, newLineConstr, this);
		
		lowerField.setValue(-10.0);
		stepField.setValue(0.5);
		upperField.setValue(10.0);
		StandardFormats.addGridBagElement(new JLabel("Lower bound of range (um):"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(lowerField, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(new JLabel("Step size (um):"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(stepField, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(new JLabel("Upper bound of range (um):"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(upperField, layout, newLineConstr, this);
		
		// The add button
        JButton addRangeButton = new JButton("Add Range");
        addRangeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	double upper = ((Number)upperField.getValue()).doubleValue();
                	double lower = ((Number)lowerField.getValue()).doubleValue();
                	double step = ((Number)stepField.getValue()).doubleValue();
                	int numElements = (int)((upper - lower) / step + 1);
                	if(numElements > 0)
                	{
	                	double[] rows = new double[numElements];
	                	for(int i=0; i<numElements; i++)
	                	{
	                		rows[i] = lower + i * step;
	                	}
	                	AddRangeFrame.this.parent.addRows(rows);
                	}
                	AddRangeFrame.this.frame.setVisible(false);
                }
            });
        StandardFormats.addGridBagElement(addRangeButton, layout, newLineConstr, this);
        
        frame.setContentPane(this);
        frame.pack();
	}
}
