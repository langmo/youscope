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
package org.youscope.plugin.measurementviewer.standalone;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.uielements.ImageLoadingTools;

class MinimalFrameImpl extends JFrame implements YouScopeFrame {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 3189488985375352182L;
	
	private final ArrayList<YouScopeFrameListener> frameListeners = new ArrayList<>();
	private final JPanel mainPanel = new JPanel(new BorderLayout());
	MinimalFrameImpl()
	{
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		this.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				synchronized (frameListeners) 
				{
					for(YouScopeFrameListener listener : frameListeners)
					{
						listener.frameOpened();
					}
				}
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// do nothing.
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// do nothing.
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// do nothing.
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				synchronized (frameListeners) 
				{
					for(YouScopeFrameListener listener : frameListeners)
					{
						listener.frameClosed();
					}
				}
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// do nothing.
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// do nothing.
			}
		});
		
		// Set tray icon image.
		final String TRAY_ICON_URL16 = "org/youscope/plugin/measurementviewer/standalone/images/icon-16.png";
		final String TRAY_ICON_URL32 = "org/youscope/plugin/measurementviewer/standalone/images/icon-32.png";
		final String TRAY_ICON_URL96 = "org/youscope/plugin/measurementviewer/standalone/images/icon-96.png";
		final String TRAY_ICON_URL194 = "org/youscope/plugin/measurementviewer/standalone/images/icon-194.png";
		ArrayList<Image> trayIcons = new ArrayList<Image>(4);
		Image trayIcon16 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL16, "tray icon");
		if(trayIcon16 != null)
			trayIcons.add(trayIcon16);
		Image trayIcon32 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL32, "tray icon");
		if(trayIcon32 != null)
			trayIcons.add(trayIcon32);
		Image trayIcon96 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL96, "tray icon");
		if(trayIcon96 != null)
			trayIcons.add(trayIcon96);
		Image trayIcon194 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL194, "tray icon");
		if(trayIcon194 != null)
			trayIcons.add(trayIcon194);
		if(trayIcons.size()>0)
			this.setIconImages(trayIcons);
	}
	@Override
	public void setContentPane(Component contentPane) 
	{
		mainPanel.removeAll();
		mainPanel.add(contentPane, BorderLayout.CENTER);
		mainPanel.revalidate();
	}

	@Override
	public void setMargins(int left, int top, int right, int bottom) {
		mainPanel.setBorder(new EmptyBorder(top, left, bottom, right));
	}

	@Override
	public void setMaximum(boolean maximum) {
		// do nothing, yet.
	}

	@Override
	public YouScopeFrame createChildFrame() {
		return createFrame();
	}

	@Override
	public YouScopeFrame createModalChildFrame() {
		return createFrame();
	}

	@Override
	public YouScopeFrame createFrame() {
		return createFrame();
	}

	@Override
	public void addChildFrame(YouScopeFrame childFrame) {
		// do nothing, yet.
	}

	@Override
	public void addModalChildFrame(YouScopeFrame childFrame) {
		// do nothing, yet.
	}

	@Override
	public void setClosable(boolean closable) {
		// do nothing, yet.
	}

	@Override
	public void setMaximizable(boolean maximizable) {
		/// do nothing, yet.
	}

	@Override
	public boolean isClosable() {
		return true;
	}

	@Override
	public boolean isMaximizable() {
		return true;
	}

	@Override
	public void addFrameListener(YouScopeFrameListener listener) {
		synchronized(frameListeners)
		{
			frameListeners.add(listener);
		}
	}

	@Override
	public void removeFrameListener(YouScopeFrameListener listener) {
		synchronized(frameListeners)
		{
			frameListeners.remove(listener);
		}
	}

	@Override
	public void setToErrorState(String message, Exception e) {
		// do nothing, yet.
	}

	@Override
	public void startInitializing() {
		// do nothing, yet.
	}

	@Override
	public void startLoading() {
		// do nothing, yet.
	}

	@Override
	public void endLoading() {
		// do nothing, yet.
	}

	@Override
	public void relocateFrameTo(YouScopeFrame targetFrame) {
		// do nothing, yet.
	}

}
