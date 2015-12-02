package ch.ethz.csb.youscope.client.uielements;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

/**
 * Glasspane to overlay a frame. Showing a text and possibly a waitbar.
 * 
 * @author Moritz Lang
 */
public class GlassPane extends JPanel
{
    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 8623023010617741626L;

    private final JLabel messageLabel;

    private JProgressBar waitbar = null;

    private final int messageWidth;

    private final int messageHeight;

    private boolean showWaitbar;
    
    private boolean waitbarIndeterminate = true;

    /**
     * Creates a glas pane to overlay a frame, e.g. when it is loading.
     * @param text Text to display on glass plane.
     * @param showWaitbar True if, additionally, a wait bar should be shown.
     */
    public GlassPane(String text, boolean showWaitbar)
    {
        super(null);
        if (showWaitbar)
            setOpaque(true);
        else
            setOpaque(false);
        this.showWaitbar = showWaitbar;
        messageLabel = new JLabel(text, SwingConstants.CENTER);
        messageWidth = messageLabel.getPreferredSize().width;
        messageHeight = messageLabel.getPreferredSize().height;
        messageLabel.setSize(messageWidth, messageHeight);
        add(messageLabel);
        if (showWaitbar)
        {
            ensureWaitbar();
        }
    }

    private void ensureWaitbar()
    {
        if (waitbar == null)
        {
            showWaitbar = true;
            waitbar = new JProgressBar();
            waitbar.setSize(messageWidth, 20);
            add(waitbar);
        }
    }

    /**
     * Sets the progress when the waitbat should be full.
     * @param max Progress corresponding to end waiting.
     */
    public void setWaitBarMax(int max)
    {
        ensureWaitbar();
        waitbar.setIndeterminate(waitbarIndeterminate = false);
        waitbar.setMaximum(max);
    }

    /**
     * Sets the current progress.
     * @param current current progress.
     */
    public void setWaitBarProgress(int current)
    {
        waitbar.setValue(current);
    }

    @Override
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            super.setVisible(true);
            if (showWaitbar)
                waitbar.setIndeterminate(waitbarIndeterminate);
        }
        else
        {
            if (showWaitbar)
                waitbar.setIndeterminate(false);
            super.setVisible(false);
        }
    }

    @Override
    public void paintComponent(Graphics grp)
    {
        grp.setColor(new Color(1.0F, 1.0F, 1.0F, 0.7F));
        grp.fillRect(0, 0, getWidth(), getHeight());

        int width = getWidth();
        int height = getHeight();

        if (showWaitbar)
        {
            messageLabel.setLocation((width - messageWidth) / 2, height / 2 - messageHeight - 2);
            waitbar.setLocation((width - messageWidth) / 2, height / 2 + 2);
        }
        else
        {
            messageLabel.setLocation((width - messageWidth) / 2, (height - messageHeight) / 2);
        }

        super.paintComponent(grp);
    }
}