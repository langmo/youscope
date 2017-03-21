package org.youscope.plugin.measurementviewer;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.youscope.uielements.ImageLoadingTools;

class MeasurementTilingControl extends JPanel 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -2723513207043552227L;
	private final ArrayList<TilingListener> tilingListeners = new ArrayList<>(1);
	private final JButton removeTileButton;
	public MeasurementTilingControl() 
	{
		super(new FlowLayout(FlowLayout.LEFT));
		setOpaque(false);
		Icon addTileIcon = ImageLoadingTools.getResourceIcon("icons/application-split.png", "Add Tile");
		JButton addTileButton;
		if(addTileIcon == null)
			addTileButton = new JButton("Add Tile");
		else
			addTileButton = new JButton(addTileIcon);
		addTileButton.setOpaque(false);
		addTileButton.setToolTipText("Add Tile");
		addTileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				for(TilingListener listener : tilingListeners)
				{
					listener.addTiling();
				}
			}
		});
		
		Icon removeTileIcon = ImageLoadingTools.getResourceIcon("icons/cross-script.png", "Remove Tile");
		if(removeTileIcon == null)
			removeTileButton = new JButton("Remove Tile");
		else
			removeTileButton = new JButton(removeTileIcon);
		removeTileButton.setOpaque(false);
		removeTileButton.setToolTipText("Remove Tile");
		removeTileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				for(TilingListener listener : tilingListeners)
				{
					listener.removeTiling();
				}
			}
		});
		add(addTileButton);
		add(removeTileButton);
	}
	public void setIsTileCloseable(final boolean closeable)
	{
		Runnable runner = new Runnable()
		{
			@Override
			public void run() {
				removeTileButton.setVisible(closeable);
				revalidate();
			}
	
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
	public static interface TilingListener
	{
		public void addTiling();
		public void removeTiling();
	}
	public void addTilingListener(TilingListener listener)
	{
		tilingListeners.add(listener);
	}
	public void removeTilingListener(TilingListener listener)
	{
		tilingListeners.remove(listener);
	}
}
