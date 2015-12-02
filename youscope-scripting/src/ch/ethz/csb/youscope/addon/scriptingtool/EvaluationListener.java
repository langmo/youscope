/**
 * 
 */
package ch.ethz.csb.youscope.addon.scriptingtool;

import java.io.File;
import java.util.EventListener;

import javax.script.ScriptException;

/**
 * @author langmo
 *
 */
interface EvaluationListener extends EventListener
{
	public void evalString(String script) throws ScriptException;
	
	public void evalFile(File file) throws ScriptException;
}
