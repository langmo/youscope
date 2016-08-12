/**
 * 
 */
package org.youscope.uielements;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Component to select or deselect rectangularly organized cells, e.g. wells are tiles.
 * @author Moritz Lang
 */
public class CellSelectionTable extends JScrollPane
{

	/**
	 * Serial Version UID.
	 */
	private static final long					serialVersionUID	= -3132224040673230873L;

	private int								numX;

	private int								numY;

	private CellSelectionTableModel			model				= new CellSelectionTableModel();

	private CellSelectionTableTable				table;

	private boolean[][]						cellSelected;

	private int								cellSize			= 25;

	private Vector<CellSelectionListener>		changeListeners		= new Vector<CellSelectionListener>();

	private JList<String>								rowHeader;

	private CellSelectionTableConfiguration	configuration;

	/**
	 * Constructor.
	 * @param numY Number of rows.
	 * @param numX Number of columns.
	 * @param configuration Configuration which defines in more detail the lay out of the table.
	 */
	public CellSelectionTable(int numY, int numX, CellSelectionTableConfiguration configuration)
	{
		this.numX = numX;
		this.numY = numY;
		this.configuration = configuration;

		cellSelected = new boolean[numY][numX];
		table = new CellSelectionTableTable(model);
		table.setBorder(new LineBorder(Color.black, 1));

		if(configuration.isRowNamesDisplayed())
		{
			rowHeader = new JList<String>(new RowHeaderListModel());
			rowHeader.setFixedCellWidth(30);
			rowHeader.setCellRenderer(new RowHeaderRenderer(table));
			rowHeader.setBackground(getBackground());
			setRowHeaderView(rowHeader);
		}
		setViewportView(table);
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent arg0)
			{
				table.resizeElements();
			}

			@Override
			public void componentShown(ComponentEvent arg0)
			{
				table.resizeElements();
			}
		});
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setPreferredSize(new Dimension(200, 200));
	}

	@Override
	public void revalidate()
	{
		super.revalidate();
		if(table != null)
			table.resizeElements();
	}

	/**
	 * Adds a listener which gets notified if the selection changes.
	 * @param listener The listener to add.
	 */
	public void addCellSelectionListener(CellSelectionListener listener)
	{
		changeListeners.addElement(listener);
	}

	/**
	 * Removes a previously added listener.
	 * @param listener The listener to remove.
	 */
	public void removeCellSelectionListener(CellSelectionListener listener)
	{
		changeListeners.remove(listener);
	}

	/**
	 * Returns the number of columns and rows of the table.
	 * @return Number of columns and rows.
	 */
	public Dimension getNumCells()
	{
		return new Dimension(numX, numY);
	}

	/**
	 * Sets the number of columns and rows of the table.
	 * @param numY Number of rows.
	 * @param numX Number of columns.
	 */
	public void setNumCells(int numY, int numX)
	{
		if(numY <= 0 || numX <= 0)
			return;
		if(this.numX == numX && this.numY == numY)
			return;
		if(this.numX < numX || this.numY < numY)
		{
			// Copy selections so that old ones are kept...
			int width = Math.max(numX, this.numX);
			int height = Math.max(numY, this.numY);
			boolean[][] cellSelectedNew = new boolean[height][width];
			for(int row = 0; row < this.numY; row++)
			{
				for(int column = 0; column < this.numX; column++)
				{
					cellSelectedNew[row][column] = cellSelected[row][column];
				}
			}
			cellSelected = cellSelectedNew;
		}

		this.numY = numY;
		this.numX = numX;

		model.fireTableStructureChanged();
		table.resizeElements();
		revalidate();
	}

	/**
	 * Returns a Matrix of booleans which represent the selected cells.
	 * @return Matrix of selected cells.
	 */
	public boolean[][] getSelectedCells()
	{
		if(numY == cellSelected.length && numX == cellSelected[0].length)
			return cellSelected;

		// Copy elements in array with right sizes...
		boolean[][] output = new boolean[numY][numX];
		for(int row = 0; row < numY; row++)
		{
			for(int column = 0; column < numX; column++)
			{
				output[row][column] = cellSelected[row][column];
			}
		}
		return output;
	}

	/**
	 * Sets the selected cells.
	 * @param cellSelected Array of selected cells.
	 */
	public void setSelectedCells(boolean[][] cellSelected)
	{
		this.cellSelected = cellSelected;
	}

	/**
	 * Sets the selected cells.
	 * @param cells List of all selected cells. All other cells get deselected.
	 */
	public void setSelectedCells(Point[] cells)
	{
		unselectAllCells();

		// Select the cells
		for(Point cell : cells)
		{
			if(cell.y >= 0 && cell.y < cellSelected.length && cell.x >= 0 && cell.x < cellSelected[cell.y].length)
			{
				cellSelected[cell.y][cell.x] = true;
			}
		}
		model.fireTableDataChanged();
	}

	/**
	 * Selects all cells
	 */
	public void selectAllCells()
	{
		for(int row = 0; row < cellSelected.length; row++)
		{
			for(int column = 0; column < cellSelected[row].length; column++)
			{
				cellSelected[row][column] = true;
			}
		}
	}

	/**
	 * Unselects all cells.
	 */
	public void unselectAllCells()
	{
		for(int row = 0; row < cellSelected.length; row++)
		{
			for(int column = 0; column < cellSelected[row].length; column++)
			{
				cellSelected[row][column] = false;
			}
		}
	}

	/**
	 * Returns the number of selected cells
	 * @return Number of selected cells.
	 */
	public int getSelectedCellCount()
	{
		int result = 0;
		for(int row = 0; row < this.numY; row++)
		{
			for(int column = 0; column < this.numX; column++)
			{
				if(cellSelected[row][column])
					result++;
			}
		}
		return result;
	}

	protected class RowHeaderRenderer extends JComponent implements ListCellRenderer<String>
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -6920199403063876042L;
		private String				text				= "";

		RowHeaderRenderer(JTable table)
		{
			JTableHeader header = table.getTableHeader();
			setOpaque(true);
			setForeground(header.getForeground());
			setBackground(header.getBackground());
			setFont(header.getFont());
		}

		@Override
		public void paintComponent(Graphics g)
		{
			Font font = getFont();
			int width;
			int height;
			while(true)
			{
				g.setFont(font);
				width = g.getFontMetrics().stringWidth(getText());
				height = g.getFontMetrics().getAscent();
				if(width > getWidth() || height > getHeight())
				{
					font = font.deriveFont(font.getSize2D() * 0.75F);
				}
				else
					break;
			}
			g.drawString(getText(), (getWidth() - width) / 2, (getHeight() - height) / 2 + height);
		}

		String getText()
		{
			return text;
		}

		void setText(String text)
		{
			this.text = text;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus)
		{
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}

	protected class ColumnHeaderRenderer extends JComponent implements TableCellRenderer
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -6920199403063876043L;
		private String				text				= "";

		ColumnHeaderRenderer(JTable table)
		{
			JTableHeader header = table.getTableHeader();
			setOpaque(true);
			setForeground(header.getForeground());
			setBackground(header.getBackground());
			setFont(header.getFont());
			setMinimumSize(new Dimension(10, getFontMetrics(getFont()).getHeight() + 4));
		}

		@Override
		public void paintComponent(Graphics g)
		{
			Font font = getFont();
			int width;
			int height;
			while(true)
			{
				g.setFont(font);
				width = g.getFontMetrics().stringWidth(getText());
				height = g.getFontMetrics().getAscent();
				if(width > getWidth() || height > getHeight())
				{
					font = font.deriveFont(font.getSize2D() * 0.75F);
				}
				else
					break;
			}
			g.drawString(getText(), (getWidth() - width) / 2, (getHeight() - height) / 2 + height);
		}

		String getText()
		{
			return text;
		}

		void setText(String text)
		{
			this.text = text;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}

	protected class RowHeaderListModel extends AbstractListModel<String>
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -3782119531082035074L;

		@Override
		public int getSize()
		{
			return numY;
		}

		@Override
		public String getElementAt(int index)
		{
			return configuration.getRowName(index);
		}
	}

	protected class CellSelectionTableTable extends JTable
	{

		/**
		 * Serial Version UID.
		 */
		private static final long		serialVersionUID	= 2224025418962338390L;

		private CellSelectionRenderer	renderer			= new CellSelectionRenderer();

		CellSelectionTableTable(CellSelectionTableModel model)
		{
			super(model);

			setFillsViewportHeight(false);
			getSelectionModel().addListSelectionListener(new SelectionListener());
			getColumnModel().getSelectionModel().addListSelectionListener(new SelectionListener());
			setCellSelectionEnabled(true);
			setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			setRowHeight(cellSize);
			getTableHeader().setReorderingAllowed(false);
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			getTableHeader().setResizingAllowed(false);

			if(configuration.isColumnNamesDisplayed())
				getTableHeader().setDefaultRenderer(new ColumnHeaderRenderer(this));
			else
				setTableHeader(null);
		}

		void resizeElements()
		{
			int newCellSize = Math.min(getViewport().getWidth() / numX, getViewport().getHeight() / numY);
			if(newCellSize < 1 || cellSize == newCellSize)
				return;
			if(newCellSize < 10)
				cellSize = 10;
			else
				cellSize = newCellSize;

			setPreferredSize(new Dimension(cellSize * numX, cellSize * numY));
			setRowHeight(cellSize);
			if(configuration.isRowNamesDisplayed())
				rowHeader.setFixedCellHeight(cellSize);

			model.fireTableStructureChanged();
			for(Enumeration<TableColumn> e = getColumnModel().getColumns(); e.hasMoreElements();)
			{
				TableColumn column = e.nextElement();
				column.setPreferredWidth(cellSize);
				column.setMinWidth(cellSize);
				column.setMaxWidth(cellSize);
			}
			revalidate();
		}

		@Override
		public TableCellRenderer getCellRenderer(int row, int column)
		{
			return renderer;
		}
	}

	protected class CellSelectionTableModel extends AbstractTableModel
	{

		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -3473984902925358571L;

		@Override
		public int getColumnCount()
		{
			return numX;
		}

		@Override
		public int getRowCount()
		{
			return numY;
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			return "";
		}

		@Override
		public String getColumnName(int col)
		{
			if(configuration.isColumnNamesDisplayed())
				return configuration.getColumnName(col);
			return null;
		}

		@Override
		public boolean isCellEditable(int row, int col)
		{
			return false;
		}

		@Override
		public Class<?> getColumnClass(int c)
		{
			return getValueAt(0, c).getClass();
		}
	}

	protected class CellSelectionRenderer extends JLabel implements TableCellRenderer
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 5045544589881926980L;

		public CellSelectionRenderer()
		{
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column)
		{
			boolean selected = cellSelected[row][column];
			if(isSelected)
				selected = !selected;
			if(selected)
			{
				setBackground(Color.GREEN);
			}
			else
			{
				setBackground(Color.LIGHT_GRAY);
			}
			return this;
		}
	}

	protected class SelectionListener implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent event)
		{
			if(event.getValueIsAdjusting())
			{
				return;
			}
			int[] selectedRows = table.getSelectedRows();
			for(int row : selectedRows)
			{
				for(int column : table.getSelectedColumns())
				{
					// Invert selection.
					cellSelected[row][column] = !cellSelected[row][column];
					for(CellSelectionListener listener : changeListeners)
					{
						listener.cellSelectionChanged(row, column, cellSelected[row][column]);
					}
				}
			}
			model.fireTableDataChanged();
		}
	}

	/**
	 * Listener which is called when cell gets selected or unselected.
	 * @author langmo
	 * 
	 */
	public interface CellSelectionListener extends EventListener
	{
		/**
		 * Function which is called when cell gets selected or unselected.
		 * @param row Row of the cell.
		 * @param column Column of the cell.
		 * @param isSelected TRUE if cell got selected, false otherwise.
		 */
		public void cellSelectionChanged(int row, int column, boolean isSelected);
	}
}
