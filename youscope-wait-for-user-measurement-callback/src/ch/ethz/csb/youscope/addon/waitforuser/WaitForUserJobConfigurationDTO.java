/**
 * 
 */
package ch.ethz.csb.youscope.addon.waitforuser;

import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This job/task makes images in a certain channel in regular intervals.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("wait-for-user")
public class WaitForUserJobConfigurationDTO extends JobConfiguration
{
	@XStreamAlias("message")
	@XStreamAsAttribute
	private String	message				= "No message";
	
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 7144732041177941141L;

	@Override
	public String getDescription()
	{
		String description = "<p>display(\"" + message + "\")</p>" +
			"<p>wait(user)</p>";
		return description;
	}

	

	/**
	 * Returns the message which is displayed to the user.
	 * @return Message to be displayed
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * Sets the message which is displayed to the user.
	 * @param message Message to be displayed.
	 */
	public void setMessage(String message)
	{
		if(message != null)
			this.message = message;
	}



	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "CSB::WaitForUserJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
