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
package org.youscope.uielements.plaf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

import org.youscope.uielements.AddonToolTip;
import org.youscope.uielements.DescriptionPanel;
import org.youscope.uielements.LinkLabel;


/**
 * Default/basic UI delegate for the {@link AddonToolTip}.
 * @author Moritz Lang
 *
 */
public class BasicAddonToolTipUI extends AddonToolTipUI
{
	private AddonToolTip toolTip;	    
	private final DescriptionPanel descriptionPanel = new DescriptionPanel();
	private final JScrollPane descriptionPanelScrollPane = new JScrollPane(descriptionPanel);
	private final LinkLabel linkLabel = new LinkLabel("open in wiki ");
	// extra panel containing all for the border.
	private final JPanel mainPanel = new JPanel(new BorderLayout());
	private static Popup permanentPopup;
	private static AddonToolTip permanentToolTip;
	
	/**
	 * Name of the UI property defining the background color.
	 */
	public static final String PROPERTY_BACKGROUND = "ToolTip.background";
	/**
	 * Name of the UI property defining the foreground color
	 */
	public static final String PROPERTY_FOREGROUND = "ToolTip.foreground";
	
	/**
	 * Name of the UI property defining the foreground color
	 */
	public static final String PROPERTY_BORDER = "ToolTip.border";
	
	private final PropertyChangeListener changeListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) 
		{
			updateInternal();
			
		}
	};
	
	MouseListener childMouseListener = new MouseAdapter()
	{
		private boolean inside = false;
		@Override
		public void mouseEntered(MouseEvent e) 
		{
			if(inside)
				return;
			inside = true;
			if(!toolTip.isTemporary())
				return;
			if(toolTip.isShowing())
				showPermanent();
		}

		@Override
		public void mouseExited(MouseEvent e) 
		{
			if(!inside)
				return;
			if(toolTip.contains(SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), toolTip)))
				return;
			inside = false;
			if(!toolTip.isTemporary())
				hidePermanent();
			if(toolTip.getComponent() != null)
			{ 
				MouseEvent event = new MouseEvent(toolTip.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger());
				for(MouseListener listener : toolTip.getComponent().getMouseListeners())
					listener.mouseExited(event);
			}
		}
	};
	
	private void updateInternal()
	{
		descriptionPanel.setHeader(toolTip.getAddonName());
		descriptionPanel.setText(toolTip.getAddonDescription());
		linkLabel.setVisible(isShowLink());
		descriptionPanel.setBackground(toolTip.getBackground());
		descriptionPanel.setForeground(toolTip.getForeground());
		linkLabel.setBackground(toolTip.getBackground());
		linkLabel.setForeground(toolTip.getForeground());
	}
	/**
	 * Constructor.
	 */
	public BasicAddonToolTipUI()
	{
		descriptionPanel.addMouseListener(childMouseListener);
		descriptionPanelScrollPane.addMouseListener(childMouseListener);
		linkLabel.addMouseListener(childMouseListener);
		mainPanel.addMouseListener(childMouseListener);
		
		descriptionPanelScrollPane.setPreferredSize(new Dimension(250, 100));
		
		linkLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    	linkLabel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e1) {
				if(!isShowLink())
					return;
				String page = toolTip.getAddonWikiPage();
				String wikiPage = "https://github.com/langmo/youscope/wiki/"+page;
				try
				{
					Desktop.getDesktop().browse(new URI(wikiPage));
				}
				catch(IOException | URISyntaxException e)
				{
					JOptionPane.showMessageDialog(toolTip, "Could not open website "+wikiPage+" in system browser. Error message: "+e.getMessage(), "Could not open website", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});

    	mainPanel.add(descriptionPanelScrollPane, BorderLayout.CENTER);
    	mainPanel.add(linkLabel, BorderLayout.SOUTH);
    	mainPanel.setOpaque(false);
	}
	
    @Override
    public void installUI(JComponent c) 
    {
    	toolTip = (AddonToolTip) c;
    	installDefaults();
    	installComponents();
    	installModelChangeListener();
    	
    	updateInternal();
    }
    
    protected void installDefaults()
    {
    	Color bg = toolTip.getBackground();
    	if(bg == null || bg instanceof UIResource)
    	{
    		bg = UIManager.getColor(PROPERTY_BACKGROUND);
    		toolTip.setBackground(bg==null ? new ColorUIResource(Color.WHITE):bg);
        	toolTip.setOpaque(true);
    	}
    	Color fg = toolTip.getForeground();
    	if(fg == null || fg instanceof UIResource)
    	{
    		fg = UIManager.getColor(PROPERTY_FOREGROUND);
    		toolTip.setForeground(fg==null ? new ColorUIResource(Color.BLACK):fg);
    	}
    	Border border = toolTip.getBorder();
    	if(border == null || border instanceof UIResource)
    	{
    		toolTip.setBorder(null);
    		mainPanel.setBorder(UIManager.getBorder(PROPERTY_BORDER));
	    	descriptionPanelScrollPane.setBorder(null);
			//linkLabel.setBorder(new MatteBorder(1, 0, 0, 0, toolTip.getForeground()));
    	}
    	
    }
    protected void installModelChangeListener() 
    {
    	toolTip.addPropertyChangeListener(changeListener); 
    	toolTip.addMouseListener(childMouseListener);
    }
    protected void uninstallModelChangeListener() 
    {
    	toolTip.removePropertyChangeListener(changeListener);
    	toolTip.removeMouseListener(childMouseListener);
    }
    protected void installComponents()
    {
    	toolTip.removeAll();
    	toolTip.setLayout(new BorderLayout());
    	toolTip.add(mainPanel, BorderLayout.CENTER);
    }

    private boolean isShowLink()
    {
    	return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) && toolTip.getAddonWikiPage() != null;
    }
    
    protected void uninstallComponents()
    {
    	toolTip.remove(mainPanel);
    }

    @Override
    public void uninstallUI(JComponent c) {
    	uninstallModelChangeListener();
    	uninstallComponents();
        toolTip = null;

    }

    public static ComponentUI createUI(JComponent c) {
    	BasicAddonToolTipUI ui = new BasicAddonToolTipUI();   	
    	return ui;
    }

	
	private void showPermanent()
	{
		if(permanentPopup != null)
		{
			if(permanentToolTip == toolTip)
				return;
			permanentPopup.hide();
			permanentPopup = null;
			permanentToolTip = null;
		}
		
		permanentToolTip = new AddonToolTip(toolTip);
		permanentToolTip.setTemporary(false);
		Point location = toolTip.getLocationOnScreen();
		PopupFactory popupFactory = PopupFactory.getSharedInstance();
		
		permanentPopup = popupFactory.getPopup(toolTip.getComponent(), permanentToolTip, location.x, location.y);
		
		permanentPopup.show();
	}

	private void hidePermanent()
	{
		if(permanentPopup == null || permanentToolTip != toolTip)
			return;
		permanentPopup.hide();
		permanentPopup = null;
		permanentToolTip = null;
	}
}
