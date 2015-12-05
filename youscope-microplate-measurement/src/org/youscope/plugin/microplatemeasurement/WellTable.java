/**
 * 
 */
package org.youscope.plugin.microplatemeasurement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JPanel;

import org.youscope.common.Well;
import org.youscope.uielements.CellSelectionTable;
import org.youscope.uielements.CellSelectionTableConfiguration;

/**
 * @author langmo
 * 
 */
public class WellTable extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long								serialVersionUID	= 9065035597917159692L;
	protected CellSelectionTable							selectedCells;
	protected Vector<ActionListener> tableChangeListeners = new Vector<ActionListener>();

	/**
	 * Constructor.
	 */
	public WellTable()
	{
		super(new BorderLayout());
		selectedCells = new CellSelectionTable(8, 12, new CellSelectionTableConfiguration()
		{

			@Override
			public String getRowName(int index)
			{
				return (new Well(index, 0)).getYWellName();
			}

			@Override
			public String getColumnName(int index)
			{
				return (new Well(0, index)).getXWellName();
			}

			@Override
			public boolean isRowNamesDisplayed()
			{
				return true;
			}

			@Override
			public boolean isColumnNamesDisplayed()
			{
				return true;
			}
		});
		selectedCells.addCellSelectionListener(new CellSelectionTable.CellSelectionListener()
		{
			@Override
			public void cellSelectionChanged(int row, int column, boolean isSelected)
			{
				wellsChanged();
			}
		});
		
		add(selectedCells, BorderLayout.CENTER);
	}

	/**
	 * Loads the configuration data into this UI element.
	 * @param settings Configuration from which data should be loaded.
	 */
	public void loadFromConfiguration(MicroplatePositionConfiguration settings)
	{
		selectedCells.setDimension(settings.getNumWellsY(), settings.getNumWellsX());
		selectedCells.setSelectedCells(settings.getMeasuredWells());
	}
	
	/**
	 * Saves the settings into the configuration object.
	 * @param settings Configuration to which data should be saved.
	 */
	public void saveToConfiguration(MicroplatePositionConfiguration settings)
	{
		boolean[][] selected = selectedCells.getSelectedCells();
		for(int y = 0; y < selected.length; y++)
		{
			for(int x = 0; x < selected[y].length; x++)
			{
				settings.setMeasureWell(selected[y][x], new Well(y, x));
			}
		}
	}
	
	/**
	 * Adds a listener which gets notified if due to this UI element the configuration changed.
	 * @param listener The listener to be added.
	 */
	public void addWellsChangeListener(ActionListener listener)
	{
		tableChangeListeners.add(listener);
	}
	
	/**
	 * Removes a previously added listener.
	 * @param listener The listener to be removed.
	 */
	public void removeWellsChangeListener(ActionListener listener)
	{
		tableChangeListeners.remove(listener);
	}
	
	protected void wellsChanged()
	{
		for(ActionListener listener : tableChangeListeners)
		{
			listener.actionPerformed(new ActionEvent(this, 1234, "wellsChanged"));
		}
	}
}
