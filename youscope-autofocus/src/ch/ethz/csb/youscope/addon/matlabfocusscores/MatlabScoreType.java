/**
 * 
 */
package ch.ethz.csb.youscope.addon.matlabfocusscores;

/**
 * @author Moritz Lang
 *
 */
public enum MatlabScoreType
{
	/**
	 * Histogram range algorithm.
	 */
	HISTOGRAM_RANGE("Histogram Range"),
	/**
	 * Sobel edge detection.
	 */
	SOBEL3("Sobel 3x3"),
	/**
	 * Sobel edge detection.
	 */
	SOBEL5("Sobel 5x5"),
	/**
	 * Sobel edge detection.
	 */
	SOBEL7("Sobel 7x7"),
	/**
	 * Normalized variances.
	 */
	NORMALIZED_VARIANCES("Normalized Variances");
	
	
	private final String description;
	MatlabScoreType(String description)
	{
		this.description = description;
	}
	
	/**
	 * Returns a human readable short description of the algorithm.
	 * @return Short algorithm description.
	 */
	public String getDescription()
	{
		return description;
	}
	
	@Override
	public String toString()
	{
		return description;
	}
}
