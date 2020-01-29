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
package org.youscope.uielements;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.youscope.uielements.plaf.BasicQuickLoggerUI;
import org.youscope.uielements.plaf.QuickLoggerUI;

/**
 * UI Component realizing a log screen/console.
 * @author mlang
 *
 */
public class QuickLogger extends JComponent
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -7005553395944159115L;
	
	/**
	 * UI Delegate name.
	 */
	public static final String UI_CLASS_ID = "QuickLoggerUI";
	
	private final QuickLoggerModel model;
	/**
	 * Constructor.
	 * @param model Model to use for quick logger.
	 */
	public QuickLogger(QuickLoggerModel model)
	{
		this.model = model;
		updateUI();		
		
	}
	/**
	 * Constructor. Uses the {@link DefaultQuickLoggerModel} as the model.
	 */
	public QuickLogger()
	{
		this(new DefaultQuickLoggerModel());	
		
	}
	
	/**
	 * Returns the model of the quick logger.
	 * @return Model of quick logger.
	 */
	public QuickLoggerModel getModel()
	{
		return model;
	}
	
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			QuickLoggerUI ui = (QuickLoggerUI) UIManager.getUI(this);
            setUI(ui);
        } else {
            setUI(new BasicQuickLoggerUI());
        }

	}

	 @Override
	    public String getUIClassID() {
	        return UI_CLASS_ID;
	    }
	
	 /**
	  * Sets the UI delegate for the logger.
	  * @param ui UI delegate.
	  */
	public void setUI(QuickLoggerUI ui) {
        super.setUI(ui);
    }

	/**
	 * Adds a message with the current time to the logger. Convenience method for {@link #getModel()}.{@link QuickLoggerModel#addMessage(String, long)}.
	 * @param message Message to add.
	 */
	public void addMessage(String message)
	{
		model.addMessage(message, -1);
	}
	/**
	 * Adds a message with the given time stamp to the logger. Convenience method for {@link #getModel()}.{@link QuickLoggerModel#addMessage(String, long)}.
	 * @param message Message to add.
	 * @param time Timestamp of message.
	 */
	public void addMessage(String message, long time)
	{
		model.addMessage(message, time);
	}
	/**
	 * Clears all messages of the logger. Convenience method for {@link #getModel()}.{@link QuickLoggerModel#clearMessages()}.
	 */
	public void clearMessages()
	{
		model.clearMessages();
	}
	

}
