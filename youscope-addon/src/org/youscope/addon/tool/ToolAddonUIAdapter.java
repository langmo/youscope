package org.youscope.addon.tool;

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
import org.youscope.common.tools.TextTools;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * An adapter class to simplify tool development.
 * @author Moritz Lang
 *
 */
public abstract class ToolAddonUIAdapter  implements ToolAddonUI 
{
	private static final String DEFAULT_CLOSE_BUTTON_LABEL = "Close";
	
	private boolean maximize = false;
	private Dimension preferredSize = null;
	private boolean resizable = true;
	private boolean maximizable = false;
	private boolean closeButton = false;
	private ArrayList<YouScopeFrameListener> frameListeners = new ArrayList<YouScopeFrameListener>();
	private String title = null;
	private String closeButtonLabel = DEFAULT_CLOSE_BUTTON_LABEL;
	private volatile YouScopeFrame containingFrame = null;
	private final YouScopeClient client;
	private final YouScopeServer server;
	private volatile boolean initialized = false;
	private volatile boolean separateFrame = false;
	private final ToolMetadata metadata;
	private String description = null;
	/**
	 * Constructor.
	 * @param metadata The metadata of the tool.
	 * @param client The YouScope client.
	 * @param server The YouScope server.
	 * @throws AddonException
	 */
	public ToolAddonUIAdapter(final ToolMetadata metadata,  final YouScopeClient client, final YouScopeServer server) throws AddonException 
	{
		this.client = client;
		this.server = server;
		this.metadata = metadata;
	}
	
	/**
	 * Sets a description for this tool. This description is displayed to the user in form of a tooltip at the top right.
	 * Must be called before {@link #toFrame()} or {@link #toPanel(YouScopeFrame)}.
	 * @param description Description of the tool.
	 */
	protected void setDescription(String description)
	{
		this.description = description;
	}
	
	/**
	 * Call this function if the layout of the tool changed in a way such that the UI components need resizing.
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
		if(closeButton || description != null)
		{
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(contentPane, BorderLayout.CENTER);
			if(closeButton)
			{
				JButton commitButton = new JButton(closeButtonLabel);
				commitButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						closeTool();
						
					}
				});
				panel.add(commitButton, BorderLayout.SOUTH);
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
		if(maximize)
			containingFrame.setMaximum(maximize);
		containingFrame.setResizable(resizable);
		if(title!=null && title.length() > 0)
			containingFrame.setTitle(title);
		for(YouScopeFrameListener listener : frameListeners)
		{
			containingFrame.addFrameListener(listener);
		}
		return containingFrame;
	}
	
	private Component getDescriptionComponent()
	{
		if(description == null)
			return null;
		URL iconURL = ToolAddonUIAdapter.class.getClassLoader().getResource("icons/question.png");
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
			private static final long serialVersionUID = -7377779286259970924L;

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
				Dimension preferredSize = ToolAddonUIAdapter.this.preferredSize;
				boolean resizable = ToolAddonUIAdapter.this.resizable;
				if(!resizable && preferredSize != null)
					return preferredSize;
				return super.getMaximumSize();
			}

			@Override
			public Dimension getMinimumSize() 
			{
				Dimension preferredSize = ToolAddonUIAdapter.this.preferredSize;
				boolean resizable = ToolAddonUIAdapter.this.resizable;
				if(!resizable && preferredSize != null)
					return preferredSize;
				return super.getMinimumSize();
			}

			@Override
			public Dimension getPreferredSize() 
			{
				Dimension preferredSize = ToolAddonUIAdapter.this.preferredSize;
				if(preferredSize != null)
					return preferredSize;
				return super.getPreferredSize();
			}
	
		}
		return new UIPanel(contentPane);
	}
	
	/**
	 * Return an AWT Component (typically a JPanel) containing the UI elements of the tool. These elements will
	 * be automatically layouted to fit into a frame or a panel, depending on what is requested. Do not add any
	 * close button, but rather use {@link #setCloseButton(boolean)} and {@link #setCloseButtonLabel(String)}.
	 * @return AWT component containing the UI elements of this tool.
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
	}
	
	/**
	 * Adds a listener to the frame in which the UI element is initialized (or, to which the panel belongs) which is informed e.g. if the frame is closed.
	 * @param listener The listener to add.
	 */
	synchronized void addFrameListener(YouScopeFrameListener listener)
	{
		frameListeners.add(listener);
	}

	/**
	 * Removes a previously added listener.
	 * @param listener The listener to remove.
	 */
	synchronized void removeFrameListener(YouScopeFrameListener listener)
	{
		frameListeners.remove(listener);
	}

	/**
	 * Returns the label of the close button which is displayed at the bottom of a frame, when initialized as a frame and {@link #setCloseButton(boolean)} is set to true.
	 * @return Close button label.
	 */
	protected String getCloseButtonLabel() {
		return closeButtonLabel;
	}

	/**
	 * Sets the label of the close button which is displayed at the bottom of a frame, when initialized as a frame and {@link #setCloseButton(boolean)} is set to true.
	 * Set to null or empty string to restore default close button label.
	 * @param closeButtonLabel Label of the close button, or null.
	 */
	public synchronized void setCloseButtonLabel(String closeButtonLabel) 
	{
		if(closeButtonLabel != null && closeButtonLabel.length() > 0)
			this.closeButtonLabel = closeButtonLabel;
		else
			this.closeButtonLabel  = DEFAULT_CLOSE_BUTTON_LABEL;
	}
	
	/**
	 * Call this function to signal that the tool can be closed.
	 * Function is automatically called when pressing close button.
	 * If initialized in a frame, this will close the frame.
	 * Only call after UI became visible.
	 */
	protected void closeTool()
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
	 * @return True if tool is initialized.
	 */
	protected boolean isInitialized()
	{
		return initialized;
	}
	
	/**
	 * Returns true if {@link #toFrame()} was called. Returns false if {@link #toPanel(YouScopeFrame)} was called, or neither was called.
	 * @return true if tool is in separate frame.
	 */
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
	 * Returns true if a close button is automatically added when tool is initialized as a frame.
	 * @return true if close button is created.
	 */
	protected boolean isCloseButton() {
		return closeButton;
	}

	/**
	 * Set to true if a close button should be automatically added when tool is initialized as a frame. Default is false.
	 * @param closeButton True if close button should be created.
	 */
	protected synchronized void setCloseButton(boolean closeButton) {
		this.closeButton = closeButton;
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
	public ToolMetadata getToolMetadata() {
		return metadata;
	}
}
