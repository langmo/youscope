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
package org.youscope.addon.measurement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.youscope.addon.AddonException;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.measurement.MeasurementConfiguration;

/**
 * Abstract page class for the standard YouScope measurement configuration layout.
 * The layout is typically in a frame/panel, in which one can switch with "previous"/"next" between single configuration steps, here called pages.
 * When using the standard layout, one only has to define the pages, which have to be subclasses of this class.
 * @author Moritz Lang
 * @param <T> The measurement configuration type file which should be edited by this page.
 *
 */
public abstract class MeasurementAddonUIPage<T extends MeasurementConfiguration> extends JPanel
{
	private final ArrayList<ActionListener> sizeChangeListeners = new ArrayList<ActionListener>();
	
	/**
	 * Adds a listener which gets notified if the size of this page changed.
	 * @param listener Listener to add.
	 */
	public void addSizeChangeListener(ActionListener listener)
	{
		synchronized(sizeChangeListeners)
		{
			sizeChangeListeners.add(listener);
		}
	}
	/**
	 * Removes a previously added listener.
	 * @param listener Listener to be removed.
	 */
	public void removeSizeChangeListener(ActionListener listener)
	{
		synchronized(sizeChangeListeners)
		{
			sizeChangeListeners.remove(listener);
		}
	}
	
	/**
	 * Returns if the page should be jumpable to, or false, if it will not appear.
	 * @param configuration Current configuration.
	 * @return True if visible, false if page does not appear.
	 */
	protected boolean isAppear(T configuration)
	{
		return true;
	}
	
	/**
	 * Notifies all listeners that the size of this page changed.
	 */
	protected void fireSizeChanged()
	{
		synchronized(sizeChangeListeners)
		{
			for(ActionListener listener : sizeChangeListeners)
			{
				listener.actionPerformed(new ActionEvent(this, 712, "Size changed."));
			}
		}
	}
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -3439343870691181358L;
	/**
	 * Loads the configuration data.
	 * @param configuration
	 */
	public abstract void loadData(T configuration);
	/**
	 * Saves the configuration data.
	 * If the configuration is invalid, a message should be displayed to the user.
	 * @param configuration
	 * @return Returns true if data is valid, false otherwise.
	 */
	public abstract boolean saveData(T configuration);
	
	/**
	 * Sets the values corresponding to this page to their default values.
	 * @param configuration The configuration to set to default values.
	 * @throws AddonException Thrown if error occurs while setting configuration to default values.
	 */
	public abstract void setToDefault(T configuration) throws AddonException;
	
	/**
	 * Returns a short name/description what the page does.
	 * @return Name of page.
	 */
	public abstract String getPageName();
	
	/**
	 * Called to create the UI elements during the loading process.
	 * @param parentFrame The frame in which this panel is initialized. Needed since child frames might be opened.
	 */
	public abstract void createUI(YouScopeFrame parentFrame);
}
