/**
 * 
 */
package ch.ethz.csb.youscope.client;

/**
 * Type of the table data representation in a plot.
 * @author Moritz Lang
 *
 */
enum TableDataPlotType
{
	/**
	 * Each table data element gets plotted as a scatter element.
	 */
	Scatter,
	/**
	 * If more than one element is transmitted per evaluation, only the median is plotted.
	 */
	LineMedian,
	/**
	 * If more than one element is transmitted per evaluation, only the mean is plotted.
	 */
	LineMean,
	/**
	 * If more than one element is transmitted per evaluation, only the first is plotted.
	 */
	LineFirst,
	/**
	 * If more than one element is transmitted per evaluation, all are plotted. The elements which should be connected are determined by an additional column.
	 */
	LineIdentity;
}
