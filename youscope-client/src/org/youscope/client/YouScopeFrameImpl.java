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
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.uielements.GlassPane;
import org.youscope.uielements.ImageLoadingTools;

/**
 * @author Moritz Lang
 */
final class YouScopeFrameImpl extends JInternalFrame
{
    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 8568759837709867044L;

    /**
	 * URL of the image displayed on top of every frame.
	 * If null, no image will be displayed.
	 */
	private static final String					TOP_IMAGE_URL		= null;
	
    /**
     * URL of the icon of this frame.
     */
    private static final String                 TRAY_ICON_URL       = "org/youscope/client/images/icon-16.png";

    
    /**
     * Icon of the frame.
     */
    private static final Icon                  TRAY_ICON;

    /**
	 * image displayed on top of every frame
	 */
	private static final BufferedImage				TOP_IMAGE;

	/**
	 * Load static elements.
	 */
	static
	{
		BufferedImage topImageTemp = null;
		if(TOP_IMAGE_URL!=null)
		{
			URL topImageURL = YouScopeFrameImpl.class.getClassLoader().getResource(TOP_IMAGE_URL);
			if(topImageURL != null)
			{
				try
				{
					topImageTemp = ImageIO.read(topImageURL);
				}
				catch(@SuppressWarnings("unused") Exception e)
				{
					// do nothing.
				}
			}		
		}
		TOP_IMAGE = topImageTemp;
		
		if(TRAY_ICON_URL != null)
			TRAY_ICON     = ImageLoadingTools.getResourceIcon(TRAY_ICON_URL, "Tray icon");
		else
			TRAY_ICON = null;
	}
	
    // ////////////////////////////////////////
    // Individual frame members.
    // ////////////////////////////////////////

    /**
     * The content of every frame gets displayed in this element.
     */
    private final JPanel contentPane = new JPanel(new BorderLayout(0, 0));

    /**
     * List of all child frames.
     */
    private final ArrayList<ChildFrame> childFrames = new ArrayList<ChildFrame>();

    /**
     * List of all listeners getting close(), open() events from this frame.
     */
    private final Set<YouScopeFrameListener> frameListeners = new HashSet<YouScopeFrameListener>();

    /**
     * Private constructor. Initializes the frame layout.
     */
    private YouScopeFrameImpl()
    {
        super("", true, true, true, false);

        setupFrame();
    }

    private void setupFrame()
    {
    	// Set Layout and insets of the content pane.
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));

		// add logo at top.
		if(TOP_IMAGE != null)
		{
			JPanel newPane = new JPanel(new BorderLayout());
			JLabel imageLabel = new JLabel(new ImageIcon(TOP_IMAGE));
			imageLabel.setHorizontalAlignment(SwingConstants.LEFT);
			imageLabel.setBackground(Color.WHITE);
			imageLabel.setOpaque(true);
			imageLabel.setBorder(new EmptyBorder(2, 5, 1, 1));
			newPane.add(imageLabel, BorderLayout.NORTH);
			newPane.add(contentPane, BorderLayout.CENTER);
			setContentPane(newPane);
		}
		else
		{
			setContentPane(contentPane);
		}
        if(TRAY_ICON != null)
        {
            this.setFrameIcon(TRAY_ICON);
        }

        // Add listener to get informed about events happening to the frame triggered by the user.
        final FrameListener frameListener = new FrameListener();
        addInternalFrameListener(frameListener);
        addVetoableChangeListener(frameListener);
        addComponentListener(frameListener);
    }

    /**
     * Creates a new top-level frame. To create child frames, use the respective functions of the parent frame.
     *
     * @return New top level frame. Frame is initialized to be not visible.
     */
    public static YouScopeFrame createTopLevelFrame()
    {
        return new YouScopeFrameRMI(new YouScopeFrameImpl().rootFrame);
    }

    // Thread save.
    private void setToErrorState(final String message, final Exception e)
    {
        class ThreadSaveRunner implements Runnable
        {
            private String message;

            private final Exception e;

            ThreadSaveRunner(final String message, final Exception e)
            {
                this.message = message;
                this.e = e;
            }

            @Override
            public void run()
            {
                synchronized (contentPane)
                {
                    contentPane.removeAll();
                    contentPane.add(new JLabel("Window could not initialize due to the following error:"), BorderLayout.NORTH);
                    final JButton closeButton = new JButton("Close");
                    closeButton.addActionListener(new ActionListener()
                    {
                        // Thread save, since only called by UI.
                        @Override
                        public void actionPerformed(final ActionEvent arg0)
                        {
                            try
                            {
                                YouScopeFrameImpl.this.setClosed(true);
                            }
                            catch (final PropertyVetoException e1)
                            {
                                // Should not happen!
                                ClientSystem.err.println("Could not close window", e1);
                            }
                        }
                    });
                    contentPane.add(closeButton, BorderLayout.SOUTH);
                    if (e != null)
                    {
                        message += "\n\n" + e.getMessage();
                    }
                    JTextArea textArea = new JTextArea(message);
                    textArea.setEditable(false);
                    contentPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
                }
                pack();
            }
        }
        if (SwingUtilities.isEventDispatchThread())
        {
            (new ThreadSaveRunner(message, e)).run();
        } else
        {
            SwingUtilities.invokeLater(new ThreadSaveRunner(message, e));
        }
    }

    // Thread save.
    private void startInitializing()
    {
        final Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                setSize(300, 200);
                setGlassPane(new GlassPane("<html><center><b>Initializing frame...</b><br />This may take several seconds.</center></html>", true));
                getGlassPane().setVisible(true);
            }
        };

        if (SwingUtilities.isEventDispatchThread())
        {
            runner.run();
        } else
        {
            SwingUtilities.invokeLater(runner);
        }
    }

    // Thread save
    private void startLoading()
    {
        // Make call thread save.
        final Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                setGlassPane(new GlassPane("<html><center><b>Loading content...</b><br />This may take several seconds.</center></html>", true));
                getGlassPane().setVisible(true);
            }
        };
        if (SwingUtilities.isEventDispatchThread())
        {
            runner.run();
        } else
        {
            SwingUtilities.invokeLater(runner);
        }
    }

    // Thread save.
    private void endLoading()
    {
        final Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                if (getGlassPane() != null)
                {
                    getGlassPane().setVisible(false);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread())
        {
            runner.run();
        } else
        {
            SwingUtilities.invokeLater(runner);
        }
    }

    /**
     * The root frame interface implementation. Do not return to the outside world, but instead return
     * new YouScopeFrameRMI(rootFrame).
     */
    private final YouScopeFrame rootFrame = new YouScopeFrame()
    {
        // Thread save.
        @Override
        public void setVisible(final boolean visible)
        {
            class ThreadSaveRunner implements Runnable
            {
                private final boolean visible;

                ThreadSaveRunner(final boolean visible)
                {
                    this.visible = visible;
                }

                @Override
                public void run()
                {
                    synchronized (YouScopeFrameImpl.this)
                    {
                        if (visible)
                        {
                            if (!isVisible())
                            {

                                // Add frame to desktop.
                                YouScopeClientImpl.getMainProgram().showFrame(YouScopeFrameImpl.this);
                                YouScopeFrameImpl.this.setVisible(true);
                            }
                            YouScopeFrameImpl.this.toFront();
                        }
                        else
                        {
                            try
                            {
                                YouScopeFrameImpl.this.setClosed(true);
                                YouScopeFrameImpl.this.setVisible(false);
                            } catch (final PropertyVetoException e)
                            {
                                ClientSystem.err.println("Could not close frame.", e);
                            }
                        }
                    }
                }
            }
            if (SwingUtilities.isEventDispatchThread())
            {
                (new ThreadSaveRunner(visible)).run();
            } else
            {
                SwingUtilities.invokeLater(new ThreadSaveRunner(visible));
            }
        }

        // Thread save
        @Override
        public boolean isVisible()
        {
            class ThreadSaveRunner implements Runnable
            {
                private volatile boolean visible = false;

                @Override
                public void run()
                {
                    visible = YouScopeFrameImpl.this.isVisible();
                }

                public boolean getResult()
                {
                    return visible;
                }
            }
            final ThreadSaveRunner runner = new ThreadSaveRunner();
            if (SwingUtilities.isEventDispatchThread())
            {
                runner.run();
            } else
            {
                try
                {
                    SwingUtilities.invokeAndWait(runner);
                } catch (final Exception e)
                {
                    ClientSystem.err.println("Could not detect if frame is visible.", e);
                }
            }
            return runner.getResult();
        }

        // Thread save.
        @Override
        public YouScopeFrame createChildFrame()
        {
            YouScopeFrameRMI frameRMI = new YouScopeFrameRMI(new YouScopeFrameImpl().rootFrame);
            addChildFrameInternal(frameRMI, false);
            return frameRMI;
        }

        // Thread save
        @Override
        public YouScopeFrame createModalChildFrame()
        {
            YouScopeFrameRMI frameRMI = new YouScopeFrameRMI(new YouScopeFrameImpl().rootFrame);
            addChildFrameInternal(frameRMI, true);
            return frameRMI;
        }

        // Thread save.
        @Override
        public YouScopeFrame createFrame()
        {
            return createTopLevelFrame();
        }

        // Thread save
        @Override
        public void setSize(final Dimension size)
        {
            class ThreadSaveRunner implements Runnable
            {
                private final Dimension size;

                ThreadSaveRunner(final Dimension size)
                {
                    this.size = size;
                }

                @Override
                public void run()
                {
                    YouScopeFrameImpl.this.setSize(size);
                }
            }
            if (SwingUtilities.isEventDispatchThread())
            {
                (new ThreadSaveRunner(size)).run();
            } else
            {
                SwingUtilities.invokeLater(new ThreadSaveRunner(size));
            }
        }

        // Thread save
        @Override
        public Dimension getSize()
        {
            class ThreadSaveRunner implements Runnable
            {
                private volatile Dimension size = null;

                @Override
                public void run()
                {
                    size = YouScopeFrameImpl.this.getSize();
                }

                public Dimension getResult()
                {
                    return size;
                }
            }
            final ThreadSaveRunner runner = new ThreadSaveRunner();
            if (SwingUtilities.isEventDispatchThread())
            {
                runner.run();
            } else
            {
                try
                {
                    SwingUtilities.invokeAndWait(runner);
                } catch (final Exception e)
                {
                    ClientSystem.err.println("Could not get frame size.", e);
                }
            }
            return runner.getResult();
        }

        // Thread save
        @Override
        public void pack()
        {
            final Runnable runner = new Runnable()
            {
                @Override
                public void run()
                {
                    YouScopeFrameImpl.this.pack();
                }
            };

            if (SwingUtilities.isEventDispatchThread())
            {
                runner.run();
            } else
            {
                SwingUtilities.invokeLater(runner);
            }
        }

        // Thread save
        @Override
        public void setResizable(final boolean resizable)
        {
            class ThreadSaveRunner implements Runnable
            {
                private final boolean resizable;

                ThreadSaveRunner(final boolean resizable)
                {
                    this.resizable = resizable;
                }

                @Override
                public void run()
                {
                    YouScopeFrameImpl.this.setResizable(resizable);
                }
            }
            if (SwingUtilities.isEventDispatchThread())
            {
                (new ThreadSaveRunner(resizable)).run();
            } else
            {
                SwingUtilities.invokeLater(new ThreadSaveRunner(resizable));
            }
        }

        // Thread save
        @Override
        public void setClosable(final boolean closable)
        {
            class ThreadSaveRunner implements Runnable
            {
                private final boolean closable;

                ThreadSaveRunner(final boolean closable)
                {
                    this.closable = closable;
                }

                @Override
                public void run()
                {
                    YouScopeFrameImpl.this.setClosable(closable);
                }
            }
            if (SwingUtilities.isEventDispatchThread())
            {
                (new ThreadSaveRunner(closable)).run();
            } else
            {
                SwingUtilities.invokeLater(new ThreadSaveRunner(closable));
            }
        }

        // Thread save
        @Override
        public void setMaximizable(final boolean maximizable)
        {
            class ThreadSaveRunner implements Runnable
            {
                private final boolean maximizable;

                ThreadSaveRunner(final boolean maximizable)
                {
                    this.maximizable = maximizable;
                }

                @Override
                public void run()
                {
                    YouScopeFrameImpl.this.setMaximizable(maximizable);
                }
            }
            if (SwingUtilities.isEventDispatchThread())
            {
                (new ThreadSaveRunner(maximizable)).run();
            } else
            {
                SwingUtilities.invokeLater(new ThreadSaveRunner(maximizable));
            }
        }

        // Thread save
        @Override
        public boolean isResizable()
        {
            class ThreadSaveRunner implements Runnable
            {
                private volatile boolean resizable = false;

                @Override
                public void run()
                {
                    resizable = YouScopeFrameImpl.this.isResizable();
                }

                public boolean getResult()
                {
                    return resizable;
                }
            }
            final ThreadSaveRunner runner = new ThreadSaveRunner();
            if (SwingUtilities.isEventDispatchThread())
            {
                runner.run();
            } else
            {
                try
                {
                    SwingUtilities.invokeAndWait(runner);
                } catch (final Exception e)
                {
                    ClientSystem.err.println("Could not detect if frame is resizable.", e);
                }
            }
            return runner.getResult();
        }

        // Thread save
        @Override
        public boolean isClosable()
        {
            class ThreadSaveRunner implements Runnable
            {
                private volatile boolean closeable = false;

                @Override
                public void run()
                {
                    closeable = YouScopeFrameImpl.this.isClosable();
                }

                public boolean getResult()
                {
                    return closeable;
                }
            }
            final ThreadSaveRunner runner = new ThreadSaveRunner();
            if (SwingUtilities.isEventDispatchThread())
            {
                runner.run();
            } else
            {
                try
                {
                    SwingUtilities.invokeAndWait(runner);
                } catch (final Exception e)
                {
                    ClientSystem.err.println("Could not detect if frame is closeable.", e);
                }
            }
            return runner.getResult();
        }

        // Thread save
        @Override
        public boolean isMaximizable()
        {
            class ThreadSaveRunner implements Runnable
            {
                private volatile boolean maximizable = false;

                @Override
                public void run()
                {
                    maximizable = YouScopeFrameImpl.this.isMaximizable();
                }

                public boolean getResult()
                {
                    return maximizable;
                }
            }
            final ThreadSaveRunner runner = new ThreadSaveRunner();
            if (SwingUtilities.isEventDispatchThread())
            {
                runner.run();
            } else
            {
                try
                {
                    SwingUtilities.invokeAndWait(runner);
                } catch (final Exception e)
                {
                    ClientSystem.err.println("Could not detect if frame is maximizable.", e);
                }
            }
            return runner.getResult();
        }

        // Thread save.
        @Override
        public void addFrameListener(final YouScopeFrameListener listener)
        {
            synchronized (YouScopeFrameImpl.this.frameListeners)
            {
                YouScopeFrameImpl.this.frameListeners.add(listener);
            }
        }

        // Thread save
        @Override
        public void removeFrameListener(final YouScopeFrameListener listener)
        {
            synchronized (YouScopeFrameImpl.this.frameListeners)
            {
                YouScopeFrameImpl.this.frameListeners.remove(listener);
            }
        }

        // Thread save
        @Override
        public void setTitle(final String title)
        {
            class ThreadSaveRunner implements Runnable
            {
                private final String title;

                ThreadSaveRunner(final String title)
                {
                    this.title = title;
                }

                @Override
                public void run()
                {
                    YouScopeFrameImpl.this.setTitle(title);
                }
            }
            if (SwingUtilities.isEventDispatchThread())
            {
                (new ThreadSaveRunner(title)).run();
            } else
            {
                SwingUtilities.invokeLater(new ThreadSaveRunner(title));
            }
        }

        // Thread save
        @Override
        public String getTitle()
        {
            class ThreadSaveRunner implements Runnable
            {
                private volatile String title = null;

                @Override
                public void run()
                {
                    title = YouScopeFrameImpl.this.getTitle();
                }

                public String getResult()
                {
                    return title;
                }
            }
            final ThreadSaveRunner runner = new ThreadSaveRunner();
            if (SwingUtilities.isEventDispatchThread())
            {
                runner.run();
            } else
            {
                try
                {
                    SwingUtilities.invokeAndWait(runner);
                } catch (final Exception e)
                {
                    ClientSystem.err.println("Could not get frame title.", e);
                }
            }
            return runner.getResult();
        }

        // Thread save, since only calling thread save functions.
        @Override
        public void setToErrorState(final String message, final Exception e)
        {
            YouScopeFrameImpl.this.setToErrorState(message, e);
        }

        // Thread save, since only calling thread save functions.
        @Override
        public void startInitializing()
        {
            YouScopeFrameImpl.this.startInitializing();
        }

        // Thread save, since only calling thread save functions.
        @Override
        public void startLoading()
        {
            YouScopeFrameImpl.this.startLoading();
        }

        // Thread save, since only calling thread save functions.
        @Override
        public void endLoading()
        {
            YouScopeFrameImpl.this.endLoading();
        }

        // Thread save
        @Override
        public void setMaximum(final boolean maximized)
        {
            class ThreadSaveRunner implements Runnable
            {
                private final boolean maximized;

                ThreadSaveRunner(final boolean maximized)
                {
                    this.maximized = maximized;
                }

                @Override
                public void run()
                {
                    try
                    {
                        if (isVisible())
                        {
                            YouScopeFrameImpl.this.setMaximum(maximized);
                        }
                    } catch (@SuppressWarnings("unused") final PropertyVetoException e)
                    {
                        // Do nothing.
                    }
                }
            }

            if (SwingUtilities.isEventDispatchThread())
            {
                (new ThreadSaveRunner(maximized)).run();
            } else
            {
                SwingUtilities.invokeLater(new ThreadSaveRunner(maximized));
            }
        }

        // Thread save.
        @Override
        public void setContentPane(final Component contentPane)
        {
            class ThreadSaveRunner implements Runnable
            {
                private final Component contentPane;

                ThreadSaveRunner(final Component contentPane)
                {
                    this.contentPane = contentPane;
                }

                @Override
                public void run()
                {
                    synchronized (contentPane)
                    {
                        YouScopeFrameImpl.this.contentPane.removeAll();
                        YouScopeFrameImpl.this.contentPane.add(contentPane);
                        YouScopeFrameImpl.this.revalidate();
                    }
                }
            }
            if (SwingUtilities.isEventDispatchThread())
            {
                (new ThreadSaveRunner(contentPane)).run();
            } else
            {
                SwingUtilities.invokeLater(new ThreadSaveRunner(contentPane));
            }
        }

        @Override
        public void relocateFrameTo(YouScopeFrame targetFrame)
        {
            // do nothing. The root frame cannot be relocated.
        }

		@Override
		public void setMargins(final int left, final int top, final int right, final int bottom)
		{
			final Runnable runner = new Runnable() 
			{
				@Override
				public void run() 
				{
					if(left==0 && top == 0 && right == 0 && bottom == 0)
						contentPane.setBorder(null);
					else
						contentPane.setBorder(new EmptyBorder(top, left, bottom, right));
				}
			};
			if (SwingUtilities.isEventDispatchThread())
            {
                runner.run();
            } else
            {
                SwingUtilities.invokeLater(runner);
            }
		}

		@Override
		public void addChildFrame(YouScopeFrame childFrame) 
		{
			addChildFrameInternal(childFrame, false);
		}

		@Override
		public void addModalChildFrame(YouScopeFrame childFrame) {
			addChildFrameInternal(childFrame, true);
		}

		@Override
		public void toFront() 
		{
			moveToFront();
			try {
				setSelected(true);
			} 
			catch (@SuppressWarnings("unused") PropertyVetoException e) 
			{
				// do nothing, not severe.
			}
			requestFocusInWindow();
		}
    };
    
    /**
     * Pseudo frame basically only forwarding to parent frame.
     * We use this concept to be able to relocate this object to another frame.
     * @author Moritz Lang
     *
     */
    private static class YouScopeFrameRMI implements YouScopeFrame
    {
        private YouScopeFrame parentFrame;
        YouScopeFrameRMI(YouScopeFrame parentFrame)
        {
            this.parentFrame = parentFrame;
        }
        
        @Override
        public synchronized void setVisible(final boolean visible)
        {
            parentFrame.setVisible(visible);
        }

        @Override
        public synchronized boolean isVisible()
        {
            return parentFrame.isVisible();
        }

        
        @Override
        public synchronized YouScopeFrame createChildFrame()
        {
            return parentFrame.createChildFrame();
        }

        @Override
        public synchronized YouScopeFrame createModalChildFrame()
        {
            return parentFrame.createModalChildFrame();
        }

        @Override
        public synchronized YouScopeFrame createFrame()
        {
            return parentFrame.createFrame();
        }

        
        @Override
        public synchronized void setSize(final Dimension size)
        {
            parentFrame.setSize(size);
        }

        
        @Override
        public synchronized Dimension getSize()
        {
            return parentFrame.getSize();
        }

        
        @Override
        public synchronized void pack()
        {
            parentFrame.pack();
        }

        
        @Override
        public synchronized void setResizable(final boolean resizable)
        {
            parentFrame.setResizable(resizable);
        }

        
        @Override
        public synchronized void setClosable(final boolean closable)
        {
            parentFrame.setClosable(closable);
        }

        
        @Override
        public synchronized void setMaximizable(final boolean maximizable)
        {
            parentFrame.setMaximizable(maximizable);
        }

        
        @Override
        public synchronized boolean isResizable()
        {
            return parentFrame.isResizable();
        }

        
        @Override
        public synchronized boolean isClosable()
        {
            return parentFrame.isClosable();
        }

        
        @Override
        public synchronized boolean isMaximizable()
        {
            return parentFrame.isMaximizable();
        }

        @Override
        public synchronized void addFrameListener(final YouScopeFrameListener listener)
        {
            parentFrame.addFrameListener(listener);
        }

        
        @Override
        public synchronized void removeFrameListener(final YouScopeFrameListener listener)
        {
            parentFrame.removeFrameListener(listener);
        }

        
        @Override
        public synchronized void setTitle(final String title)
        {
            parentFrame.setTitle(title);
        }

        
        @Override
        public synchronized String getTitle()
        {
            return parentFrame.getTitle();
        }

        @Override
        public synchronized void setToErrorState(final String message, final Exception e)
        {
            parentFrame.setToErrorState(message, e);
        }

        @Override
        public synchronized void startInitializing()
        {
            parentFrame.startInitializing();
        }

        @Override
        public synchronized void startLoading()
        {
            parentFrame.startLoading();
        }

        @Override
        public synchronized void endLoading()
        {
            parentFrame.endLoading();
        }

        @Override
        public synchronized void setMaximum(final boolean maximized)
        {
            parentFrame.setMaximum(maximized);
        }

        @Override
        public synchronized void setContentPane(final Component contentPane)
        {
            parentFrame.setContentPane(contentPane);
        }

        @Override
        public void relocateFrameTo(YouScopeFrame targetFrame)
        {
            parentFrame = targetFrame;
        }

		@Override
		public void setMargins(int left, int top, int right, int bottom) 
		{
			parentFrame.setMargins(left, top, right, bottom);
		}

		@Override
		public void addChildFrame(YouScopeFrame childFrame) {
			parentFrame.addChildFrame(childFrame);
		}

		@Override
		public void addModalChildFrame(YouScopeFrame childFrame) {
			parentFrame.addModalChildFrame(childFrame);
			
		}

		@Override
		public void toFront() 
		{
			parentFrame.toFront();
		}
    }

    
    private void closeAllChildFrames()
    {
    	synchronized(childFrames)
    	{
	    	for(Iterator<ChildFrame> iterator = childFrames.iterator();iterator.hasNext();)
	    	{
	    		ChildFrame childFrame = iterator.next();
	    		iterator.remove();
	    		childFrame.frame.removeFrameListener(childFrame);
	    		childFrame.frame.setVisible(false);
	    	}
    	}
    }
    private void addChildFrameInternal(YouScopeFrame childFrame, boolean modal)
    {
    	ChildFrame newChild = new ChildFrame(childFrame, modal);
    	childFrame.addFrameListener(newChild);
    	if(childFrame.isVisible())
    	{
	    	synchronized(childFrames)
	    	{
	    		childFrames.add(newChild);
	    	}
	    	if(modal)
	    		deactivateIfChildModal();
    	}
    }
    
    /**
     * Listener which gets added to every child frame to get notified when it is shown and hidden.
     *
     * @author Moritz Lang
     */
    private class ChildFrame implements YouScopeFrameListener
    {
        private final YouScopeFrame frame;
        private final boolean modal;
        ChildFrame(final YouScopeFrame frame, boolean modal)
        {
            this.frame = frame;
            this.modal = modal;
            frame.addFrameListener(this);
        }

        // Thread save
        @Override
        public void frameClosed()
        {
            synchronized (childFrames)
            {
                childFrames.remove(this);
            }
            if(modal)
            	deactivateIfChildModal();
        }

        // Thread save
        @Override
        public void frameOpened()
        {
            synchronized (childFrames)
            {
                childFrames.add(this);
            }
            if(modal)
            	deactivateIfChildModal();
        }

    }

    // Thread save
    private ChildFrame getFirstModalChild()
    {
        synchronized (childFrames)
        {
            for (final ChildFrame childFrame : childFrames)
            {
                if (childFrame.modal)
                {
                    return childFrame;
                }
            }
        }
        return null;
    }

    // Thread save.
    private void deactivateIfChildModal()
    {
        final Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                if (getFirstModalChild() != null)
                {
                        setGlassPane(new GlassPane(
                            "<html><center><span style=\"color:#000000\"><b>Modal child frame is active.</b><br />Close frame ("
                                    + getFirstModalChild().frame.getTitle() + ") to be able to access this frame again.</span></center></html>", false));
                    getGlassPane().setVisible(true);
                }
                else
                {
                    getGlassPane().setVisible(false);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread())
        {
            runner.run();
        } else
        {
            SwingUtilities.invokeLater(runner);
        }
    }

    /**
     * Listener class for frame events like closing/showing...
     *
     * @author Moritz Lang
     */
    private class FrameListener implements InternalFrameListener, VetoableChangeListener, ComponentListener
    {
        // Thread save, since only called by UI thread.
        @Override
        public void internalFrameActivated(final InternalFrameEvent e)
        {
            final ChildFrame childFrame = getFirstModalChild();
            if (childFrame != null)
            {
            	try {
					setSelected(false);
				} 
            	catch (@SuppressWarnings("unused") PropertyVetoException e1) 
            	{
					// do nothing, not severe.
				}
                childFrame.frame.toFront();
            }

        }

        // Thread save, since only called by UI thread.
        @Override
        public void internalFrameClosed(final InternalFrameEvent e)
        {
            for (final YouScopeFrameListener listener : frameListeners)
            {
                listener.frameClosed();
            }
        }

        // Thread save, since only called by UI thread.
        @Override
        public void internalFrameClosing(final InternalFrameEvent e)
        {
            FramePositionStorage.getInstance().storeFramePosition(YouScopeFrameImpl.this);
        }

        // Thread save, since only called by UI thread.
        @Override
        public void internalFrameDeactivated(final InternalFrameEvent e)
        {
        	// do nothing.
        }

        // Thread save, since only called by UI thread.
        @Override
        public void internalFrameDeiconified(final InternalFrameEvent e)
        {
        	// do nothing.
        }

        // Thread save, since only called by UI thread.
        @Override
        public void internalFrameIconified(final InternalFrameEvent e)
        {
        	// do nothing.
        }

        // Thread save
        @Override
        public void internalFrameOpened(final InternalFrameEvent e)
        {
            synchronized (frameListeners)
            {
                for (final YouScopeFrameListener listener : frameListeners)
                {
                    listener.frameOpened();
                }
            }
        }

        // Thread save
        @Override
        public void vetoableChange(final PropertyChangeEvent changeEvent) throws PropertyVetoException
        {
            if (changeEvent.getPropertyName() != null && changeEvent.getPropertyName().compareToIgnoreCase(JInternalFrame.IS_CLOSED_PROPERTY) == 0)
            {
                // Don't close if at least one child is modal.
                if (getFirstModalChild() != null)
                {
                    throw new PropertyVetoException("Modal Child open.", changeEvent);
                }

                // Close all child frames.
                closeAllChildFrames();
            }
        }

        

        @Override
        public void componentResized(final ComponentEvent e)
        {
            // do nothing.
        }

        @Override
        public void componentMoved(final ComponentEvent e)
        {
            // do nothing.
        }

        @Override
        public void componentShown(final ComponentEvent e)
        {
            // do nothing.
        }

        @Override
        public void componentHidden(final ComponentEvent e)
        {
        	// do nothing.
        }
    }
}
