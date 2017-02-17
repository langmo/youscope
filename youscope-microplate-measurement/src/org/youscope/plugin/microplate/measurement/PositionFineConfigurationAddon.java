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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.AddonMetadataAdapter;
import org.youscope.addon.AddonUIAdapter;
import org.youscope.addon.microplate.MicroplateWellSelectionUI;
import org.youscope.addon.microplate.RectangularMicroplateLayout;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.Well;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.microplate.WellLayout;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.LiveStreamPanel;

/**
 * Addon to fine configure the positions of wells/tiles.
 * @author Moritz Lang
 * 
 */
public class PositionFineConfigurationAddon  extends AddonUIAdapter<AddonMetadata>
{
	private int currentPositionIndex = 0;
	
	private LiveStreamPanel liveStreamPanel;

	private String										focusDevice = null;
	private String										stageDevice = null;

	private JPopupMenu wellChooserPopup;
	
	private JPopupMenu tileChooserPopup;

	private final JButton wellChooserButton = new JButton();
	private final JButton tileChooserButton = new JButton();
	
	private MicroplateLayout microplateLayout = null;
	private TileConfiguration tileConfiguration = null;
	private final HashMap<PositionInformation, XYAndFocusPosition> configuredPositions = new HashMap<>();
	private List<PositionInformation> selectedPositions = null;
	
	private HashSet<Well> selectedWells = new HashSet<>(200);
	private HashSet<Well> selectedTiles = new HashSet<>(0);

	private final static String TYPE_IDENTIFIER = "YouScope.PositionConfiguration";
	private MicroplateWellSelectionUI wellSelectionUI = null;
	private MicroplateWellSelectionUI tileSelectionUI = null;
	
	private boolean positionConfigurationChanged = false;
	
	private final ArrayList<ActionListener> saveListeners = new ArrayList<>(1);
	/**
	 * Constructor.
	 * @param client YouScope client.
	 * @param server YouScope server
	 * @throws AddonException
	 */
	public PositionFineConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	static AddonMetadata getMetadata()
	{
		return new AddonMetadataAdapter(TYPE_IDENTIFIER, "Position Configuration", new String[0],
				"User interface to fine adjust the positions of the well/tiles to take images in.",
				"icons/magnifier-zoom-fit.png");
	}
	
	/**
	 * Returns the positions of the wells/tiles as configured by this UI.
	 * @return Configured positions.
	 */
	public Map<PositionInformation, XYAndFocusPosition> getConfiguredPositions()
	{
		return configuredPositions;
	}
	
	/**
	 * Adds a listener which gets notified if the positions should be saved.
	 * @param listener listener to add.
	 */
	public void addSaveListener(ActionListener listener)
	{
		saveListeners.add(listener);
	}
	/**
	 * Removes a previously added listener.
	 * @param listener Listener to remove.
	 */
	public void removeSaveListener(ActionListener listener)
	{
		saveListeners.remove(listener);
	}
	
	/**
	 * Sets the layout of the microplate and the wells which should be configured.
	 * @param microplateLayout Layout of microplate. Set to null to allow user to define arbitrary positions.
	 * @param selectedWells Identifiers of wells which should be configured. Set to null to configure all wells.
	 */
	public void setSelectedWells(MicroplateLayout microplateLayout, Set<Well> selectedWells)
	{
		this.microplateLayout = microplateLayout;
		this.selectedWells.clear();
		if(microplateLayout != null && selectedWells != null)
		{
			this.selectedWells.addAll(selectedWells);
		}
		else if(microplateLayout != null)
		{
			for(WellLayout wellLayout : microplateLayout)
			{
				this.selectedWells.add(wellLayout.getWell());
			}
		}
	}
	
	/**
	 * Sets the layout of the tiles and the tiles which should be configured.
	 * @param tileConfiguration Configuration of tiles. Set to null to not measure multiple tiles.
	 * @param selectedTiles Identifiers of tiles which should be configured. Set to null to configure all tiles.
	 */
	public void setSelectedTiles(TileConfiguration tileConfiguration, Set<Well> selectedTiles)
	{
		this.tileConfiguration = tileConfiguration;
		this.selectedTiles.clear();
		if(tileConfiguration != null && selectedTiles != null)
		{
			this.selectedTiles.addAll(selectedTiles);
		}
		else if(tileConfiguration != null)
		{
			for(int y = 0; y < tileConfiguration.getNumTilesY(); y++)
			{
				for(int x = 0; x < tileConfiguration.getNumTilesX(); x++)
				{
					this.selectedTiles.add(new Well(y, x));
				}
			}
		}
	}
	
	/**
	 * Sets the positions already configured for this microplate.
	 * @param configuredPositions Map of configured positions
	 */
	public void setPositions(Map<PositionInformation, XYAndFocusPosition> configuredPositions)
	{
		this.configuredPositions.clear();
		this.configuredPositions.putAll(configuredPositions);
	}
	
	/**
	 * Sets the stage device responsible for X/Y-positioning.
	 * @param stageDevice Identifier of the stage device.
	 */
	public void setStageDevice(String stageDevice)
	{
		this.stageDevice = stageDevice;
	}
	
	/**
	 * Sets the focus device responsible for Z-positioning. Set to null to not save Z-positions.
	 * @param focusDevice device responsible for focussing, or null.
	 */
	public void setFocusDevice(String focusDevice)
	{
		this.focusDevice = focusDevice;
	}
	
	private void createNewPath(double zeroX, double zeroY, double focus)
	{
		for(PositionInformation positionInformation : selectedPositions)
		{
			WellLayout wellLayout = microplateLayout.getWell(positionInformation.getWell());
			double wellX;
			double wellY;
			if(tileConfiguration == null)
			{
				wellX = zeroX + wellLayout.getX() + wellLayout.getWidth() /2;
				wellY = zeroY + wellLayout.getY() + wellLayout.getHeight() /2;
			}
			else
			{
				wellX = zeroX + wellLayout.getX() + (positionInformation.getPosition(1)+0.5) * wellLayout.getWidth() / tileConfiguration.getNumTilesX();
				wellY = zeroY + wellLayout.getY() + (positionInformation.getPosition(0)+0.5) * wellLayout.getHeight() / tileConfiguration.getNumTilesY();
			}
			configuredPositions.put(positionInformation, new XYAndFocusPosition(wellX, wellY, focus));
		}
	
	}

	private PositionInformation getPositionInformation(int positionID)
	{
		if(microplateLayout == null)
		{
			return new PositionInformation(PositionInformation.POSITION_TYPE_MAIN_POSITION, positionID);
		}
		return selectedPositions.get(positionID);
	}
	private XYAndFocusPosition getCurrentPosition()
	{
		Point2D.Double position;
		try {
			position = getMicroscope().getStageDevice(stageDevice).getPosition();
		} catch (RemoteException | MicroscopeException | InterruptedException | DeviceException e) {
			getClient().sendError("Could not get current position of stage " + stageDevice+".", e);
			return null;
		}
		double focus;
		if(focusDevice != null)
		{
			try {
				focus = getMicroscope().getFocusDevice(focusDevice).getFocusPosition();
			} catch (RemoteException | MicroscopeException | InterruptedException | DeviceException e) {
				getClient().sendError("Could not get current position of focus " + focusDevice+".", e);
				return null;
			}
		}
		else
			focus = Double.NaN;
		return new XYAndFocusPosition(position.getX(), position.getY(), focus);
	}
	private void saveCurrentPos()
	{
		XYAndFocusPosition currentPosition = getCurrentPosition();
		if(configuredPositions.isEmpty() && microplateLayout != null)
		{
			PositionInformation zeroPositionInformation = selectedPositions.get(0);
			WellLayout zeroWellLayout = microplateLayout.getWell(zeroPositionInformation.getWell());
			double zeroX;
			double zeroY;
			if(tileConfiguration == null)
			{
				zeroX = currentPosition.getX() - zeroWellLayout.getX() - zeroWellLayout.getWidth()/2;
				zeroY = currentPosition.getY() - zeroWellLayout.getY() - zeroWellLayout.getHeight()/2;
			}
			else
			{
				zeroX = currentPosition.getX() - zeroWellLayout.getX() - (zeroPositionInformation.getPosition(1)+0.5)*zeroWellLayout.getWidth()/tileConfiguration.getNumTilesX();
				zeroY = currentPosition.getY() - zeroWellLayout.getY() - (zeroPositionInformation.getPosition(0)+0.5)*zeroWellLayout.getHeight()/tileConfiguration.getNumTilesY();
			}
			
			createNewPath(zeroX, zeroY, currentPosition.getFocus());
			
			if(microplateLayout != null && !selectedWells.isEmpty())
				wellChooserButton.setEnabled(true);
			if(tileConfiguration != null && !selectedTiles.isEmpty())
				tileChooserButton.setEnabled(true);
		}
		
		PositionInformation positionInformation = getPositionInformation(this.currentPositionIndex);
		configuredPositions.put(positionInformation, currentPosition);
		
		positionConfigurationChanged = true;
	}
	void setAllFocusPositions()
	{
		// Get current focus
		double focus;
		try
		{
			if(focusDevice != null)
			{
				focus = getMicroscope().getFocusDevice(focusDevice).getFocusPosition();
			}
			else
				return;
		}
		catch(Exception e)
		{
			getClient().sendError("Could not get current focus position.", e);
			return;
		}
		
		for(XYAndFocusPosition position : configuredPositions.values())
		{
			position.setFocus(focus);
		}
	}

	private void gotoEast()
	{
		currentPositionIndex++;
		if(microplateLayout != null && currentPositionIndex >= selectedPositions.size())
		{
			int userAnswer = JOptionPane.showConfirmDialog(null, "<html>Iterated through all wells/tiles.<br />Go to first well/tile?", "Iterated through all wells.", JOptionPane.YES_NO_OPTION);
			if(userAnswer != JOptionPane.YES_OPTION)
			{
				currentPositionIndex--;
				return;
			}
			currentPositionIndex = 0;
		}
		gotoPositionIndex(currentPositionIndex);
	}

	private void gotoWest()
	{
		currentPositionIndex--;
		if(currentPositionIndex <0)
		{
			int userAnswer = JOptionPane.showConfirmDialog(null, "<html>Iterated through all wells/tiles.<br />Go to last well/tile?", "Iterated through all wells.", JOptionPane.YES_NO_OPTION);
			if(userAnswer != JOptionPane.YES_OPTION)
			{
				currentPositionIndex++;
				return;
			}
			if(microplateLayout != null)
				currentPositionIndex = selectedPositions.size()-1;
			else
				currentPositionIndex = configuredPositions.size()-1;
		}
		gotoPositionIndex(currentPositionIndex);
	}
	
	private void gotoPosition(PositionInformation positionInformation)
	{
		for(int i=0; i<selectedPositions.size(); i++)
		{
			if(selectedPositions.get(i).equals(positionInformation))
			{
				gotoPositionIndex(i);
				return;
			}
		}
	}

	private void gotoPositionIndex(int positionID)
	{
		if(configuredPositions.isEmpty())
			return;
		final PositionInformation positionInformation = getPositionInformation(positionID);
		if(microplateLayout == null && positionID >= configuredPositions.size())
		{
			configuredPositions.put(positionInformation, getCurrentPosition());
			return;
		}
		
		gotoAbsolutePos(configuredPositions.get(positionInformation));
		
		Runnable runner = new Runnable()
		{
			@Override
			public void run()
			{
				// Actualize title
				String wellName = positionInformation.toString();
				if(isSeparateFrame())
					getContainingFrame().setTitle("Fine-Configuration of " + wellName);
				if(wellChooserButton != null && positionInformation.getWell() != null)
					wellChooserButton.setText(positionInformation.getWell().toString());
				if(tileChooserButton != null && positionInformation.getNumPositions() == 2)
					tileChooserButton.setText(Well.getWellName(positionInformation.getPosition(0), positionInformation.getPosition(1)));
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
		
		currentPositionIndex = positionID;
		
		if(wellSelectionUI != null)
			wellSelectionUI.setSelected(positionInformation.getWell(), true);
		if(tileSelectionUI != null)
			tileSelectionUI.setSelected(new Well(positionInformation.getPosition(0), positionInformation.getPosition(1)), true);
	}
	
	private void gotoAbsolutePos(XYAndFocusPosition position)
	{
		if(position == null)
		{
			getClient().sendError("Provided stage position is null.");
			return;
		}
		try
		{
			getMicroscope().getStageDevice(stageDevice).setPosition(position.getX(), position.getY());
		}
		catch(Exception e)
		{
			getClient().sendError("Could not move stage "+stageDevice+" to position x=" + Double.toString(position.getX()) + ", y=" + Double.toString(position.getY()) + ".", e);
			return;
		}

		// Set focus position
		if(focusDevice != null && !Double.isNaN(position.getFocus()))
		{
			try
			{
				getMicroscope().getFocusDevice(focusDevice).setFocusPosition(position.getFocus());
			}
			catch(Exception e)
			{
				getClient().sendError("Could not move focus device "+focusDevice+" to position " + Double.toString(position.getFocus()) + ".", e);
				return;
			}
		}
	}
	
	private List<PositionInformation> getSelectedPositions()
	{
		ArrayList<PositionInformation> selectedPositions = new ArrayList<>(selectedWells.size() * selectedTiles.size());
		if(selectedWells.isEmpty())
			return selectedPositions;
		
		if(!selectedTiles.isEmpty())
		{
			for(Well well : selectedWells)
			{
				PositionInformation wellInformation = new PositionInformation(well);
				for(Well tile : selectedTiles)
				{
					PositionInformation tileInformation = new PositionInformation(wellInformation, PositionInformation.POSITION_TYPE_YTILE, tile.getWellY());
					selectedPositions.add(new PositionInformation(tileInformation, PositionInformation.POSITION_TYPE_XTILE, tile.getWellX()));
				}
			}
		}
		else
		{
			for(Well well : selectedWells)
			{
				selectedPositions.add(new PositionInformation(well));
			}
		}
		Collections.sort(selectedPositions);
		return selectedPositions;
	}
	@Override
	protected Component createUI() throws AddonException {
		if(stageDevice == null)
			throw new AddonException("No stage device set.");
		setMaximizable(true);
		setResizable(true);
		setClosable(false);
		setTitle("Microplate Position Fine Configuration");
		setPreferredSize(new Dimension(800, 600));
		setShowCloseButton(false);

		selectedPositions = getSelectedPositions();
		
		// Direction buttons.
		Icon eastIcon = ImageLoadingTools.getResourceIcon("icons/arrow.png", "Next Position");
		JButton eastButton;
		if(eastIcon == null)
			eastButton = new JButton("Next");
		else
			eastButton = new JButton(eastIcon);
		eastButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(isSeparateFrame())
					getContainingFrame().startLoading();
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						saveCurrentPos();
						gotoEast();
						if(isSeparateFrame())
							getContainingFrame().endLoading();
					}
				}).start();
			}
		});
		
		Icon westIcon = ImageLoadingTools.getResourceIcon("icons/arrow-180.png", "Previous Position");
		JButton westButton;
		if(eastIcon == null)
			westButton = new JButton("Previous");
		else
			westButton = new JButton(westIcon);
		westButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(isSeparateFrame())
					getContainingFrame().startLoading();
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						saveCurrentPos();
						gotoWest();
						if(isSeparateFrame())
							getContainingFrame().endLoading();
					}
				}).start();
			}
		});
		
		Icon saveIcon = ImageLoadingTools.getResourceIcon("icons/disk.png", "Save Positions");
		JButton saveButton;
		if(saveIcon == null)
			saveButton = new JButton("Save");
		else
			saveButton = new JButton(saveIcon);
		saveButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				saveCurrentPos();
				for(ActionListener listener : saveListeners)
				{
					listener.actionPerformed(new ActionEvent(PositionFineConfigurationAddon.this, 1234, "savePressed"));
				}
				if(PositionFineConfigurationAddon.this.isSeparateFrame())
					getContainingFrame().setVisible(false);
				else
					close();
			}
		});
		
		Icon discardIcon = ImageLoadingTools.getResourceIcon("icons/cross.png", "Discard Positions");
		final JButton discardButton;
		if(discardIcon == null)
			discardButton = new JButton("Discard");
		else
			discardButton = new JButton(discardIcon);
		discardButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(positionConfigurationChanged)
				{
					if(JOptionPane.showConfirmDialog(discardButton, "When quitting without saving, all newly configured or modified positions will be lost.", "Confirm Quit", JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)
						return;
				}
				if(PositionFineConfigurationAddon.this.isSeparateFrame())
					getContainingFrame().setVisible(false);
				else
					close();
			}
		});

		// Main panel
		liveStreamPanel = new LiveStreamPanel(getClient(), getServer());
		getContainingFrame().addFrameListener(new YouScopeFrameListener() {
			
			@Override
			public void frameOpened() {
				if(selectedPositions.size() > 0 && configuredPositions.get(selectedPositions.get(0))!= null)
					gotoPositionIndex(0);
				liveStreamPanel.getFrameListener().frameOpened();
			}
			
			@Override
			public void frameClosed() {
				close();
			}
		});
		
		// "Menu" Panel
		JPanel menuPanelWellAndPosition = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if(microplateLayout != null && !selectedWells.isEmpty())
		{
			menuPanelWellAndPosition.add(new JLabel("Well:"));
			wellChooserButton.setText(selectedPositions.get(0).getWell().toString());
			
			wellChooserPopup = new JPopupMenu();
			wellSelectionUI = new MicroplateWellSelectionUI(getClient(), getServer());
			wellSelectionUI.setMicroplateLayout(microplateLayout);
			wellSelectionUI.setEnabledWells(selectedWells);
			wellSelectionUI.setSelectionMode(MicroplateWellSelectionUI.SelectionMode.EXACTLY_ONE);
			wellSelectionUI.setSelected(selectedPositions.get(0).getWell(), true);
			wellSelectionUI.setCenter(true);
			Component content = wellSelectionUI.toPanel(getContainingFrame());
			wellChooserPopup.add(content);
			wellChooserPopup.setPopupSize(content.getPreferredSize());
		
			wellChooserButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
			        wellChooserPopup.show(wellChooserButton, 0, wellChooserButton.getHeight());
			    }
			});
			wellSelectionUI.addWellsChangeListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e) {
							Well well = wellSelectionUI.getSelectedWell();
							if(well == null)
								return;
							PositionInformation positionInformation = new PositionInformation(well);
							if(tileSelectionUI != null)
							{
								Well tile = tileSelectionUI.getSelectedWell();
								if(tile == null)
									return;
								positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_YTILE, tile.getWellY());
								positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_XTILE, tile.getWellX());
							}
							saveCurrentPos();
							gotoPosition(positionInformation);
						}
					});
			if(configuredPositions.isEmpty())
				wellChooserButton.setEnabled(false);
			menuPanelWellAndPosition.add(wellChooserButton);
		}
		if(microplateLayout != null && !selectedWells.isEmpty() && tileConfiguration != null && !selectedTiles.isEmpty())
		{
			menuPanelWellAndPosition.add(new JLabel("Tile:"));
			tileChooserButton.setText(Well.getWellName(selectedPositions.get(0).getPosition(0), selectedPositions.get(0).getPosition(1)));
			
			tileChooserPopup = new JPopupMenu();
			tileSelectionUI = new MicroplateWellSelectionUI(getClient(), getServer());
			
			// Set tile layout to layout of tiles of first well.
			WellLayout wellLayout = microplateLayout.getWell(0);
			RectangularMicroplateLayout tileLayout = new RectangularMicroplateLayout(tileConfiguration.getNumTilesX(), tileConfiguration.getNumTilesY(), wellLayout.getWidth() / tileConfiguration.getNumTilesX(), wellLayout.getHeight() / tileConfiguration.getNumTilesY());
			tileSelectionUI.setMicroplateLayout(tileLayout);
			tileSelectionUI.setEnabledWells(selectedTiles);
			tileSelectionUI.setSelectionMode(MicroplateWellSelectionUI.SelectionMode.EXACTLY_ONE);
			tileSelectionUI.setSelected(new Well(selectedPositions.get(0).getPosition(0), selectedPositions.get(0).getPosition(1)), true);
			tileSelectionUI.setCenter(true);
			Component content = tileSelectionUI.toPanel(getContainingFrame());
			tileChooserPopup.add(content);
			tileChooserPopup.setPopupSize(content.getPreferredSize());
		
			tileChooserButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
			        tileChooserPopup.show(tileChooserButton, 0, tileChooserButton.getHeight());
			    }
			});
			tileSelectionUI.addWellsChangeListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e) {
							Well well = wellSelectionUI.getSelectedWell();
							if(well == null)
								return;
							PositionInformation positionInformation = new PositionInformation(well);
							if(tileSelectionUI != null)
							{
								Well tile = tileSelectionUI.getSelectedWell();
								if(tile == null)
									return;
								positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_YTILE, tile.getWellY());
								positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_XTILE, tile.getWellX());
							}
							saveCurrentPos();
							gotoPosition(positionInformation);
						}
					});
			if(configuredPositions.isEmpty())
				tileChooserButton.setEnabled(false);
			menuPanelWellAndPosition.add(tileChooserButton);
		}
			
		JPanel menuPanel = new JPanel(new BorderLayout());
		menuPanel.add(menuPanelWellAndPosition, BorderLayout.CENTER);
		if(focusDevice != null)
		{
			JButton setAllZ = new JButton("Set Focus All");
			setAllZ.addActionListener(new ActionListener()
				{

					@Override
					public void actionPerformed(ActionEvent e)
					{
						setAllFocusPositions();
					}
				});
			menuPanel.add(setAllZ, BorderLayout.EAST);
		}
		
		// Combine panels
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(westButton);
		buttonPanel.add(eastButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(discardButton);
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(liveStreamPanel, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		contentPane.add(menuPanel, BorderLayout.NORTH);
		
		return contentPane;
	}
	/**
	 * Function to manually deactivate the live stream used for fine-configuration. Is automatically deactivated if
	 * containing frame is closed.
	 */
	public void close() 
	{
		if(liveStreamPanel != null)
		{
			liveStreamPanel.getFrameListener().frameClosed();
		}
		if(!configuredPositions.isEmpty())
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					gotoPositionIndex(0);
				}
			}).start();
		}
	}
}
