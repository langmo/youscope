/**
 * 
 */
package org.youscope.uielements;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author langmo
 */
public class FileNameComboBox extends JComboBox<Object>
{
	/**
	 * The type of this combo box.
	 * 
	 * @author langmo
	 */
	public enum Type
	{
		/**
		 * Combo Box represents a file name.
		 */
		FILE_NAME,
		/**
		 * Combo Box represents a folder name.
		 */
		FOLDER_NAME
	}

	/**
	 * Serial Version UID.
	 */
	private static final long		serialVersionUID			= -365527994404402593L;

	/**
	 * List of usually used file name types. Two dimensional list, where the
	 * first element is the macro and the second an example.
	 */
	public static final String[][]	PRE_DEFINED_FILE_NAMES		= {
			{ "%N_position%4w%2p_time%n", "RFP_position010203_time5" },
			{ "%N_%C_position%4w%2p_time%n", "RFP_Camera1_position010203_time5" },
			{
			"%N_channel_%c_well_%W(pos_%4p)_time_%4y.%2m.%2d-%2H.%2M.%2s-%3S_(number_%4n)",
			"RFP_well_B3(pos_0003)_time_2011.01.01-14.30.05-365_number_0005" },
			{ "%N_%c_%W(%2p)_%4y.%2m.%2d-%2H.%2M.%2s-%3S_(%4n)",
			"RFP_B3(0003)_2011.01.01-14.30.05-365_0005" }		};

	/**
	 * List of usually used folder name types. Two dimensional list, where the
	 * first element is the macro and the second an example.
	 */
	public static final String[][]	PRE_DEFINED_FOLDER_NAMES	= {
			{ "%N", "RFP" },
			{ "%N_%W_%4p", "RFP_B3_0003" },
			{ "%N_channel_%c_well_%W_position_%4p",
			"RFP_channel_RFP_well_B3_position_0003" }			};

	private static final String[][]	FILE_NAME_MACROS			= {
			{ "i", "Index of image, starting at 0." },
			{ "n", "Number of the image, starting at 1." },
			{ "N", "Name of the imaging job." },
			{ "c", "Channel, in which the image was made." },
			{ "C", "Camera, with which the image was made." },
			{ "p",
			"Position, in which the image was made. =0 if no positions were defined." },
			{ "w",
			"Well of the image in decimal format, e.g. 0302 for well C2 (C=03, 2=02)." },
			{ "W", "Well of the image in standard format, e.g. C2." },
			{ "d", "Day in month." }, { "m", "Month." }, { "y", "Year." },
			{ "h", "Hour (0-12)." }, { "H", "Hour (0-24)." },
			{ "M", "Minute." }, { "s", "Second." }, { "S", "Millisecond." } };

	private static final String[][]	FOLDER_NAME_MACROS			= {
			{ "c", "Channel, in which the image was made." },
			{ "p",
			"Position, in which the image was made. =0 if no positions were defined." },
			{ "w",
			"Well of the image in decimal format, e.g. 0302 for well C2 (C=03, 2=02)." },
			{ "W", "Well of the image in standard format, e.g. C2." } };

	protected static class ComboBoxItem
	{
		public final String	fileName;

		public final String	example;

		ComboBoxItem(String fileName, String example)
		{
			this.fileName = fileName;
			this.example = example;
		}

		@Override
		public String toString()
		{
			return fileName;
		}
	}

	protected static ComboBoxItem[] getItems(FileNameComboBox.Type type)
	{
		String[][] content = new String[0][0];
		switch (type)
		{
		case FILE_NAME:
			content = PRE_DEFINED_FILE_NAMES;
			break;
		case FOLDER_NAME:
			content = PRE_DEFINED_FOLDER_NAMES;
			break;
		default:
			// do nothing.
			break;
		}

		ComboBoxItem[] items = new ComboBoxItem[content.length];
		for (int i = 0; i < content.length; i++)
		{
			items[i] = new ComboBoxItem(content[i][0], content[i][1]);
		}
		return items;
	}

	protected static String getToolTip(FileNameComboBox.Type type)
	{
		switch (type)
		{
		case FOLDER_NAME:
			String folderToolTip = "<html><body><p>"
					+ "The folder where the images of this job should be stored into.<br />"
					+ "This folder name should be unique, otherwise files from different jobs might be saved in the same folder <br />"
					+ "(however, this might also be intended). <br />"
					+ "Macros of the type <code>%[0-9]?[[a-zA-Z]</code> are replaced automatically. The (optional) number thereby defines the maximal length of the replacing string, <br />"
					+ "and the character defines the replacement, e.g. <code>%y</code> with <code>2010</code> and <code>%2y</code> with <code>10</code>. Following characters are defined: <br />"
					+ "<ul>";
			for (int i = 0; i < FOLDER_NAME_MACROS.length; i++)
			{
				folderToolTip += "<li><code>" + FOLDER_NAME_MACROS[i][0]
						+ "</code>: " + FOLDER_NAME_MACROS[i][1] + "</li>";
			}
			folderToolTip += "</ul></p></body></html>";
			return folderToolTip;
		case FILE_NAME:

			String fileToolTip = "<html><body><p>The name of the file where the image is stored to.<br />"
					+ "Please note that if this name is not unique for every new image, old images will be lost.<br />"
					+ "Macros of the type <code>%[0-9]?[[a-zA-Z]</code> are replaced automatically. The (optional) number thereby defines the maximal length of the replacing string, <br />"
					+ "and the character defines the replacement, e.g. <code>%y</code> with <code>2010</code> and <code>%2y</code> with <code>10</code>. Following characters are defined: <br />"
					+ "<ul>";
			for (int i = 0; i < FILE_NAME_MACROS.length; i++)
			{
				fileToolTip += "<li><code>" + FILE_NAME_MACROS[i][0]
						+ "</code>: " + FILE_NAME_MACROS[i][1] + "</li>";
			}
			fileToolTip += "</ul></p></body></html>";
			return fileToolTip;
		default:
			return "";

		}
	}

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            Type of combo box.
	 */
	public FileNameComboBox(FileNameComboBox.Type type)
	{
		super(getItems(type));
		FileNameRenderer renderer = new FileNameRenderer();
		setRenderer(renderer);
		setEditable(true);
		setToolTipText(getToolTip(type));
	}

	class FileNameRenderer extends JLabel implements ListCellRenderer<Object>
	{

		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -365527994444402593L;

		public FileNameRenderer()
		{
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Object> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			ComboBoxItem item = (ComboBoxItem) value;

			if (isSelected)
			{
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else
			{
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			setText("<html><p style=\"margin-top:6px\"><code>"
					+ item.fileName
					+ "</code></p><p style=\"color:#555555;margin-bottom:6px\">(e.g. <samp>"
					+ item.example + "</samp>)</p><html>");

			return this;
		}
	}

	@Override
	public Dimension getPreferredSize()
	{
		Dimension proposed = super.getPreferredSize();
		proposed.height = new JComboBox<Object>().getPreferredSize().height;
		return proposed;
	}
}
