/**
 * 
 */
package org.youscope.plugin.scripting;

import java.util.EventListener;

/**
 * @author langmo
 *
 */
interface ScriptMessageListener extends EventListener
{
	/**
	 * Executed if engine produced a text message due to the execution of one or more lines of code.
	 * @param message Message the engine did send.
	 */
	public void outputMessage(String message);
	
	/**
	 * Executed if code was send to the engine to be executed.
	 * @param message Code which will be executed.
	 */
	public void inputMessage(String message);
}
