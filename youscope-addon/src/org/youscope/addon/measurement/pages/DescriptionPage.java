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
package org.youscope.addon.measurement.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.util.TextTools;
import org.youscope.uielements.DynamicPanel;

/**
 * A page describing the measurement type, eventually showing some figure, e.g. a flow diagram or similar.
 * @author Moritz Lang
 *
 */
public class DescriptionPage extends MeasurementAddonUIPage<MeasurementConfiguration>
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -5407788842391715831L;

	private final String text;
	private final Icon descriptiveImage;
	private final String iconLegend;
	private final String header;
	private final String pageName;
	/**
	 * Constructor.
	 * @param pageName Name of the page. If set to null, defaults to "Description".
	 * @param header Title of the measurement description. If set to null, defaults to "Description:".
	 * @param text Measurement description. 
	 * @param descriptiveImage An image to illustrate the measurement. If null, no image is displayed.
	 * @param iconLegend Text displayed directly below the image. If null, no legend will be added. Parameter is ignored if descriptiveImage is null.
	 */
	public DescriptionPage(String pageName, String header, String text, Icon descriptiveImage, String iconLegend)
	{
		this.pageName = pageName == null ? "Description" : pageName;
		this.text = text;
		this.descriptiveImage = descriptiveImage;
		this.iconLegend = iconLegend;
		this.header = header == null ? "Description:" : header;
	}
	/**
	 * Constructor.
	 * @param header Title of the measurement description. If set to null, defaults to "Description:".
	 * @param text Measurement description. 
	 */
	public DescriptionPage(String header, String text)
	{
		this(null, header, text, null, null);
	}
	/**
	 * Constructor.
	 * @param header Title of the measurement description. If set to null, defaults to "Description:".
	 * @param text Measurement description. 
	 * @param descriptiveImage An image to illustrate the measurement. If null, no image is displayed.
	 * @param iconLegend Text displayed directly below the image. If null, no legend will be added. Parameter is ignored if descriptiveImage is null.
	 */
	public DescriptionPage(String header, String text, Icon descriptiveImage, String iconLegend)
	{
		this(null, header, text, descriptiveImage, iconLegend);
	}
	/**
	 * Constructor.
	 * @param pageName Name of the page. If set to null, defaults to "Description".
	 * @param header Title of the measurement description. If set to null, defaults to "Description:".
	 * @param text Measurement description. 
	 */
	public DescriptionPage(String pageName, String header, String text)
	{
		this(pageName, header, text, null, null);
	}
	
	@Override
	public void loadData(MeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public boolean saveData(MeasurementConfiguration configuration)
	{
		// Do nothing.
		return true;
	}

	@Override
	public void setToDefault(MeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public String getPageName()
	{
		return pageName;
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		setLayout(new BorderLayout(5, 5));
		setOpaque(false);
		
		JEditorPane descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		descriptionPane.setText("<html>"+TextTools.toHTML(header, text)+"</html>");
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
		descriptionScrollPane.setPreferredSize(new Dimension(250, 200));
		add(descriptionScrollPane, BorderLayout.CENTER);
		
		// Descriptive image
		if(descriptiveImage != null)
		{
			DynamicPanel imagePanel = new DynamicPanel();
			imagePanel.setOpaque(false);
			JLabel imageLabel = new JLabel(descriptiveImage, SwingConstants.LEFT);
			imageLabel.setOpaque(false);
			imageLabel.setBorder(new LineBorder(Color.BLACK, 1));
			imagePanel.add(imageLabel);
			if(iconLegend != null)
			{
				JLabel legendLabel = new JLabel("<html>"+TextTools.toHTML(iconLegend)+"</html>", SwingConstants.LEFT);
				imagePanel.add(legendLabel);
			}
			imagePanel.addFillEmpty();
			add(imagePanel, BorderLayout.WEST);
		}
	}

}
