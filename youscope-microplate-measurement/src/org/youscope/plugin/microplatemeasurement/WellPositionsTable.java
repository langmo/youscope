/**
 * 
 */
package org.youscope.plugin.microplatemeasurement;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.Formatter;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.youscope.uielements.CellSelectionTable;
import org.youscope.uielements.CellSelectionTableConfiguration;
import org.youscope.uielements.StandardFormats;


/**
 * @author langmo
 * 
 */
public class WellPositionsTable extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long								serialVersionUID	= 5575651002147418067L;
	private CellSelectionTable							selectedCells;
	private Rectangle										cellsLocation		= new Rectangle(30, 30, 100, 100);
	private Rectangle										boundaryLocation	= new Rectangle(0, 0, 160, 160);
	private JComboBox<Integer>										numPosField			= new JComboBox<Integer>(new Integer[] {3, 5, 7, 9});
	private JLabel										numPosLabel			= new JLabel("× XXXXXXXXXX µm");
	private JFormattedTextField							borderField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private JLabel										borderLabel			= new JLabel("% = XXXXXXXX µm");
	private JPanel borderPanel;
	private JPanel numPosPanel;
	private Vector<ActionListener> tableChangeListeners = new Vector<ActionListener>();
	
	private volatile boolean loading = false;
	/**
	 * Constructor.
	 */
	public WellPositionsTable()
	{
		super();
		
		setOpaque(false);
		selectedCells = new CellSelectionTable(1, 1, new CellSelectionTableConfiguration()
		{

			@Override
			public String getRowName(int index)
			{
				return "";
			}

			@Override
			public String getColumnName(int index)
			{
				return "";
			}

			@Override
			public boolean isRowNamesDisplayed()
			{
				return false;
			}

			@Override
			public boolean isColumnNamesDisplayed()
			{
				return false;
			}
		});
		selectedCells.addCellSelectionListener(new CellSelectionTable.CellSelectionListener()
		{
			@Override
			public void cellSelectionChanged(int row, int column, boolean isSelected)
			{
				wellPositionsChanged();
			}
		});

		setLayout(null);

		add(selectedCells);
		selectedCells.setBounds(cellsLocation);

		numPosLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		numPosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		numPosPanel.add(numPosField);
		numPosPanel.add(numPosLabel);
		add(numPosPanel);
		Dimension numPosSize = numPosPanel.getPreferredSize();
		numPosPanel.setBounds((int)boundaryLocation.getMaxX() + 18, (int)(cellsLocation.getMinY() + cellsLocation.getMaxY() - numPosSize.height) / 2, numPosSize.width, numPosSize.height);

		borderField.setValue(999.99);
		borderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		borderPanel.setOpaque(false);
		borderPanel.add(borderField);
		borderPanel.add(borderLabel);
		add(borderPanel);
		Dimension borderSize = borderPanel.getPreferredSize();
		borderPanel.setBounds((int)boundaryLocation.getMaxX() + 40, (int)boundaryLocation.getMinY() + 10, borderSize.width, borderSize.height);

		Dimension size = new Dimension(200, (int)(boundaryLocation.getMaxY() + 1));
		setMinimumSize(size);
		setPreferredSize(size);

		numPosField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int dimension = ((Number)numPosField.getSelectedItem()).intValue();
				selectedCells.setDimension(dimension, dimension);
				wellPositionsChanged();
			}
		});

		borderField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				wellPositionsChanged();
			}
		});
	}
	
	/**
	 * Adds a listener which gets notified if due to this UI element the configuration changed.
	 * @param listener The listener to be added.
	 */
	public void addWellPositionsChangeListener(ActionListener listener)
	{
		tableChangeListeners.add(listener);
	}
	
	/**
	 * Removes a previously added listener.
	 * @param listener The listener to be removed.
	 */
	public void removeWellPositionsChangeListener(ActionListener listener)
	{
		tableChangeListeners.remove(listener);
	}
	
	private void wellPositionsChanged()
	{
		if(loading)
			return;
		for(ActionListener listener : tableChangeListeners)
		{
			listener.actionPerformed(new ActionEvent(this, 1235, "wellPositionsChanged"));
		}
	}

	/**
	 * Loads the configuration data into this UI element.
	 * @param settings Configuration from which data should be loaded.
	 */
	public void loadFromConfiguration(MicroplatePositionConfiguration settings)
	{
		loading = true;
		selectedCells.setDimension(settings.getWellNumPositionsY(), settings.getWellNumPositionsX());
		selectedCells.setSelectedCells(settings.getMeasuredPositionsInWell());
		selectedCells.revalidate();

		numPosField.setSelectedItem(settings.getWellNumPositionsX());
		
		borderField.setValue(settings.getWellMarginX()*100.0);
		Formatter formatter = new Formatter();
		borderLabel.setText("% = " + formatter.format("%6.2f", settings.getWellMarginX() * settings.getWellWidth()) + " µm");
		formatter.close();
		formatter = new Formatter();
		numPosLabel.setText("× " + formatter.format("%6.2f", settings.getWellPositionDistanceY()).toString() + " µm");
		formatter.close();
		
		Dimension borderSize = borderPanel.getPreferredSize();
		borderPanel.setBounds((int)boundaryLocation.getMaxX() + 40, (int)boundaryLocation.getMinY() + 10, borderSize.width, borderSize.height);

		Dimension numPosSize = numPosPanel.getPreferredSize();
		numPosPanel.setBounds((int)boundaryLocation.getMaxX() + 18, (int)(cellsLocation.getMinY() + cellsLocation.getMaxY() - numPosSize.height) / 2, numPosSize.width, numPosSize.height);
		
		loading = false;
	}
	
	/**
	 * Saves the settings into the configuration object.
	 * @param settings Configuration to which data should be saved.
	 */
	public void saveToConfiguration(MicroplatePositionConfiguration settings)
	{
		settings.setWellMarginX(((Number)borderField.getValue()).doubleValue() / 100);
		settings.setWellMarginY(((Number)borderField.getValue()).doubleValue() / 100);
		Dimension dim = selectedCells.getDimension();
		settings.setWellNumPositionsX(dim.width);
		settings.setWellNumPositionsY(dim.height);
		
		boolean[][] selected = selectedCells.getSelectedCells();
		for(int row = 0; row <selected.length; row ++)
		{
			for(int column = 0; column <selected[row].length; column ++)
			{
				settings.setMeasurePosition(selected[row][column], row, column);
			}
		}
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2D = (Graphics2D)g;
		// super.paintComponent(g2D);

		g2D.setColor(Color.WHITE);
		g2D.fill(boundaryLocation);
		g2D.setColor(Color.BLACK);
		g2D.draw(boundaryLocation);

		// helping lines
		g2D.setColor(Color.LIGHT_GRAY);
		g2D.drawLine((int)cellsLocation.getMaxX(), (int)cellsLocation.getMinY(), (int)boundaryLocation.getMaxX() + 20, (int)cellsLocation.getMinY());
		g2D.drawLine((int)cellsLocation.getMaxX(), (int)cellsLocation.getMaxY() - 1, (int)boundaryLocation.getMaxX() + 20, (int)cellsLocation.getMaxY() - 1);

		g2D.drawLine((int)cellsLocation.getMaxX() - 1, (int)cellsLocation.getMinY(), (int)cellsLocation.getMaxX() - 1, (int)boundaryLocation.getMinY() + 1);

		// arrows
		fillDoubleArrow(g2D, boundaryLocation.getMaxX() + 15, cellsLocation.getMinY(), boundaryLocation.getMaxX() + 15, cellsLocation.getMaxY() - 1);

		fillArrow(g2D, boundaryLocation.getMaxX(), boundaryLocation.getMinY() + 7, cellsLocation.getMaxX(), boundaryLocation.getMinY() + 7);
		fillArrow(g2D, boundaryLocation.getMaxX() + 90, boundaryLocation.getMinY() + 7, boundaryLocation.getMaxX(), boundaryLocation.getMinY() + 7);

	}

	static void fillDoubleArrow(Graphics2D g2D, double x1, double y1, double x2, double y2)
	{
		Shape arrow = createDoubleArrowShape(x1, y1, x2, y2);
		g2D.setColor(Color.BLACK);
		g2D.fill(arrow);
		g2D.setColor(Color.DARK_GRAY);
		g2D.draw(arrow);
	}

	static void fillArrow(Graphics2D g2D, double x1, double y1, double x2, double y2)
	{
		Shape arrow = createArrowShape(x1, y1, x2, y2);
		g2D.setColor(Color.BLACK);
		g2D.fill(arrow);
		g2D.setColor(Color.DARK_GRAY);
		g2D.draw(arrow);
	}

	static Shape createDoubleArrowShape(double x1, double y1, double x2, double y2)
	{
		double xMid = (x1 + x2) / 2;
		double yMid = (y1 + y2) / 2;
		int dist = (int)(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) / 2);

		int headSize = 4;
		int halfWidth = 1;

		Polygon arrowPolygon = new Polygon();
		arrowPolygon.addPoint(-dist + headSize, -halfWidth);
		arrowPolygon.addPoint(-dist + headSize, -headSize);
		arrowPolygon.addPoint(-dist, 0);
		arrowPolygon.addPoint(-dist + headSize, headSize);
		arrowPolygon.addPoint(-dist + headSize, halfWidth);

		arrowPolygon.addPoint(dist - headSize, halfWidth);
		arrowPolygon.addPoint(dist - headSize, headSize);
		arrowPolygon.addPoint(dist, 0);
		arrowPolygon.addPoint(dist - headSize, -headSize);
		arrowPolygon.addPoint(dist - headSize, -halfWidth);

		AffineTransform transform = new AffineTransform();
		transform.translate(xMid, yMid);

		double rotate = Math.atan2(y2 - y1, x2 - x1);
		transform.rotate(rotate);

		return transform.createTransformedShape(arrowPolygon);
	}

	static Shape createArrowShape(double x1, double y1, double x2, double y2)
	{
		double xMid = (x1 + x2) / 2;
		double yMid = (y1 + y2) / 2;
		int dist = (int)(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) / 2);

		int headSize = 4;
		int halfWidth = 1;

		Polygon arrowPolygon = new Polygon();
		arrowPolygon.addPoint(-dist, -halfWidth);
		arrowPolygon.addPoint(-dist, halfWidth);

		arrowPolygon.addPoint(dist - headSize, halfWidth);
		arrowPolygon.addPoint(dist - headSize, headSize);
		arrowPolygon.addPoint(dist, 0);
		arrowPolygon.addPoint(dist - headSize, -headSize);
		arrowPolygon.addPoint(dist - headSize, -halfWidth);

		AffineTransform transform = new AffineTransform();
		transform.translate(xMid, yMid);

		double rotate = Math.atan2(y2 - y1, x2 - x1);
		transform.rotate(rotate);

		return transform.createTransformedShape(arrowPolygon);
	}

}
