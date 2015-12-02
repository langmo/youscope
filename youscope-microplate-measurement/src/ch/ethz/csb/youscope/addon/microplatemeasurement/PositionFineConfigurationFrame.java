/**
 * 
 */
package ch.ethz.csb.youscope.addon.microplatemeasurement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.client.uielements.LiveStreamPanel;
import ch.ethz.csb.youscope.shared.Well;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author langmo
 * 
 */
public class PositionFineConfigurationFrame
{
	private Well											well				= new Well(0,0);
	private int												positionX			= -1;						// will automatically jump to the next heigher one at start.
	private int												positionY			= 0;
	
	private static final Dimension buttonPrefSize = new Dimension(30, 30);

	//private ContinousMeasurementAndControlsPanel			measurementPanel;
	private LiveStreamPanel liveStreamPanel;

	private final String										focusDevice;
	private final String										stageDevice;

	private final MicroplatePositionConfigurationDTO	configuration;

	private YouScopeFrame									frame;
	
	private JPopupMenu wellChooserPopup;
	
	private JPopupMenu positionChooserPopup;

	private JButton wellButton = null;
	private JButton positionButton = null;
	
	private final YouScopeClient client;
	private final YouScopeServer server;
	
	private XYAndFocusPositionDTO zeroPosition = null;
	
	/**
	 * Constructor.
	 * @param client Interface to the UI.
	 * @param server Interface to the microscope.
	 * @param frame The frame in which the fine configuration is displayed.
	 * @param configuration The position configuration which positions should be fine configured.
	 * @param focusDevice The focus device for which the focus value should be stored, or null if focus device value should not be stored.
	 * @param stageDevice The stage device which should be used to change the position.
	 * @param forceNewConfiguration True, if a new path should be generated, even if the old path is still valid. Remark, that if the path is not valid, a new one will be constructed anyway.
	 */
	public PositionFineConfigurationFrame(YouScopeClient client, YouScopeServer server, YouScopeFrame frame, MicroplatePositionConfigurationDTO configuration, String focusDevice, String stageDevice, boolean forceNewConfiguration)
	{
		this.frame = frame;
		this.configuration = configuration;
		this.focusDevice = focusDevice;
		this.client = client;
		this.server = server;
		this.stageDevice = stageDevice;

		frame.setTitle("Position Fine-Configuration");
		frame.setClosable(true);
		frame.setResizable(true);
		frame.setMaximizable(true);

		if(!configuration.isInitialized() || forceNewConfiguration)
		{
			boolean result = createNewPath();
			if(!result)
			{
				frame.setToErrorState("Zero position was not set", null);
				return;
			}
		}

		initialize();

		gotoEast();
	}

	private void initialize()
	{
		// Direction buttons.
		ImageIcon eastIcon = ImageLoadingTools.getResourceIcon("bonus/icons-24/arrow.png", "go right");
		JButton saveAndEastButton;
		if(eastIcon == null)
			saveAndEastButton = new JButton("Next Position");
		else
			saveAndEastButton = new JButton("Next Position", eastIcon);
		saveAndEastButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				frame.startLoading();
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						saveCurrentPos();
						gotoEast();
						frame.endLoading();
					}
				}).start();
			}
		});
		
		ImageIcon westIcon = ImageLoadingTools.getResourceIcon("bonus/icons-24/arrow-180.png", "go left");
		JButton saveAndWestButton;
		if(eastIcon == null)
			saveAndWestButton = new JButton("Previous Position");
		else
			saveAndWestButton = new JButton("Previous Position", westIcon);
		saveAndWestButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				frame.startLoading();
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						saveCurrentPos();
						gotoWest();
						frame.endLoading();
					}
				}).start();
			}
		});
		
		// OK Button.
		JButton okButton = new JButton("Save and Exit");
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						saveCurrentPos();
						returnToFirstWell();
					}
				}).start();
				
				frame.setVisible(false);
			}
		});

		// Main panel
		liveStreamPanel = new LiveStreamPanel(client, server);
		liveStreamPanel.setAutoStartStream(true);
		frame.addFrameListener(liveStreamPanel.getFrameListener());
		
		// Well chooser popup
		wellChooserPopup = new JPopupMenu();
		JPanel wellChooserGrid = new JPanel();
		
		int numColumns;
		int numRows;
		if(configuration.isAliasMicroplate())
		{
			numColumns = (int)Math.ceil(Math.sqrt(configuration.getNumWellsX()));
			numRows = (int)Math.ceil(((double)configuration.getNumWellsX()) / numColumns);
			
		}
		else
		{
			numRows=configuration.getNumWellsY();
			numColumns=configuration.getNumWellsX();
		}
		
		wellChooserGrid.setLayout(new GridLayout(numRows, numColumns));
		if(numRows > 16 || numColumns > 24)
		{
			wellChooserPopup.add(new JScrollPane(wellChooserGrid));
			wellChooserPopup.setPopupSize(new Dimension(24 * buttonPrefSize.width, 16*buttonPrefSize.height));
		}
		else
		{
			wellChooserPopup.add(wellChooserGrid);
		}
		
		
		for(int wellY = 0; wellY < configuration.getNumWellsY(); wellY ++)
		{
			for(int wellX = 0; wellX < configuration.getNumWellsX(); wellX ++)
			{
				WellButton button = new WellButton(new Well(wellY, wellX));
				wellChooserGrid.add(button);
			}
		}
		
		if(!configuration.isSinglePosition())
		{
			// position chooser popup
			positionChooserPopup = new JPopupMenu();
			
			JPanel positionChooserGrid = new JPanel();
			numRows = configuration.getWellNumPositionsY();
			numColumns = configuration.getWellNumPositionsX();
			positionChooserGrid.setLayout(new GridLayout(numRows, numColumns));
			for(int posY = 0; posY < configuration.getWellNumPositionsY(); posY ++)
			{
				for(int posX = 0; posX < configuration.getWellNumPositionsX(); posX ++)
				{
					PositionButton button = new PositionButton(posY, posX);
					positionChooserGrid.add(button);
				}
			}
			if(numRows > 16 || numColumns > 24)
			{
				positionChooserPopup.add(new JScrollPane(positionChooserGrid));
				positionChooserPopup.setPopupSize(new Dimension(24 * buttonPrefSize.width, 16*buttonPrefSize.height));
			}
			else
			{
				positionChooserPopup.add(positionChooserGrid);
			}
		}
		
		// "Menu" Panel
		JPanel menuPanelWellAndPosition = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if(configuration.isAliasMicroplate())
			menuPanelWellAndPosition.add(new JLabel("Current position:"));
		else
			menuPanelWellAndPosition.add(new JLabel("Current well:"));
		wellButton = new JButton("XXXX");
		wellButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent arg0)
				{
			        wellChooserPopup.show(wellButton, 0, wellButton.getHeight());
			    }
			});
		menuPanelWellAndPosition.add(wellButton);
		
		if(!configuration.isSinglePosition())
		{
			menuPanelWellAndPosition.add(new JLabel("Current position:"));
			positionButton = new JButton("XXXX");
			positionButton.addActionListener(new ActionListener()
				{
	
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
				        positionChooserPopup.show(positionButton, 0, positionButton.getHeight());
				    }
				});
			menuPanelWellAndPosition.add(positionButton);
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
		JPanel southButtonPanel = new JPanel(new GridLayout(2, 1, 2, 2));
		JPanel nextPreviousPanel = new JPanel(new GridLayout(1, 2, 2, 2));
		nextPreviousPanel.add(saveAndWestButton);
		nextPreviousPanel.add(saveAndEastButton);
		southButtonPanel.add(nextPreviousPanel);
		southButtonPanel.add(okButton);
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(liveStreamPanel, BorderLayout.CENTER);
		contentPane.add(southButtonPanel, BorderLayout.SOUTH);
		contentPane.add(menuPanel, BorderLayout.NORTH);

		// Setup frame
		frame.setContentPane(contentPane);
		frame.setSize(new Dimension(800, 600));
		frame.setMaximum(true);
	}

	private class WellButton extends JButton implements ActionListener
	{
		/**
		 * Serial version UID.
		 */
		private static final long	serialVersionUID	= -4832070555032806678L;
		private final Well well;
		private final Color rollOverColor = new Color(0F, 0.7F, 0F);
		WellButton(Well well)
		{
			if(configuration.isAliasMicroplate())
				setText(Integer.toString(well.getWellX()+1));
			else
			setText(well.getWellName());
			if(configuration.isMeasureWell(well))
				addActionListener(this);
			else
				setEnabled(false);
			this.well = well;
		}
		@Override
		public Dimension getPreferredSize()
		{
			return buttonPrefSize;
		}
		@Override
		public Dimension getMinimumSize()
		{
			return buttonPrefSize;
		}
		@Override
		public void paintComponent(Graphics g)
		{
			if(getModel().isRollover() || (PositionFineConfigurationFrame.this.well.compareTo(well) == 0))
				g.setColor(rollOverColor);
			else if(isEnabled())
				g.setColor(Color.GREEN);
			else
				g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, getWidth()-1, getHeight()-1);
			g.setColor(Color.DARK_GRAY);
			g.drawRect(0, 0, getWidth()-1, getHeight()-1);
			
			if(isEnabled())
				g.setColor(Color.BLACK);
			else
				g.setColor(Color.DARK_GRAY);
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
		
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			wellChooserPopup.setVisible(false);
			saveCurrentPos();
			gotoWellPos(well, positionY, positionX);
		}
	}
	
	private class PositionButton extends JButton implements ActionListener
	{
		/**
		 * Serial version UID.
		 */
		private static final long	serialVersionUID	= -4832070555032806678L;
		private int posY;
		private int posX;
		private final Color rollOverColor = new Color(0F, 0.7F, 0F);
		PositionButton(int posY, int posX)
		{
			setText(Integer.toString(posY+1) + " / " + Integer.toString(posX+1));
			if(configuration.isMeasurePosition(posY, posX))
				addActionListener(this);
			else
				setEnabled(false);
			this.posX = posX;
			this.posY = posY;
		}
		
		@Override
		public Dimension getPreferredSize()
		{
			return buttonPrefSize;
		}
		@Override
		public Dimension getMinimumSize()
		{
			return buttonPrefSize;
		}
		@Override
		public void paintComponent(Graphics g)
		{
			if(getModel().isRollover() || (PositionFineConfigurationFrame.this.positionX == posX && PositionFineConfigurationFrame.this.positionY == posY))
				g.setColor(rollOverColor);
			else if(isEnabled())
				g.setColor(Color.GREEN);
			else
				g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, getWidth()-1, getHeight()-1);
			g.setColor(Color.DARK_GRAY);
			g.drawRect(0, 0, getWidth()-1, getHeight()-1);
			
			if(isEnabled())
				g.setColor(Color.BLACK);
			else
				g.setColor(Color.DARK_GRAY);
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
		
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			positionChooserPopup.setVisible(false);
			saveCurrentPos();
			gotoWellPos(well, posY, posX);
		}
	}
	
	private boolean createNewPath()
	{
		double wellWidth = configuration.getWellWidth();
		double wellHeight = configuration.getWellHeight();

		for(int wellY = 0; wellY < configuration.getNumWellsY(); wellY++)
		{
			for(int wellX = 0; wellX < configuration.getNumWellsX(); wellX++)
			{
				if(!configuration.isMeasureWell(new Well(wellY, wellX)))
					continue;
				zeroPosition = askForZeroPosition(new Well(wellY, wellX));
				if(zeroPosition == null)
					return false;
				double A1x = zeroPosition.getX() - wellWidth * wellX;
				double A1y = zeroPosition.getY() - wellHeight * wellY;
				configuration.createDefaultPath(A1x, A1y, zeroPosition.getFocus());
				return true;
			}
		}
		return false;
	}
	
	private boolean returnToFirstWell()
	{
		for(int wellY = 0; wellY < configuration.getNumWellsY(); wellY++)
		{
			for(int wellX = 0; wellX < configuration.getNumWellsX(); wellX++)
			{
				if(!configuration.isMeasureWell(new Well(wellY, wellX)))
					continue;
				
				for(int posX = 0; posX < configuration.getWellNumPositionsX(); posX++)
				{
					for(int posY = 0; posY < configuration.getWellNumPositionsY(); posY++)
					{
						if(!configuration.isMeasurePosition(positionY, positionX))
							continue;
						
						gotoWellPos(new Well(wellY, wellX), posY, posX);
						return true;
					}
				}
			}
		}
		return false;
	}

	private XYAndFocusPositionDTO askForZeroPosition(Well well)
	{
		if(!configuration.isAliasMicroplate())
		{
			int userAnswer = JOptionPane.showConfirmDialog(null, "<html><body><p>Please manually move microscope to the center of well " + well.getWellName() + " and press OK (cancel to abort)!</p><p style=\"color:#AA0000\"><br>Warning: Just pressing OK without correctly adjusting microscope position may lead to serious microscope damage!</p></body></html>", "Move Microscope to first position", JOptionPane.OK_CANCEL_OPTION);
			if(userAnswer != JOptionPane.OK_OPTION)
				return null;
		}

		// Get current position (= position of center of first well).
		Point2D.Double zeroPositionXY;
		try
		{
			zeroPositionXY = server.getMicroscope().getStageDevice(stageDevice).getPosition();
		}
		catch(Exception e)
		{
			client.sendError("Could not obtain current stage position.", e);
			return null;
		}

		double zeroPositionFocus;
		if(focusDevice != null)
		{
			try
			{
				zeroPositionFocus = server.getMicroscope().getFocusDevice(focusDevice).getFocusPosition();
			}
			catch(Exception e)
			{
				client.sendError("Could not obtain current focus position", e);
				return null;
			}
		}
		else
		{
			zeroPositionFocus = 0;
		}

		return new XYAndFocusPositionDTO(zeroPositionXY.x, zeroPositionXY.y, zeroPositionFocus);
	}

	private void saveCurrentPos()
	{
		if(!configuration.isMeasurePosition(positionY, positionX) || !configuration.isMeasureWell(well))
			return;
		try
		{
			Point2D.Double position = server.getMicroscope().getStageDevice(stageDevice).getPosition();

			double focus = 0.0;
			// Store focus position.
			if(focusDevice != null)
			{
				focus = server.getMicroscope().getFocusDevice(focusDevice).getFocusPosition();
			}

			configuration.setPosition(new XYAndFocusPositionDTO(position.x, position.y, focus), well, positionY, positionX);
		}
		catch(Exception e)
		{
			client.sendError("Could not save current position.", e);
			return;
		}
	}
	void setAllFocusPositions()
	{
		// Get current focus
		double focus;
		try
		{
			if(focusDevice != null)
			{
				focus = server.getMicroscope().getFocusDevice(focusDevice).getFocusPosition();
			}
			else
				return;
		}
		catch(Exception e)
		{
			client.sendError("Could not get current focus position.", e);
			return;
		}
		
		// Set all focus positions
		int positionX = 0;
		int positionY = 0;
		int wellX = 0;
		int wellY = 0;
		while(true)
		{
			positionX++;
			if(positionX >= configuration.getWellNumPositionsX())
			{
				positionX = 0;
				positionY++;
				if(positionY >= configuration.getWellNumPositionsY())
				{
					positionY = 0;
					wellX++;
					if(wellX >= configuration.getNumWellsX())
					{
						wellX = 0;
						wellY++;
						if(wellY >= configuration.getNumWellsY())
						{
							break;
						}
					}
				}
			}
			Well tempWell = new Well(wellY, wellX);
			if(configuration.isMeasurePosition(positionY, positionX) && configuration.isMeasureWell(tempWell))
			{
				XYAndFocusPositionDTO focusConf = configuration.getPosition(tempWell, positionY, positionX);
				focusConf.setFocus(focus);
				configuration.setPosition(focusConf, tempWell, positionY, positionX);
			}
		}
	}

	private void gotoEast()
	{
		int positionX = this.positionX;
		int positionY = this.positionY;
		int wellX = well.getWellX();
		int wellY = well.getWellY();
		while(true)
		{
			positionX++;
			if(positionX >= configuration.getWellNumPositionsX())
			{
				positionX = 0;
				positionY++;
				if(positionY >= configuration.getWellNumPositionsY())
				{
					positionY = 0;
					wellX++;
					if(wellX >= configuration.getNumWellsX())
					{
						wellX = 0;
						wellY++;
						if(wellY >= configuration.getNumWellsY())
						{
							int userAnswer = JOptionPane.showConfirmDialog(null, "Iterated through all wells/positions. Restart?", "No more wells/positions in this direction.", JOptionPane.YES_NO_OPTION);
							if(userAnswer != JOptionPane.YES_OPTION)
								return;
							wellY = 0;
						}
					}
				}
			}
			if(configuration.isMeasurePosition(positionY, positionX) && configuration.isMeasureWell(new Well(wellY, wellX)))
				break;
		}
		gotoWellPos(new Well(wellY, wellX), positionY, positionX);
	}

	private void gotoWest()
	{
		int positionX = this.positionX;
		int positionY = this.positionY;
		int wellX = well.getWellX();
		int wellY = well.getWellY();
		while(true)
		{
			positionX--;
			if(positionX < 0)
			{
				positionX = configuration.getWellNumPositionsX() - 1;
				positionY--;
				if(positionY < 0)
				{
					positionY = configuration.getWellNumPositionsY() - 1;
					wellX--;
					if(wellX < 0)
					{
						wellX = configuration.getNumWellsX() - 1;
						wellY--;
						if(wellY < 0)
						{
							int userAnswer = JOptionPane.showConfirmDialog(null, "Iterated through all wells/positions. Restart?", "No more wells/positions in this direction.", JOptionPane.YES_NO_OPTION);
							if(userAnswer != JOptionPane.YES_OPTION)
								return;
							wellY = configuration.getNumWellsY() - 1;
						}
					}
				}
			}
			if(configuration.isMeasurePosition(positionY, positionX) && configuration.isMeasureWell(new Well(wellY, wellX)))
				break;
		}
		gotoWellPos(new Well(wellY, wellX), positionY, positionX);
	}
	
	@SuppressWarnings("unused")
	private void gotoSouth()
	{
		int positionX = this.positionX;
		int positionY = this.positionY;
		int wellX = well.getWellX();
		int wellY = well.getWellY();
		while(true)
		{
			positionY++;
			if(positionY >= configuration.getWellNumPositionsY())
			{
				positionY = 0;
				positionX++;
				if(positionX >= configuration.getWellNumPositionsX())
				{
					positionX = 0;
					wellY++;
					if(wellY >= configuration.getNumWellsY())
					{
						wellY = 0;
						wellX++;
						if(wellX >= configuration.getNumWellsX())
						{
							int userAnswer = JOptionPane.showConfirmDialog(null, "Iterated through all wells/positions. Restart?", "No more wells/positions in this direction.", JOptionPane.YES_NO_OPTION);
							if(userAnswer != JOptionPane.YES_OPTION)
								return;
							wellX = 0;
						}
					}
				}
			}
			if(configuration.isMeasurePosition(positionY, positionX) && configuration.isMeasureWell(new Well(wellY, wellX)))
				break;
		}
		gotoWellPos(new Well(wellY, wellX), positionY, positionX);
	}
	
	@SuppressWarnings("unused")
	private void gotoNorth()
	{
		int positionX = this.positionX;
		int positionY = this.positionY;
		int wellX = well.getWellX();
		int wellY = well.getWellY();
		while(true)
		{
			positionY--;
			if(positionY < 0)
			{
				positionY = configuration.getWellNumPositionsY() - 1;
				positionX--;
				if(positionX < 0)
				{
					positionX = configuration.getWellNumPositionsX() - 1;
					wellY--;
					if(wellY < 0)
					{
						wellY = configuration.getNumWellsY() - 1;
						wellX--;
						if(wellX < 0)
						{
							int userAnswer = JOptionPane.showConfirmDialog(null, "Iterated through all wells/positions. Restart?", "No more wells/positions in this direction.", JOptionPane.YES_NO_OPTION);
							if(userAnswer != JOptionPane.YES_OPTION)
								return;
							wellX = configuration.getNumWellsX() - 1;
						}
					}
				}
			}
			if(configuration.isMeasurePosition(positionY, positionX) && configuration.isMeasureWell(new Well(wellY, wellX)))
				break;
		}
		gotoWellPos(new Well(wellY, wellX), positionY, positionX);
	}

	private void gotoWellPos(final Well well, final int positionY, final int positionX)
	{
		if(!configuration.isMeasurePosition(positionY, positionX) || !configuration.isMeasureWell(well))
			return;

		this.well = well;
		this.positionX = positionX;
		this.positionY = positionY;

		// Set XY-position
		XYAndFocusPositionDTO position = configuration.getPosition(well, positionY, positionX);
		if(position != null && !(configuration.isAliasMicroplate() && zeroPosition!=null && position.compareTo(zeroPosition) == 0))
		{
			double xPos = position.getX();
			double yPos = position.getY();
			double focusPos;
			if(focusDevice != null)
				focusPos = position.getFocus();
			else
				focusPos = -1;
			gotoAbsolutePos(xPos, yPos, focusDevice, focusPos);
		}
		
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				// Actualize title
				if(configuration.isAliasMicroplate())
				{
					frame.setTitle("Fine-Configuration of Position " + Integer.toString(well.getWellX() + 1));
					if(wellButton != null)
						wellButton.setText(Integer.toString(well.getWellX()+1));
				}
				else
				{
					String title = "Fine-Configuration of Well " + well.getWellName();
					if(configuration.getWellNumPositionsX() > 1 || configuration.getWellNumPositionsY() > 1)
					{
						title += ", Position " + "" + Integer.toString(positionY+1) + "/" + Integer.toString(positionX+1);
					}
					frame.setTitle(title);
					if(wellButton != null)
						wellButton.setText(well.getWellName());
					if(positionButton != null)
						positionButton.setText(Integer.toString(positionY+1) + " / " + Integer.toString(positionX+1));
					
				}
			}
		});
	}
	
	private void gotoAbsolutePos(double x, double y, String focusDevice, double focus)
	{
		try
		{
			server.getMicroscope().getStageDevice(stageDevice).setPosition(x, y);
		}
		catch(Exception e)
		{
			client.sendError("Could not go to position x=" + Double.toString(x) + ", y=" + Double.toString(y) + ".", e);
			return;
		}

		// Set focus position
		if(focusDevice != null)
		{
			try
			{
				server.getMicroscope().getFocusDevice(focusDevice).setFocusPosition(focus);
			}
			catch(Exception e)
			{
				client.sendError("Could not set focus to " + Double.toString(focus) + ".", e);
				return;
			}
		}
	}
}
