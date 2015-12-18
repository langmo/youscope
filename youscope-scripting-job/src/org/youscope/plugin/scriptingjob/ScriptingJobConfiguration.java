/**
 * 
 */
package org.youscope.plugin.scriptingjob;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.JobContainerConfiguration;
import org.youscope.common.job.basicjobs.ScriptingJob;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * @author langmo
 */
@XStreamAlias("scripting-job")
public class ScriptingJobConfiguration extends JobConfiguration implements JobContainerConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long						serialVersionUID		= 3987606293823513854L;

	/**
	 * The jobs which can be called from the script.
	 */
	@XStreamAlias("jobs")
	private Vector<JobConfiguration>	jobs					= new Vector<JobConfiguration>();

	/**
	 * True, if a scriptEngine from the client side should be used, false otherwise.
	 */
	@XStreamAlias("use-client-engine")
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	@XStreamAsAttribute
	private boolean									useClientScriptEngine	= false;

	/**
	 * The name of the engine with which the script should be interpreted.
	 */
	@XStreamAsAttribute
	@XStreamAlias("engine")
	private String									scriptEngine			= "Mozilla Rhino";

	/**
	 * The actual script which should be interpreted by the scriptEngine.
	 */
	@XStreamAsAttribute
	@XStreamAlias("file")
	private URL									scriptFile					= null;

	@Override
	public String getDescription()
	{
		return "<p>" + getScriptEngine() + ".evaluate(script)</p>";
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		ScriptingJobConfiguration clone = (ScriptingJobConfiguration)super.clone();
		clone.jobs = new Vector<JobConfiguration>();
		for(int i = 0; i < jobs.size(); i++)
		{
			clone.jobs.add((JobConfiguration)jobs.elementAt(i).clone());
		}
		if(scriptFile != null)
		{
			try
			{
				clone.scriptFile = new URL(scriptFile.toExternalForm());
			}
			catch(MalformedURLException e)
			{
				throw new CloneNotSupportedException("Could not clone script file location ("+e.getMessage()+").");
			}
		}
		return clone;
	}

	@Override
	public JobConfiguration[] getJobs()
	{
		return jobs.toArray(new JobConfiguration[jobs.size()]);
	}

	@Override
	public void setJobs(JobConfiguration[] jobs)
	{
		this.jobs.clear();
		for(JobConfiguration job:jobs)
		{
			this.jobs.add(job);
		}
	}

	@Override
	public void addJob(JobConfiguration job)
	{
		jobs.add(job);
	}

	@Override
	public void clearJobs()
	{
		jobs.clear();
	}

	@Override
	public void removeJobAt(int index)
	{
		jobs.removeElementAt(index);
	}

	@Override
	public void addJob(JobConfiguration job, int index)
	{
		jobs.insertElementAt(job, index);

	}

	/**
	 * @param useClientScriptEngine the useClientScriptEngine to set
	 */
	public void setUseClientScriptEngine(boolean useClientScriptEngine)
	{
		this.useClientScriptEngine = useClientScriptEngine;
	}

	/**
	 * @return the useClientScriptEngine
	 */
	public boolean isUseClientScriptEngine()
	{
		return useClientScriptEngine;
	}

	/**
	 * @param scriptEngine the scriptEngine to set
	 */
	public void setScriptEngine(String scriptEngine)
	{
		this.scriptEngine = scriptEngine;
	}

	/**
	 * @return the scriptEngine
	 */
	public String getScriptEngine()
	{
		return scriptEngine;
	}

	/**
	 * Sets the file which should be executed by this script job (the file where the script is in).
	 * @param scriptFile the file where the script is in.
	 */
	public void setScriptFile(URL scriptFile)
	{
		this.scriptFile = scriptFile;
	}

	/**
	 * Returns the file which should be executed by this script job (the file where the script is in).
	 * @return the file where the script is in, or null if yet not set.
	 */
	public URL getScriptFile()
	{
		return scriptFile;
	}

	@Override
	public String getTypeIdentifier()
	{
		return ScriptingJob.DEFAULT_TYPE_IDENTIFIER;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		
		if(scriptFile == null || scriptFile.toString() == null || scriptFile.toString().length() == 0)
    	{
    		throw new ConfigurationException("No script file selected.");
    	}
		if(jobs == null)
			throw new ConfigurationException("Jobs are null.");
		for(JobConfiguration job : jobs)
			job.checkConfiguration();
	}
	
}
