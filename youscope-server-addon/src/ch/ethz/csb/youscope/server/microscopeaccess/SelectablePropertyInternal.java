/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess;


/**
 * @author Moritz Lang
 *
 */
public interface SelectablePropertyInternal extends PropertyInternal
{
	/**
	 * Returns a list of all allowed property values. If all possible values are allowed, the allowed values are not known, or the allowed values are not discrete, returns null.
	 * @return List of all allowed values or null.
	 */
	public String[] getAllowedPropertyValues();
}
