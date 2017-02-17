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
package org.youscope.addon;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.youscope.addon.AddonException;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.util.TextTools;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * An adapter class to simplify UI addon development.
 * @author Moritz Lang
 * @param <T> Type of metadata returned by addon.
 *
 */
public abstract class AddonUIAdapter<T extends AddonMetadata>  implements AddonUI<T> 
{
	private static final String DEFAULT_CLOSE_BUTTON_LABEL = "Close";
	
	
	private boolean maximize = false;
	private Dimension preferredSize = null;
	private boolean resizable = true;
	private boolean closable = true;
	private boolean maximizable = false;
	private boolean showCloseButton = true;
	private ArrayList<YouScopeFrameListener> frameListeners = new ArrayList<YouScopeFrameListener>();
	private String title = null;
	private String closeButtonLabel = DEFAULT_CLOSE_BUTTON_LABEL;
	private volatile YouScopeFrame containingFrame = null;
	private final YouScopeClient client;
	private final YouScopeServer server;
	private volatile boolean initialized = false;
	private volatile boolean separateFrame = false;
	private final T metadata;
	private String description = null;
	/**
	 * Constructor.
	 * @param metadata The metadata of the addon.
	 * @param client The YouScope client.
	 * @param server The YouScope server.
	 */
	public AddonUIAdapter(final T metadata,  final YouScopeClient client, final YouScopeServer server) 
	{
		this.client = client;
		this.server = server;
		this.metadata = metadata;
	}
	
	/**
	 * Sets a description for this addon. This description is displayed to the user in form of a tooltip at the top right.
	 * Must be called before {@link #toFrame()} or {@link #toPanel(YouScopeFrame)}.
	 * @param description Description of the addon.
	 */
	protected void setDescription(String description)
	{
		this.description = description;
	}
	
	/**
	 * Call this function if the layout of the addon changed in a way such that the components need resizing.
	 */
	protected void notifyLayoutChanged()
	{
		YouScopeFrame frame = getContainingFrame();
		if(frame != null)
			frame.pack();
	}
	
	@Override
	public synchronized YouScopeFrame toFrame()  throws AddonException
	{
		separateFrame = true;
		containingFrame = getClient().createFrame();
		for(YouScopeFrameListener listener : frameListeners)
		{
			containingFrame.addFrameListener(listener);
		}
		return setupFrame();
	}

	@Override
	public synchronized Component toPanel(YouScopeFrame containingFrame)  throws AddonException
	{
		this.containingFrame = containingFrame;
		for(YouScopeFrameListener listener : frameListeners)
		{
			containingFrame.addFrameListener(listener);
		}
		return setupPanel();
	}
	
	private YouScopeFrame setupFrame() throws AddonException
	{
		Component contentPane;
		contentPane = createUI();
		initialized = true;
		if(showCloseButton || description != null)
		{
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(contentPane, BorderLayout.CENTER);
			if(showCloseButton)
			{
				JButton closeButton = new JButton(closeButtonLabel);
				closeButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						closeAddon();
						
					}
				});
				panel.add(closeButton, BorderLayout.SOUTH);
			}
			if(description != null)
			{
				panel.add(getDescriptionComponent(), BorderLayout.NORTH);
			}
			contentPane = panel;
		}
		containingFrame.setContentPane(contentPane);
		if(preferredSize != null)
			containingFrame.setSize(preferredSize);
		else
			containingFrame.pack();
		containingFrame.setMaximizable(maximizable);
		containingFrame.setClosable(closable);
		if(maximize)
			containingFrame.setMaximum(maximize);
		containingFrame.setResizable(resizable);
		if(title!=null && title.length() > 0)
			containingFrame.setTitle(title);
		return containingFrame;
	}
	
	private Component getDescriptionComponent()
	{
		if(description == null)
			return null;
		URL iconURL = AddonUIAdapter.class.getClassLoader().getResource("icons/question.png");
		JLabel label;
		if (iconURL != null)
		{
			label = new JLabel(new ImageIcon(iconURL, "Description"), SwingConstants.RIGHT);
		}
		else
			label = new JLabel("Help", SwingConstants.RIGHT);
		
		
		label.setOpaque(false);
		label.setToolTipText("<html><div style=\"width:500px\">"+TextTools.toHTML(description)+"</div></html>");
		
		return label;
	}
	
	private Component setupPanel() throws AddonException
	{
		Component contentPane;
		contentPane = createUI();
		initialized = true;
		class UIPanel extends JPanel
		{

			/**
			 * Serial Version UID
			 */
			private static final long serialVersionUID = -7377469286259970924L;

			public UIPanel(Component contentPane)
			{
				super(new BorderLayout());
				if(description != null)
					add(getDescriptionComponent(), BorderLayout.NORTH);
				add(contentPane, BorderLayout.CENTER);
				setOpaque(false);
			}
			@Override
			public Dimension getMaximumSize() 
			{
				Dimension preferredSize = AddonUIAdapter.this.preferredSize;
				boolean resizable = AddonUIAdapter.this.resizable;
				if(!resizable && preferredSize != null)
					return preferredSize;
				return super.getMaximumSize();
			}

			@Override
			public Dimension getMinimumSize() 
			{
				Dimension preferredSize = AddonUIAdapter.this.preferredSize;
				boolean resizable = AddonUIAdapter.this.resizable;
				if(!resizable && preferredSize != null)
					return preferredSize;
				return super.getMinimumSize();
			}

			@Override
			public Dimension getPreferredSize() 
			{
				Dimension preferredSize = AddonUIAdapter.this.preferredSize;
				if(preferredSize != null)
					return preferredSize;
				return super.getPreferredSize();
			}
	
		}
		return new UIPanel(contentPane);
	}
	
	/**
	 * Return a Component (typically a JPanel) containing the UI elements of the addon. These elements will
	 * be automatically layouted to fit into a frame or a panel, depending on what is requested. Do not add any
	 * confirm or close button, but rather change the label of the button which is automatically added (see {@link #setShowCloseButton(boolean)} and {@link #setCloseButtonLabel(String)}).
	 * @return Component containing the UI elements of this addon.
	 * 
	 */
	protected abstract Component createUI() throws AddonException;

	/**
	 * Indicates if this UI element should be maximized if it is a frame.
	 * @return true if should be maximized when frame.
	 */
	protected synchronized boolean isMaximize() {
		return maximize;
	}

	/**
	 * Sets if this UI element should be maximized if it is a frame.
	 * @param maximize true if should be maximized when frame.
	 */
	protected synchronized void setMaximize(boolean maximize) {
		this.maximize = maximize;
	}

	/**
	 * Returns the preferred size of this UI element, or null if size should be calculated automatically.
	 * @return preferred size, or null.
	 */
	protected Dimension getPreferredSize() {
		return (Dimension) preferredSize.clone();
	}

	/**
	 * Set the preferred size of this UI element. Set to null, if size should be calculated automatically.
	 * @param preferredSize preferred size, or null.
	 */
	protected synchronized void setPreferredSize(Dimension preferredSize) {
		this.preferredSize = (Dimension) preferredSize.clone();
	}

	/**
	 * Returns if this element should be resizable. If initialized as a panel, this implies that the maximal size will be
	 * identical to the preferred size. If initialized as a frame, this implies forbidding resizing by the user.
	 * @return true if resizable.
	 */
	protected boolean isResizable() {
		return resizable;
	}

	/**
	 * Sets if this element should be resizable. If initialized as a panel, this implies that the maximal size will be
	 * identical to the preferred size. If initialized as a frame, this implies forbidding resizing by the user.
	 * @param resizable true if resizable.
	 */
	protected synchronized void setResizable(boolean resizable) {
		this.resizable = resizable;
	}
	
	/**
	 * Returns if this element should be closable. If initialized as a frame, the close option of the frame will be displayed.
	 * If initialized as a panel, this option has no effect.
	 * @return true if closable.
	 */
	protected boolean isClosable() {
		return closable;
	}

	/**
	 * Sets if this element should be closable. If initialized as a frame, the close option of the frame will be displayed.
	 * If initialized as a panel, this option has no effect.
	 * @param closable true if closable.
	 */
	protected synchronized void setClosable(boolean closable) {
		this.closable = closable;
	}
	
	/**
	 * Returns if this UI element will be maximizable if initialized as a frame. Has no effect if initialized as a panel.
	 * @return true if maximizable.
	 */
	protected boolean isMaximizable() {
		return maximizable;
	}

	/**
	 * Sets if this UI element will be maximizable if initialized as a frame. Has no effect if initialized as a panel.
	 * @param maximizable true if maximizable.
	 */
	protected synchronized void setMaximizable(boolean maximizable) {
		this.maximizable = maximizable;
	}

	/**
	 * Returns the title of this UI element. If initialized as a frame, then the frame title will be set to the title. If
	 * initialized as a panel, a titled border will be drawn around the UI element. If null or empty string, neither will be done.
	 * @return Title, or null if no title is set.
	 */
	protected String getTitle() {
		return title;
	}
	/**
	 * Sets the title of this UI element. If initialized as a frame, then the frame title will be set to the title. If
	 * initialized as a panel, a titled border will be drawn around the UI element. Set to null or empty string to do neither.
	 */
	protected synchronized void setTitle(String title) {
		this.title = title;
		if(isInitialized() && isSeparateFrame() && containingFrame != null)
		{
			containingFrame.setTitle(title);
		}
	}
	
	/**
	 * Adds a listener to the frame in which the UI element is initialized (or, to which the panel belongs) which is informed e.g. if the frame is closed.
	 * @param listener The listener to add.
	 */
	protected synchronized void addFrameListener(YouScopeFrameListener listener)
	{
		frameListeners.add(listener);
	}

	/**
	 * Removes a previously added listener.
	 * @param listener The listener to remove.
	 */
	protected synchronized void removeFrameListener(YouScopeFrameListener listener)
	{
		frameListeners.remove(listener);
	}

	/**
	 * Returns the label of the commit button which is displayed at the bottom of a frame, when initialized as a frame.
	 * @return Commit button label.
	 */
	protected String getCloseButtonLabel() {
		return closeButtonLabel;
	}

	/**
	 * Sets the label of the commit button which is displayed at the bottom of a frame, when initialized as a frame.
	 * Set to null or empty string to restore default commit button label.
	 * @param commitButtonLabel Label of the commit button, or null.
	 */
	protected synchronized void setCloseButtonLabel(String commitButtonLabel) 
	{
		if(commitButtonLabel != null && commitButtonLabel.length() > 0)
			this.closeButtonLabel = commitButtonLabel;
		else
			this.closeButtonLabel  = DEFAULT_CLOSE_BUTTON_LABEL;
	}
	
	/**
	 * Call this function to signal that the addon user interface has finished doing what it is supposed to do, and might be closed now.
	 * Function is automatically called when pressing the close button.
	 * If initialized in a frame, this will close the frame.
	 * Only call after UI became visible.
	 */
	protected void closeAddon()
	{
		if(separateFrame)
				containingFrame.setVisible(false);
	}
	
	/**
	 * Call this function to notify that an error occurred. Only call this function if the error did not occur as a direct response to a function call (in this case, throw a corresponding error to the function caller).
	 * @param message A description in which step the error happened.
	 * @param error The error which happened.
	 */
	protected void sendErrorMessage(String message, Throwable error) 
	{
		client.sendError(message, error);
	}

	/**
	 * Send a general message meant for information. Typically, this message is simply logged.
	 * @param message The message to send
	 */
	protected void sendMessage(String message)
	{
		client.sendMessage(message);
	}
	
	/**
	 * Returns true if the functions toXXXFrame() or toPanel() was called, that is, if
	 * createUI() was called or not.
	 * 
	 * @return True if addon is initialized.
	 */
	protected boolean isInitialized()
	{
		return initialized;
	}
	
	protected boolean isSeparateFrame()
	{
		return separateFrame;
	}
	
	/**
	 * Returns the frame containing this addon. Either, if toXXXFrame() was called, the newly created
	 * frame for this addon, or, if toPanel() was called, the parent frame, i.e. the frame in which this
	 * addon (is supposed to) have been visible. Returns null if neither function was called.
	 * @return The frame containing this addon.
	 */
	protected YouScopeFrame getContainingFrame()
	{
		return containingFrame;
	}

	/**
	 * Returns true if a commit button is automatically added when addon is initialized as a frame.
	 * @return true if commit button is created.
	 */
	protected boolean isShowCloseButton() {
		return showCloseButton;
	}

	/**
	 * Set to true if a commit button should be automatically added when addon is initialized as a frame.
	 * @param commitButton True if commit button should be created.
	 */
	protected synchronized void setShowCloseButton(boolean commitButton) {
		this.showCloseButton = commitButton;
	}
	
	/**
	 * Returns the YouScope client.
	 * @return The YouScope client.
	 */
	protected YouScopeClient getClient()
	{
		return client;
	}
	/**
	 * Returns the YouScope server.
	 * @return the YouScope server.
	 */
	protected YouScopeServer getServer()
	{
		return server;
	}
	
	/**
	 * Returns the microscope. Same as getServer().getMicroscope().
	 * @return The microscope.
	 * @throws RemoteException 
	 */
	protected Microscope getMicroscope() throws RemoteException
	{
		return server.getMicroscope();
	}

	@Override
	public T getAddonMetadata() {
		return metadata;
	}
}
