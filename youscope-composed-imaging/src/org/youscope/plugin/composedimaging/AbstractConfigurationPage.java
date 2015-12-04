/**
 * 
 */
package org.youscope.plugin.composedimaging;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JPanel;


/**
 * @author langmo
 *
 */
abstract class AbstractConfigurationPage extends JPanel
{
	private final Vector<ActionListener> sizeChangeListeners = new Vector<ActionListener>();
	
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
	 * Loads the configuration data from the given DTO.
	 * @param configuration
	 */
	public abstract void loadData(ComposedImagingMeasurementConfiguration configuration);
	/**
	 * Saves the configuration data to the given DTO.
	 * @param configuration
	 */
	public abstract void saveData(ComposedImagingMeasurementConfiguration configuration);
	
	/**
	 * Sets the values corresponding to this page to their default values.
	 * @param configuration
	 */
	public abstract void setToDefault(ComposedImagingMeasurementConfiguration configuration);
	
	/**
	 * Returns a short name/description what the page does.
	 * @return Name of page.
	 */
	public abstract String getPageName();
	
	/**
	 * Called to create the UI elements during the loading process.
	 */
	public abstract void createUI();
}
