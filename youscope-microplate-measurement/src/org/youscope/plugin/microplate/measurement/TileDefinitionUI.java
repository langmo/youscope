/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.microplate.measurement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.AddonMetadataAdapter;
import org.youscope.addon.AddonUIAdapter;
import org.youscope.addon.microplate.MicroplateWellSelectionUI;
import org.youscope.addon.microplate.RectangularMicroplateLayout;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.Well;
import org.youscope.common.microplate.WellLayout;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.IntegerTextField;


/**
 * Component allowing to define a rectangular tile layout, and to select tiles in the defined layout.
 * @author Moritz Lang
 * 
 */
public class TileDefinitionUI extends AddonUIAdapter<AddonMetadata>
{
	protected static final String TITLE = "Tile Definition:";
	private final static String TYPE_IDENTIFIER = "YouScope.TileDefinitionUI";
	private MainPanel mainPanel = null;
	private RightPanel rightPanel = null;
	private final ArrayList<ActionListener> tileChangeListeners = new ArrayList<>(1);
	private final ArrayList<ActionListener> layoutChangeListeners = new ArrayList<>(1);

	private int numTilesX = 12;
	private int numTilesY = 8;
	private double wellWidth = 9000.;
	private double wellHeight = 9000.;
	private final MicroplateWellSelectionUI tileSelectionUI;
	/**
	 * Constructor.
	 * @param client YouScope client.
	 * @param server YouScope server
	 */
	public TileDefinitionUI(YouScopeClient client, YouScopeServer server)
	{
		super(getMetadata(), client, server);
		tileSelectionUI = new MicroplateWellSelectionUI(getClient(), getServer());
		tileSelectionUI.setShowXWellLabels(false);
		tileSelectionUI.setShowYWellLabels(false);
	}
	private static AddonMetadata getMetadata()
	{
		return new AddonMetadataAdapter(TYPE_IDENTIFIER, "TileDefinition", new String[0],
				"Interface to define a rectangular tile layout, and to select tiles in the defined layout.",
				"icons/table-split.png");
	}
	/**
	 * Sets the number of tiles.
	 * @param numTilesX number of tiles in x-direction/columns.
	 * @param numTilesY number of tiles in y-direction/rows.
	 */
	public void setTileLayout(int numTilesX, int numTilesY)
	{
		this.numTilesX = numTilesX;
		this.numTilesY = numTilesY;
		

		if(mainPanel != null)
			mainPanel.updateTiles();	
		if(rightPanel != null)
			rightPanel.updateLayout();
	}
	/**
	 * Sets the size of a well.
	 * @param wellWidth width of a well in um. Also serves to determine distance between tiles.
	 * @param wellHeight height of a well in um. Also serves to determine distance between tiles.
	 */
	public void setWellSize(double wellWidth, double wellHeight)
	{
		this.wellWidth = wellWidth;
		this.wellHeight = wellHeight;
		if(mainPanel != null)
			mainPanel.updateLayout();	
	}
	
	/**
	 * Returns number of tiles in x-direction/columns.
	 * @return number of tiles in x-direction/columns.
	 */
	public int getNumTilesX()
	{
		return numTilesX;
	}
	
	/**
	 * Returns number of tiles in y-direction/rows.
	 * @return number of tiles in y-direction/rows.
	 */
	public int getNumTilesY()
	{
		return numTilesY;
	}
	/**
	 * Returns width of a well in um. Also serves to determine distance between tiles.
	 * @return width of wells in um.
	 */
	public double getWellWidth()
	{
		return wellWidth;
	}
	/**
	 * Returns height of well in um. Also serves to determine distance between tiles.
	 * @return height of wells in um.
	 */
	public double getWellHeight()
	{
		return wellHeight;
	}
	
	private class RightPanel extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -6700615463887257688L;
		private IntegerTextField numXField = new IntegerTextField(100);
		private IntegerTextField numYField = new IntegerTextField(100);
		RightPanel()
		{
			add(new JLabel("Number of tiles in x-direction:"));
			numXField.setMinimalValue(1);
			add(numXField);
			add(new JLabel("Number of tiles in y-direction:"));
			numYField.setMinimalValue(1);
			add(numYField);
			addFillEmpty();
			
			ActionListener userListener = new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					processUserUpdate();
				}
			};
			numXField.addActionListener(userListener);
			numYField.addActionListener(userListener);
			
			// fix sizes to current sizes
			numXField.setPreferredSize(numXField.getPreferredSize());
			numYField.setPreferredSize(numYField.getPreferredSize());
			
			updateLayout();
		}
		private void processUserUpdate()
		{
			numTilesX  = numXField.getValue();
			numTilesY = numYField.getValue();
			mainPanel.updateTiles();		
			notifyMicroplateLayoutChanged();
		}
		private void updateLayout()
		{
			numXField.setValue(numTilesX);
			numYField.setValue(numTilesY);
		}
	}

	private class MainPanel extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long								serialVersionUID	= 5515651002147418067L;
		
		private Rectangle										cellsLocation;
		private Rectangle										boundaryLocation;
		private static final String WIDTH_HEIGHT_LABEL_POSTFIX = "µm";
		// Set default values such that they are large enough during positioning. We set them to the initial values later.
		private JLabel										widthLabel			= new JLabel("XXXXXXXXXX"+WIDTH_HEIGHT_LABEL_POSTFIX);
		private JLabel										heightLabel			= new JLabel("XXXXXXXXXX"+WIDTH_HEIGHT_LABEL_POSTFIX);
		private static final int MAX_WIDTH_HEIGHT = 130;
		private static final int BOUNDARY_SIZE = 20;
		private final Component tileSelectionField;
		/**
		 * Constructor.
		 * @throws AddonException 
		 */
		public MainPanel() throws AddonException
		{
			super();
			
			setOpaque(false);
			tileSelectionUI.addWellsChangeListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					notifyTilesChanged();
				}
			});
	
			setLayout(null);
	
			tileSelectionField = tileSelectionUI.toPanel(getContainingFrame());
			add(tileSelectionField);
			add(heightLabel);
			add(widthLabel);
			
			updateLayout();
		}
		public void updateLayout()
		{
			int width;
			int height;
			if(wellWidth == wellHeight)
			{
				width = MAX_WIDTH_HEIGHT;
				height = MAX_WIDTH_HEIGHT;
			}
			else if(wellWidth > wellHeight)
			{
				width = MAX_WIDTH_HEIGHT;
				height = (int) Math.round(MAX_WIDTH_HEIGHT * wellHeight / wellWidth);
			}
			else
			{
				width = (int) Math.round(MAX_WIDTH_HEIGHT * wellWidth / wellHeight);
				height = MAX_WIDTH_HEIGHT;
			}
			cellsLocation		= new Rectangle(BOUNDARY_SIZE, BOUNDARY_SIZE, width, height);
			boundaryLocation	= new Rectangle(0, 0, width+2*BOUNDARY_SIZE, height+2*BOUNDARY_SIZE);
			
			tileSelectionField.setBounds(cellsLocation);
			try(Formatter formatter = new Formatter())
			{
				widthLabel.setText(formatter.format("%6.2f", wellWidth) + WIDTH_HEIGHT_LABEL_POSTFIX);
			}
			try(Formatter formatter = new Formatter())
			{
				heightLabel.setText(formatter.format("%6.2f", wellHeight) + WIDTH_HEIGHT_LABEL_POSTFIX);
			}
			Dimension elementSize = heightLabel.getPreferredSize();
			heightLabel.setBounds((int) ((boundaryLocation.getMaxX() + cellsLocation.getMaxX())/2 +4), (int)(cellsLocation.getMinY() + cellsLocation.getMaxY() - elementSize.height) / 2, elementSize.width, elementSize.height);
			elementSize = widthLabel.getPreferredSize();
			widthLabel.setBounds((int) (boundaryLocation.getMaxX() +4), (int)(boundaryLocation.getMaxY() +cellsLocation.getMaxY())/2-2-elementSize.height, elementSize.width, elementSize.height);
			
			Dimension size = new Dimension(2*MAX_WIDTH_HEIGHT, (int)(boundaryLocation.getMaxY() + 1));
			setMinimumSize(size);
			setPreferredSize(size);
			
			updateTiles();
		}
		private void updateTiles()
		{
			tileSelectionUI.setMicroplateLayout(new RectangularMicroplateLayout(numTilesX, numTilesY, wellWidth/numTilesX, wellHeight/numTilesY));
		}
	
		@Override
		public void paintComponent(Graphics g)
		{
			Graphics2D g2D = (Graphics2D)g;
			
			// helping lines
			g2D.setColor(Color.LIGHT_GRAY);
			g2D.drawLine((int)cellsLocation.getMaxX()-1, (int)cellsLocation.getMinY(), (int)boundaryLocation.getMaxX()-1, (int)cellsLocation.getMinY());
			g2D.drawLine((int)cellsLocation.getMaxX()-1, (int)cellsLocation.getMaxY() - 1, (int)boundaryLocation.getMaxX()-1, (int)cellsLocation.getMaxY() - 1);
	
			g2D.drawLine((int)cellsLocation.getMaxX() - 1, (int)cellsLocation.getMaxY(), (int)cellsLocation.getMaxX() - 1, (int)boundaryLocation.getMaxY());
			g2D.drawLine((int)cellsLocation.getMinX(), (int)cellsLocation.getMaxY(), (int)cellsLocation.getMinX(), (int)boundaryLocation.getMaxY());
	
			// arrows
			fillDoubleArrow(g2D, (boundaryLocation.getMaxX() + cellsLocation.getMaxX())/2, cellsLocation.getMinY(), (boundaryLocation.getMaxX() + cellsLocation.getMaxX())/2, cellsLocation.getMaxY() - 1);
	
			int withLabelWidth = (int) widthLabel.getPreferredSize().getWidth();
			int endPos = (int) (boundaryLocation.getMaxX() + 6 + withLabelWidth);
			
			fillArrow(g2D, endPos, (boundaryLocation.getMaxY() +cellsLocation.getMaxY())/2, cellsLocation.getMaxX(), (boundaryLocation.getMaxY() +cellsLocation.getMaxY())/2);
			fillArrow(g2D, endPos, (boundaryLocation.getMaxY() +cellsLocation.getMaxY())/2, cellsLocation.getMinX(), (boundaryLocation.getMaxY() +cellsLocation.getMaxY())/2);
	
		}
	
		private void fillDoubleArrow(Graphics2D g2D, double x1, double y1, double x2, double y2)
		{
			Shape arrow = createDoubleArrowShape(x1, y1, x2, y2);
			//g2D.setColor(Color.BLACK);
			g2D.fill(arrow);
			//g2D.setColor(Color.DARK_GRAY);
			g2D.draw(arrow);
		}
	
		private void fillArrow(Graphics2D g2D, double x1, double y1, double x2, double y2)
		{
			Shape arrow = createArrowShape(x1, y1, x2, y2);
			//g2D.setColor(Color.BLACK);
			g2D.fill(arrow);
			//g2D.setColor(Color.DARK_GRAY);
			g2D.draw(arrow);
		}
	
		private Shape createDoubleArrowShape(double x1, double y1, double x2, double y2)
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
	
		private Shape createArrowShape(double x1, double y1, double x2, double y2)
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
	@Override
	protected Component createUI() throws AddonException {
		setMaximizable(true);
		setResizable(true);
		setTitle(TITLE);
		
		mainPanel = new MainPanel(); 
		rightPanel = new RightPanel();
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(mainPanel, BorderLayout.CENTER);
		contentPanel.add(rightPanel, BorderLayout.EAST);
		return contentPanel;
	}
	/**
	 * Returns a collection of the tile layouts selected by the user, or an empty list if no microplate design is defined.
	 * @return Selected tiles layouts.
	 */
	public Set<WellLayout> getSelectedTileLayouts()
	{
		return tileSelectionUI.getSelectedWellLayouts();
	}
	/**
	 * Selects all tiles with the tile identifiers contained in the given collection, and de-selects all others.
	 * @param tiles Tiles which should be selected.
	 */
	public void setSelectedTiles(Collection<Well> tiles)
	{
		tileSelectionUI.setSelectedWells(tiles);
	}
	/**
	 * Returns a collection of the tiles selected by the user, or an empty list if no microplate design is defined.
	 * @return Selected tiles layouts.
	 */
	public Set<Well> getSelectedTiles()
	{
		return tileSelectionUI.getSelectedWells();
	}
	/**
	 * Adds a listener which gets notified if the selected tiles changed.
	 * @param listener The listener to be added.
	 */
	public void addTilesChangeListener(ActionListener listener)
	{
		tileChangeListeners.add(listener);
	}
	
	/**
	 * Removes a previously added listener.
	 * @param listener The listener to be removed.
	 */
	public void removeTilesChangeListener(ActionListener listener)
	{
		tileChangeListeners.remove(listener);
	}
	
	/**
	 * Adds a listener which gets notified if the layout of the microplate changed.
	 * @param listener The listener to be added.
	 */
	public void addLayoutChangeListener(ActionListener listener)
	{
		layoutChangeListeners.add(listener);
	}
	
	/**
	 * Removes a previously added listener.
	 * @param listener The listener to be removed.
	 */
	public void removeLayoutChangeListener(ActionListener listener)
	{
		layoutChangeListeners.remove(listener);
	}
	
	private void notifyTilesChanged()
	{
		for(ActionListener listener : tileChangeListeners)
		{
			listener.actionPerformed(new ActionEvent(this, 1234, "tilesChanged"));
		}
	}
	private void notifyMicroplateLayoutChanged()
	{
		for(ActionListener listener : layoutChangeListeners)
		{
			listener.actionPerformed(new ActionEvent(this, 1235, "layoutChanged"));
		}
	}
}
