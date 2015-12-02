package ch.ethz.csb.youscope.compatibility.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.YouScopeFrameListener;

/**
 * A JPanel pretending to be a frame. Can be used to redirect tools or configuration UIs to put their content into the JPanel instead of an own frame.
 * @author Moritz Lang
 *
 */
public class InlineFrame extends JPanel
{
    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 1253960157937742283L;

    private String title = "";
    
    private final YouScopeFrame containingFrame;

    /**
     * Constructor.
     * @param containingFrame The frame containing this component.
     */
    public InlineFrame(final YouScopeFrame containingFrame)
    {
        this.containingFrame = containingFrame;
        setLayout(new BorderLayout(0, 0));
    }

    /**
     * The root frame interface implementation. Do not return to the outside world, but instead return
     * new YouScopeFrameRMI(rootFrame).
     */
    YouScopeFrame rootFrame = new YouScopeFrame()
    {
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
                    synchronized (InlineFrame.this)
                    {
                        InlineFrame.this.removeAll();
                        if (contentPane != null)
                        {
                            InlineFrame.this.add(contentPane, BorderLayout.CENTER);

                            contentPane.validate();
                            contentPane.setVisible(true);
                        }

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
        public void setVisible(final boolean visible)
        {
            InlineFrame.this.setVisible(visible);
        }

        @Override
        public boolean isVisible()
        {
            return InlineFrame.this.isVisible();
        }

        @Override
        public void setMaximum(final boolean maximum)
        {
        	// do nothing.
        }

        @Override
        public YouScopeFrame createChildFrame()
        {
            return InlineFrame.this.containingFrame.createChildFrame();
        }

        @Override
        public YouScopeFrame createModalChildFrame()
        {
            return InlineFrame.this.containingFrame.createModalChildFrame();
        }

        @Override
        public YouScopeFrame createFrame()
        {
            return InlineFrame.this.containingFrame.createFrame();
        }

        @Override
        public void setSize(final Dimension size)
        {
            InlineFrame.this.setSize(size);
        }

        @Override
        public Dimension getSize()
        {
            return InlineFrame.this.getSize();
        }

        @Override
        public void pack()
        {
            // ignore. Must be handled by whoever uses the inline frame.
        }

        @Override
        public void setResizable(final boolean resizable)
        {
            // ignore. Must be handled by whoever uses the inline frame.

        }

        @Override
        public void setClosable(final boolean closable)
        {
         // ignore. Must be handled by whoever uses the inline frame.

        }

        @Override
        public void setMaximizable(final boolean maximizable)
        {
            // ignore. Must be handled by whoever uses the inline frame.

        }

        @Override
        public boolean isResizable()
        {
            // ignore. Must be handled by whoever uses the inline frame.
            return true;
        }

        @Override
        public boolean isClosable()
        {
            // ignore. Must be handled by whoever uses the inline frame.
            return false;
        }

        @Override
        public boolean isMaximizable()
        {
            // ignore. Must be handled by whoever uses the inline frame.
            return false;
        }

        @Override
        public void addFrameListener(final YouScopeFrameListener listener)
        {
            containingFrame.addFrameListener(listener);
        }

        @Override
        public void removeFrameListener(final YouScopeFrameListener listener)
        {
            containingFrame.addFrameListener(listener);
        }

        @Override
        public void setTitle(final String title)
        {
            InlineFrame.this.title = title;
        }

        @Override
        public String getTitle()
        {
            return InlineFrame.this.title;
        }

        @Override
        public void setToErrorState(final String message, final Exception e)
        {
            class ThreadSaveRunner implements Runnable
            {
                @Override
                public void run()
                {
                    JPanel contentPane = InlineFrame.this;
                    synchronized (contentPane)
                    {
                        contentPane.removeAll();
                        contentPane.add(new JLabel("Window could not initialize due to the following error:"), BorderLayout.NORTH);
                        String fullMessage = message;
                        if (e != null)
                        {
                            fullMessage += "\n\n" + e.getMessage();
                        }
                        JTextArea textArea = new JTextArea(fullMessage);
                        textArea.setEditable(false);
                        contentPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
                    }
                    pack();
                }
            }
            if (SwingUtilities.isEventDispatchThread())
            {
                (new ThreadSaveRunner()).run();
            } else
            {
                SwingUtilities.invokeLater(new ThreadSaveRunner());
            }
        }

        @Override
        public void startInitializing()
        {
            // TODO Implement something to display state.

        }

        @Override
        public void startLoading()
        {
         // TODO Implement something to display state.

        }

        @Override
        public void endLoading()
        {
         // TODO Implement something to display state.

        }

        @Override
        public void relocateFrameTo(YouScopeFrame targetFrame)
        {
            // do nothing. We are the root, thus, this function should not be called
            // (we should only exist inside of an YouScopeFrameRMI)
        }

		@Override
		public void setMargins(final int left, final int top, final int right , final int bottom) 
		{
			final Runnable runner = new Runnable() {
				
				@Override
				public void run() 
				{
					if(left==0 && top == 0 && right == 0 && bottom == 0)
						InlineFrame.this.setBorder(null);
					else
						InlineFrame.this.setBorder(new EmptyBorder(top, left, bottom, right));
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
		public void addChildFrame(YouScopeFrame childFrame) {
			containingFrame.addChildFrame(childFrame);
		}

		@Override
		public void addModalChildFrame(YouScopeFrame childFrame) {
			containingFrame.addModalChildFrame(childFrame);
		}

		@Override
		public void toFront() {
			containingFrame.toFront();
			InlineFrame.this.requestFocusInWindow();
			
		}

    };
    
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
		public void setMargins(int left, int top, int right, int bottom) {
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
		public void toFront() {
			parentFrame.toFront();
			
		}
    }

    /**
     * Returns the YouScopeFrame interface corresponding to this inline frame.
     * @return youscope frame interface.
     */
    public YouScopeFrame getYouScopeFrame()
    {
        return new YouScopeFrameRMI(rootFrame);
    }
}
