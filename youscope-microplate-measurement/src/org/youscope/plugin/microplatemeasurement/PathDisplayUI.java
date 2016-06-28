package org.youscope.plugin.microplatemeasurement;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.AddonMetadataAdapter;
import org.youscope.addon.AddonUIAdapter;
import org.youscope.addon.pathoptimizer.PathOptimizer;
import org.youscope.addon.pathoptimizer.PathOptimizerPosition;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;

/**
 * Component to display the optimized path through a microplate.
 * @author Moritz Lang
 *
 */
public class PathDisplayUI extends AddonUIAdapter<AddonMetadata>
{
	private volatile Iterable<PathOptimizerPosition> positions = null;
	protected static final String TITLE = "Path through microplate";
	private final static String TYPE_IDENTIFIER = "YouScope.PathDisplay";
	private final JPanel mainPanel = new JPanel();
	/**
	 * Constructor.
	 * @param client YouScope client.
	 * @param server YouScope server
	 * @throws AddonException
	 */
	public PathDisplayUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new JLabel("No path data provided."), BorderLayout.CENTER);
	}
	
	static AddonMetadata getMetadata()
	{
		return new AddonMetadataAdapter(TYPE_IDENTIFIER, "Path display", new String[0]);
	}
	
	/**
	 * Calculates and displays the path.
	 * @param optimizer Optimizer to use to calculate the path.
	 * @param microplatePositions Positions for which path should be calculated.
	 */
	public void calculatePath(final PathOptimizer optimizer, final MicroplatePositionConfiguration microplatePositions)
	{
		Runnable runner = new Runnable()
		{
			@Override
			public void run() {
				try {
					positions = optimizer.getPath(microplatePositions);
				} catch (Exception e) {
					getClient().sendError("Could not calculate optimal path.", e);
					setToError(optimizer, microplatePositions);
					return;
				}
				setTitle(optimizer.getName()+", length "+Double.toString(calculateLength()));
				setToSolution();
			}
		};
		Thread thread = new Thread(runner);
		setToProcessing(thread);
		thread.start();
	}
	private void setToSolution()
	{
		Runnable runner = new Runnable()
		{
			@Override
			public void run() 
			{
				mainPanel.removeAll();
				mainPanel.setLayout(new BorderLayout());
				mainPanel.add(pathComponent, BorderLayout.CENTER);
				mainPanel.revalidate();
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
	private void setToError(final PathOptimizer optimizer, final MicroplatePositionConfiguration microplatePositions)
	{
		Runnable runner = new Runnable()
		{
			@Override
			public void run() 
			{
				mainPanel.removeAll();
				mainPanel.setLayout(new BorderLayout());
				DynamicPanel contentPanel = new DynamicPanel();
				contentPanel.addFillEmpty();
				contentPanel.add(new JLabel("Error occured while processing path."));
				JButton retryButton = new JButton("Retry");
				contentPanel.add(retryButton);
				contentPanel.addFillEmpty();
				retryButton.addActionListener(new ActionListener() 
				{
					@Override
					public void actionPerformed(ActionEvent e) {
						calculatePath(optimizer, microplatePositions);
					}
				});
				mainPanel.add(contentPanel, BorderLayout.CENTER);
				mainPanel.revalidate();
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
	private void setToProcessing(final Thread thread)
	{
		Runnable runner = new Runnable()
		{
			@Override
			public void run() 
			{
				mainPanel.removeAll();
				mainPanel.setLayout(new BorderLayout());
				DynamicPanel contentPanel = new DynamicPanel();
				contentPanel.addFillEmpty();
				contentPanel.add(new JLabel("Processing path. Please wait."));
				JButton interruptButton = new JButton("Interrupt");
				contentPanel.add(interruptButton);
				contentPanel.addFillEmpty();
				interruptButton.addActionListener(new ActionListener() 
				{
					@Override
					public void actionPerformed(ActionEvent e) {
						thread.interrupt();
					}
				});
				mainPanel.add(contentPanel, BorderLayout.CENTER);
				mainPanel.revalidate();
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
	
	@Override
	protected Component createUI() throws AddonException 
	{
		setMaximizable(true);
		setResizable(true);
		setTitle(PathDisplayUI.TITLE);
		setPreferredSize(new Dimension(800,500));
		return mainPanel;
	}
	
	private double calculateLength()
	{
		double length = 0;
		Iterator<PathOptimizerPosition> iterator = positions.iterator(); 
		PathOptimizerPosition start = iterator.next();
		PathOptimizerPosition last = start;
		while(iterator.hasNext())
		{
			PathOptimizerPosition pos = iterator.next();
			length+= Math.abs(last.getX()-pos.getX())+Math.abs(last.getY()-pos.getY());
			last = pos;
		}
		return length+ Math.abs(last.getX()-start.getX())+Math.abs(last.getY()-start.getY());
	}
	private final JComponent pathComponent = new JComponent()
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 4735187053061407336L;
		private static final String NO_PATH_TEXT = "No Wells selected!";
		@Override
	    public void paintComponent(Graphics grp)
	    {
			grp.setColor(getBackground());
			grp.fillRect(0, 0, getWidth(), getHeight());
			Iterable<PathOptimizerPosition> positions = PathDisplayUI.this.positions;
			if(positions == null || !positions.iterator().hasNext())
			{
				grp.setColor(getForeground());
				grp.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
	        	int strWidth = grp.getFontMetrics().stringWidth(NO_PATH_TEXT);
	        	int strHeight = grp.getFontMetrics().getHeight();
	        	grp.drawString(NO_PATH_TEXT, (getWidth()-strWidth)/2, (getHeight()-strHeight)/2);
	        	return;
			}
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;
			for(PathOptimizerPosition position : positions)
			{
				if(position.getX() < minX)
					minX = position.getX();
				if(position.getX() > maxX)
					maxX = position.getX();
				
				if(position.getY() < minY)
					minY = position.getY();
				if(position.getY() > maxY)
					maxY = position.getY();
			}
			Graphics2D g = (Graphics2D)grp;
			AffineTransform oldTransform = g.getTransform();
			
			// create transform
			double width = getWidth();
			double height = getHeight();
			double imageWidth = maxX-minX;
			double imageHeight = maxY-minY;
			double zoom;
			if(width/imageWidth < height/imageHeight)
				zoom = width/imageWidth*0.95;
			else
				zoom = height/imageHeight*0.95;
			double scaledImageWidth = imageWidth * zoom;
			double scaledImageHeight = imageHeight * zoom;
			double deltaX = (width-scaledImageWidth) / 2;
			double deltaY = (height-scaledImageHeight) / 2;
			AffineTransform scale = AffineTransform.getScaleInstance(zoom, zoom);
			AffineTransform move = AffineTransform.getTranslateInstance(deltaX, deltaY);
			move.concatenate(scale);
			move.concatenate(AffineTransform.getTranslateInstance(-minX, -minY));
			g.transform(move);
			int pointSize = (int) Math.round(5 / zoom);
			int fontSize = (int)Math.round(10 / zoom);
			
			Iterator<PathOptimizerPosition> iterator = positions.iterator(); 
			PathOptimizerPosition start = iterator.next();
			PathOptimizerPosition last = start;
			g.setColor(getForeground());
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
			int id = 0;
			while(iterator.hasNext())
			{
				id++;
				PathOptimizerPosition pos = iterator.next();
				g.drawLine((int)last.getX(), (int)last.getY(), (int)pos.getX(), (int)pos.getY());
				g.fillOval((int)last.getX()-pointSize/2, (int)last.getY()-pointSize/2,  pointSize, pointSize);
				g.drawString(Integer.toString(id), (int)last.getX()+pointSize/2+1, (int)last.getY());
				last = pos;
			}
			g.drawLine((int)last.getX(), (int)last.getY(), (int)start.getX(), (int)start.getY());
			g.fillOval((int)last.getX()-pointSize/2, (int)last.getY()-pointSize/2,  pointSize, pointSize);
			g.drawString(Integer.toString(++id), (int)last.getX()+pointSize/2+1, (int)last.getY());
			
			g.setTransform(oldTransform);
	    }
	};
}
