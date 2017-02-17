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
package org.youscope.starter;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

/**
 * @author langmo
 */
class SplashScreen extends JWindow
{

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 4466369250733192230L;

    protected BufferedImage splashImage = null;

    protected JProgressBar progressBar = new JProgressBar(0, 100);

    protected static final String splashImageFile = "org/youscope/starter/images/splash.jpg";

    SplashScreen(String initialString)
    {

        try
        {
            URL topImageURL = getClass().getClassLoader().getResource(splashImageFile);
            if (topImageURL != null)
            {
                splashImage = ImageIO.read(topImageURL);
            }
        } catch (@SuppressWarnings("unused") Exception e)
        {
            // Do nothing.
        }
        setContentPane(new ContentPane(initialString));
        setSize(600, 361);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
        // setAlwaysOnTop(true);
    }

    void setProgress(int progress, String description)
    {
        progressBar.setValue(progress);
        progressBar.setString(description);
    }

    protected class ContentPane extends JPanel
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = 4932589253009033239L;

        ContentPane(String initialString)
        {
            super(null);
            setOpaque(false);
            progressBar.setLocation(100, 338);
            progressBar.setSize(400, 20);
            progressBar.setString(initialString);
            progressBar.setStringPainted(true);
            add(progressBar);
        }

        @Override
        public void paint(Graphics g)
        {
            if (splashImage != null)
                g.drawImage(splashImage, 0, 0, null);
            super.paint(g);
        }
    }
}
