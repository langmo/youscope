/**
 * 
 */
package ch.ethz.csb.youscope.client;

import java.util.Hashtable;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * 
 * @author Moritz Lang
 *
 */
class PlotColumnHandler
{
	private final XYSeriesCollection plotsCollection;
	private final String columnName;
	private Hashtable<String, XYSeries> plots = new Hashtable<String, XYSeries>();
	
	private static final String NULL_IDENT_STRING = "theDefault1234";
	PlotColumnHandler(String columnName, XYSeriesCollection plotsCollection)
	{
		this.plotsCollection = plotsCollection;
		this.columnName = columnName;
	}
	
	@Override
	public String toString()
	{
		return columnName;
	}
	
	public String getColumnName()
	{
		return columnName;
	}
	
	public void addDate(double x, double y, String identityString)
	{
		XYSeries series = plots.get(identityString == null ? NULL_IDENT_STRING : identityString);
		if(series == null)
		{
			series = new XYSeries(identityString == null ? columnName : identityString + " - " + columnName);
			plots.put(identityString == null ? NULL_IDENT_STRING : identityString, series);
			plotsCollection.addSeries(series);
		}
		series.add(x, y);
	}
}
