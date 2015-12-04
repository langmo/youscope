package org.youscope.addon.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
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
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.tools.TextTools;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * An adapter class to simplify UI addon development.
 * @author mlang
 * @param <C> 
 *
 */
public abstract class ComponentAddonUIAdapter<C extends Configuration>  implements ComponentAddonUI<C> 
{
	private static final String DEFAULT_COMMIT_BUTTON_LABEL = "Commit";
	
	
	private boolean maximize = false;
	private Dimension preferredSize = null;
	private boolean resizable = true;
	private boolean maximizable = false;
	private boolean commitButton = true;
	private ArrayList<YouScopeFrameListener> frameListeners = new ArrayList<YouScopeFrameListener>();
	private ArrayList<ComponentAddonUIListener<? super C>> configurationListeners = new ArrayList<ComponentAddonUIListener<? super C>>();
	private String title = null;
	private String commitButtonLabel = DEFAULT_COMMIT_BUTTON_LABEL;
	private volatile YouScopeFrame containingFrame = null;
	private final YouScopeClient client;
	private final YouScopeServer server;
	private C configuration;
	private volatile boolean initialized = false;
	private volatile boolean separateFrame = false;
	private final ComponentMetadata<C> metadata;
	private String description = null;
	/**
	 * Constructor.
	 * @param metadata The metadata of the addon.
	 * @param client The YouScope client.
	 * @param server The YouScope server.
	 * @throws AddonException
	 */
	public ComponentAddonUIAdapter(final ComponentMetadata<C> metadata,  final YouScopeClient client, final YouScopeServer server) throws AddonException 
	{
		this.client = client;
		this.server = server;
		this.metadata = metadata;
		Class<C> configurationClass = metadata.getConfigurationClass();
		Constructor<C> constructor;
		try {
			constructor = configurationClass.getDeclaredConstructor();
		} catch (NoSuchMethodException e) {
			throw new AddonException("Configuration class " + configurationClass.getName() + " does not have a no-arguments constructor.", e);
		} catch (SecurityException e) {
			throw new AddonException("Could not get constructor of configuration class " + configurationClass.getName() + ".", e);
		}
		constructor.setAccessible(true);
		try {
			configuration = constructor.newInstance();
		} 
		catch (Exception e) 
		{
			throw new AddonException("Could not construct default configuration of configuration class " + configurationClass.getName() + ".", e);
		}
	}
	
	/**
	 * Sets a description for this configuration. This description is displayed to the user in form of a tooltip at the top right.
	 * Must be called before {@link #toFrame()} or {@link #toPanel(YouScopeFrame)}.
	 * @param description Description of the configuration.
	 */
	protected void setDescription(String description)
	{
		this.description = description;
	}
	
	/**
	 * Call this function if the layout of the configuration addon changed in a way such that the components need resizing.
	 */
	protected void notifyLayoutChanged()
	{
		YouScopeFrame frame = getContainingFrame();
		if(frame != null)
			frame.pack();
	}
	
	/**
	 * Returns the configuration class.
	 * @return Configuration class.
	 */
	protected Class<C> getConfigurationClass()
	{
		return metadata.getConfigurationClass();
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
	
	@Override
	public void addUIListener(ComponentAddonUIListener<? super C> listener) 
	{
		synchronized(configurationListeners)
		{
			configurationListeners.add(listener);
		}
	}

	@Override
	public void removeUIListener(ComponentAddonUIListener<? super C> listener) 
	{
		synchronized(configurationListeners)
		{
			configurationListeners.remove(listener);
		}
	}
	
	private YouScopeFrame setupFrame() throws AddonException
	{
		Component contentPane;
		contentPane = createUI(configuration);
		initialized = true;
		if(commitButton || description != null)
		{
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(contentPane, BorderLayout.CENTER);
			if(commitButton)
			{
				JButton commitButton = new JButton(commitButtonLabel);
				commitButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						configurationFinished();
						
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
		URL iconURL = ComponentAddonUIAdapter.class.getClassLoader().getResource("icons/question.png");
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
		contentPane = createUI(configuration);
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
				Dimension preferredSize = ComponentAddonUIAdapter.this.preferredSize;
				boolean resizable = ComponentAddonUIAdapter.this.resizable;
				if(!resizable && preferredSize != null)
					return preferredSize;
				return super.getMaximumSize();
			}

			@Override
			public Dimension getMinimumSize() 
			{
				Dimension preferredSize = ComponentAddonUIAdapter.this.preferredSize;
				boolean resizable = ComponentAddonUIAdapter.this.resizable;
				if(!resizable && preferredSize != null)
					return preferredSize;
				return super.getMinimumSize();
			}

			@Override
			public Dimension getPreferredSize() 
			{
				Dimension preferredSize = ComponentAddonUIAdapter.this.preferredSize;
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
	 * confirm button, but rather change the label of the button which is automatically added (<code>setCommitButtonLabel</code>).
	 * @param configuration The current configuration which should be loaded.
	 * @return Component containing the UI elements of this addon.
	 * 
	 */
	protected abstract Component createUI(C configuration) throws AddonException;

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
	 * Returns the label of the commit button which is displayed at the bottom of a frame, when initialized as a frame.
	 * @return Commit button label.
	 */
	protected String getCommitButtonLabel() {
		return commitButtonLabel;
	}

	/**
	 * Sets the label of the commit button which is displayed at the bottom of a frame, when initialized as a frame.
	 * Set to null or empty string to restore default commit button label.
	 * @param commitButtonLabel Label of the commit button, or null.
	 */
	public synchronized void setCommitButtonLabel(String commitButtonLabel) 
	{
		if(commitButtonLabel != null && commitButtonLabel.length() > 0)
			this.commitButtonLabel = commitButtonLabel;
		else
			this.commitButtonLabel  = DEFAULT_COMMIT_BUTTON_LABEL;
	}
	
	/**
	 * Call this function to signal that configuration in the UI has finished.
	 * Function is automatically called when pressing commit button.
	 * If initialized in a frame, this will close the frame.
	 * Only call after UI became visible.
	 */
	protected void configurationFinished()
	{
		C configuration = getConfiguration();
		try 
		{
			configuration.checkConfiguration();
		} 
		catch(ConfigurationException e)
		{
			YouScopeFrame errorFrame = ComponentAddonTools.displayConfigurationInvalid(e, getClient());
			getContainingFrame().addModalChildFrame(errorFrame);
			errorFrame.setVisible(true);
			return;
		}
		
		synchronized(configurationListeners)
		{
			for(ComponentAddonUIListener<? super C> configurationListener : configurationListeners)
			{
				configurationListener.configurationFinished(configuration);
			}
		}
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
	protected boolean isCommitButton() {
		return commitButton;
	}

	/**
	 * Set to true if a commit button should be automatically added when addon is initialized as a frame.
	 * @param commitButton True if commit button should be created.
	 */
	protected synchronized void setCommitButton(boolean commitButton) {
		this.commitButton = commitButton;
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
	public void setConfiguration(Configuration configuration)
			throws AddonException, ConfigurationException 
	{
		String typeIdentifier = metadata.getTypeIdentifier();
		Class<C> configurationClass = metadata.getConfigurationClass();
		if(configuration == null)
			throw new AddonException("Configuration which should be loaded is null.");
		if(isInitialized())
			throw new AddonException("Configuration can only be set before toXXXFrame() or toPanel() is called.");
		if(!typeIdentifier.equals(configuration.getTypeIdentifier()))
			throw new AddonException("Provided configuration has type identifier " + configuration.getTypeIdentifier()+", however, type identifier "+typeIdentifier+" is required.");
		if(!configurationClass.isInstance(configuration))
			throw new AddonException("Configuration type identifier " + configuration.getTypeIdentifier()+" is valid for this configuration addon, however, the class of the configuration " + configuration.getClass().getName() + " is not a subclass of " + configurationClass.getName()+".");
		this.configuration = configurationClass.cast(configuration);
	}

	/**
	 * Is called when the current state of all forms etc. should be saved into the configuration.
	 * Guaranteed to be called only after createUI() has been called.
	 * @param configuration The configuration in which changes should be saved. 
	 */
	protected abstract void commitChanges(C configuration);
	
	@Override
	public C getConfiguration()
	{
		if(isInitialized())
			commitChanges(configuration);
		return configuration;
	}

	@Override
	public ComponentMetadata<C> getComponentMetadata() {
		return metadata;
	}
}
