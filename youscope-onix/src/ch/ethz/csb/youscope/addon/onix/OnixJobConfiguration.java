/**
 * 
 */
package ch.ethz.csb.youscope.addon.onix;

import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.configuration.TableConsumerConfiguration;
import ch.ethz.csb.youscope.shared.table.TableDefinition;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * A job to control the CellAsic Onix microfluidic system.
 * @author Moritz Lang
 */
@XStreamAlias("onix-job")
public class OnixJobConfiguration extends JobConfiguration implements TableConsumerConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 7547751129638360497L;

	@XStreamAlias("protocol")
	private String	onixProtocol	= "";

	@XStreamAlias("wait-until-finished")
	@XStreamAsAttribute
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean waitUntilFinished = true;
	
	@Override
	public String getDescription()
	{
		String text = "onix.";
		text += waitUntilFinished ? "eval(&quot;" : "parallel_eval(&quot;";
		text += onixProtocol.replace("\n", "<br />");
		text += "&quot;)";
		
		return text;
	}

	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "CSB::OnixJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * Sets the protocol which gets evaluated on the onix device every time the job gets evaluated.
	 * @param onixProtocol The onix protocol.
	 */
	public void setOnixProtocol(String onixProtocol)
	{
		if(onixProtocol == null)
			onixProtocol = "";
		this.onixProtocol = onixProtocol;
	}

	/**
	 * Returns the onix protocol.
	 * @return Onix protocol.
	 */
	public String getOnixProtocol()
	{
		return onixProtocol;
	}

	/**
	 * If true, the job waits when evaluating the onix protocol until the protocol is finished. If false, the onix protocol gets evaluated in parallel.
	 * @param waitUntilFinished True if job should wait until end of onix protocol evaluation.
	 */
	public void setWaitUntilFinished(boolean waitUntilFinished)
	{
		this.waitUntilFinished = waitUntilFinished;
	}

	/**
	 * If true, the job waits when evaluating the onix protocol until the protocol is finished. If false, the onix protocol gets evaluated in parallel.
	 * @return True if job should wait until end of onix protocol evaluation.
	 */
	public boolean isWaitUntilFinished()
	{
		return waitUntilFinished;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Override
	public TableDefinition getConsumedTableDefinition() {
		return OnixTable.getTableDefinition();
	}
}
