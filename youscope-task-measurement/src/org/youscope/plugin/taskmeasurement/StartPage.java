package org.youscope.plugin.taskmeasurement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.ImageLoadingTools;

/**
 * @author Moritz Lang
 *
 */
public class StartPage extends MeasurementAddonUIPage<TaskMeasurementConfiguration>
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2394617369656492466L;
	@Override
	public void loadData(TaskMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public boolean saveData(TaskMeasurementConfiguration configuration)
	{
		// Do nothing.
		return true;
	}

	@Override
	public void setToDefault(TaskMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public String getPageName()
	{
		return "Description";
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		setLayout(new BorderLayout(5, 5));
		setOpaque(false);
		
		JEditorPane descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>Description:</b></p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">A task measurement helps to<br />perform several different<br />tasks a microscope should do<br />in parallel (see Figure 1).</p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">Every task is repeated regularly with<br />a given period and start time,<br />and is composed of several<br />subelements, called jobs.</p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">One job thereby corresponds to a single<br />action of the microscope, like taking<br />a bright-field image or changing the<br /> stage position.</p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">Every task either has a fixed period length,<br />meaning that its jobs are executed<br />e.g. every two minutes,<br />or a variable period length,<br />meaning that its jobs are executed<br />a given time span after they finished</p>"+
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">The latter one is useful if a task of lower<br />priority should be executed with a<br />high frequency, but without<br />blocking the exectution of tasks<br /> of higher priority.</p></html>");
		
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
		descriptionScrollPane.setPreferredSize(new Dimension(250, 200));
		add(descriptionScrollPane, BorderLayout.CENTER);
		
		// Descriptive image
		ImageIcon measurementIcon = ImageLoadingTools.getResourceIcon("org/youscope/plugin/taskmeasurement/images/taskMeasurement.jpg", "Task Measurement");if(measurementIcon != null)
		if(measurementIcon != null)
		{
			JPanel imagePanel = new JPanel(new BorderLayout());
			imagePanel.setOpaque(false);
			JLabel imageLabel = new JLabel(measurementIcon, SwingConstants.LEFT);
			imageLabel.setBackground(Color.WHITE);
			imageLabel.setOpaque(true);
			imageLabel.setBorder(new LineBorder(Color.BLACK, 1));
			JLabel legendLabel = new JLabel("<html><b>Figure 1:</b> Flowchart of a task measurement.</html>", SwingConstants.LEFT);
			imagePanel.add(imageLabel, BorderLayout.CENTER);
			imagePanel.add(legendLabel, BorderLayout.SOUTH);
			add(imagePanel, BorderLayout.WEST);
		}
	}
}
