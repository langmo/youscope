/**
 * 
 */
package ch.ethz.csb.youscope.shared.measurement;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * Information about how many times the currently executed job has already been executed.
 * <br>
 * The class is immutable.
 * @author Moritz Lang
 * 
 */
public final class ExecutionInformation implements Serializable, Cloneable
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8699056656518664004L;
	private final int			evaluationNumber;
	private final long			measurementStartTime;
	private final int[]			loopNumbers;

	/**
	 * Creates an execution information initialized with the given evaluation number, starting at 0.
	 * @param measurementStartTime The time when the measurement was started, in number of milliseconds since January 1, 1970, 00:00:00 GMT.
	 * @param evaluationNumber The main evaluation number.
	 */
	public ExecutionInformation(long measurementStartTime, int evaluationNumber)
	{
		this.measurementStartTime = measurementStartTime;
		this.evaluationNumber = evaluationNumber;
		this.loopNumbers = new int[0];
	}

	/**
	 * Creates a new execution information for the execution of jobs in a loop.
	 * @param parentInformation The execution information of the job which represents or executes the looping.
	 * @param loopNumber The evaluation number of the loop, starting at 0.
	 * 
	 */
	public ExecutionInformation(ExecutionInformation parentInformation, int loopNumber)
	{
		this.evaluationNumber = parentInformation.getEvaluationNumber();
		this.measurementStartTime = parentInformation.getMeasurementStartTime();
		int[] parentLoopNo = parentInformation.getLoopNumbers();
		this.loopNumbers = new int[parentLoopNo.length + 1];
		System.arraycopy(parentLoopNo, 0, this.loopNumbers, 0, parentLoopNo.length);
		this.loopNumbers[this.loopNumbers.length - 1] = loopNumber;

	}

	/**
	 * Returns the main evaluation number, i.e. how often the task containing this job has already be executed, starting at 0.
	 * @return The number of times the corresponding task has been executed.
	 */
	public int getEvaluationNumber()
	{
		return evaluationNumber;
	}

	/**
	 * Returns a string with the evaluation number followed by the loop numbers, separated by dashes.
	 * Note that in the return string the numbering of evaluations and loop numbers starts at one, different to the
	 * internal variables which start at zero.
	 * @return String indicating evaluation and loop numbers, separated by dashes, counting from one upwards.
	 */
	public String getEvaluationString()
	{
		String returnVal = Integer.toString(evaluationNumber + 1);
		for(int j = 0; j < loopNumbers.length; j++)
		{
			returnVal += "." + Integer.toString(loopNumbers[j] + 1);
		}
		return returnVal;
	}
	@Override
	public String toString()
	{
		return getEvaluationString();
	}

	/**
	 * Returns the absolute time when the measurement was started, in number of milliseconds since January 1, 1970, 00:00:00 GMT.
	 * @return The time when the measurement was started, in Unix time.
	 */
	public long getMeasurementStartTime()
	{
		return measurementStartTime;
	}

	/**
	 * Returns the time in ms of how long the current measurement is already running.
	 * Note that the runtime is precise only in the moment when the function is called.
	 * @return runtime of the measurement since it was started (not initialized; start occurs after intialization) in ms.
	 */
	public long getMeasurementRuntime()
	{
		return new Date().getTime() - getMeasurementStartTime();
	}

	/**
	 * Some jobs may execute their child jobs more than once per execution, in some kind of loop.
	 * The child jobs should than obtain an execution information with the same evaluation number, but with a loop number indicating the number of times the loop has been executed, starting at 0.
	 * Since loops can be in loops, the returned value can contain more than one loop number. The outer loop numbers are stored at the beginning, and the inner at the end of the array.
	 * @return an array indicating the number of times the loops surrounding this job have been executed. Can have the length zero, but is never null.
	 */
	public int[] getLoopNumbers()
	{
		int[] returnValue = new int[loopNumbers.length];
		System.arraycopy(loopNumbers, 0, returnValue, 0, loopNumbers.length);
		return returnValue;
	}

	@Override
	protected ExecutionInformation clone()
	{
		try
		{
			ExecutionInformation clone = (ExecutionInformation)super.clone();
			System.arraycopy(loopNumbers, 0, clone.loopNumbers, 0, loopNumbers.length);
			return clone;
		}
		catch(CloneNotSupportedException e)
		{
			throw new RuntimeException("Clone not supported", e); // won't happen.
		}
			
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + evaluationNumber;
		result = prime * result + Arrays.hashCode(loopNumbers);
		result = prime * result + (int) (measurementStartTime ^ (measurementStartTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExecutionInformation other = (ExecutionInformation) obj;
		if (evaluationNumber != other.evaluationNumber)
			return false;
		if (!Arrays.equals(loopNumbers, other.loopNumbers))
			return false;
		if (measurementStartTime != other.measurementStartTime)
			return false;
		return true;
	}
}
