/**
 * 
 */
package org.youscope.client;

import javax.swing.JPanel;

/**
 * @author Moritz Lang
 *
 */
abstract class ManageTabElement extends JPanel
{
	/**
	 * Serial Version UID
	 */
	private static final long	serialVersionUID	= -2478439915045468521L;
	public abstract void initializeContent();
	public abstract boolean storeContent();
}
