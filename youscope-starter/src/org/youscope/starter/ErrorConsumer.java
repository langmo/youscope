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
package org.youscope.starter;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author langmo
 */
class ErrorConsumer
{
    static void consumeException(String description, Exception exception)
    {
    	exception.printStackTrace();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(description), BorderLayout.NORTH);
        String message = "";
        Throwable iter = exception;
        while(iter != null)
        {
            message += iter.getClass().getSimpleName() + ": " + iter.getMessage() + "\n";
            iter = iter.getCause();
            if (iter == null)
                break;
        }
        if(exception.getStackTrace() != null)
        {
        	message += "\nStack Trace:";
        	for(StackTraceElement element: exception.getStackTrace())
        	{
        		if(element != null && !element.isNativeMethod() && element.getFileName() != null)
        			message+="\n"+element.getFileName()+", Line "+element.getLineNumber();
        	}
        }
        JTextArea exceptionDescription = new JTextArea(message);
        exceptionDescription.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(exceptionDescription);
        scrollPane.setPreferredSize(new Dimension(700, 200));
        panel.add(scrollPane, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(null, panel, "Error while starting YouScope", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
