/**
 * 
 */
package org.youscope.plugin.microplatejob;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.Microplate;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class MicroplatePage extends JobConfigurationPage<MicroplateJobConfiguration>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long	serialVersionUID	= -779685914331188563L;
	
	private final YouScopeClient	client;
	
	private JLabel									numberLabel					= new JLabel("Number of wells horizontal/vertical:");

	private JPanel									numberPanel					= new JPanel(new GridLayout(1, 2, 5, 5));

	private JLabel									distanceLabel				= new JLabel("Distance wells horizontal/vertical (in μm):");

	private JPanel									distancePanel				= new JPanel(new GridLayout(1, 2, 5, 5));

	private JFormattedTextField					widthField					= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField					heightField					= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField					numXField					= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField					numYField					= new JFormattedTextField(StandardFormats.getIntegerFormat());

private JRadioButton 							microplatePredefined 		= new JRadioButton("Common microplate type.");
	
	private JRadioButton							microplateCustom			= new JRadioButton("Custom microplate type.", false);

	private JRadioButton							arbitraryPositions			= new JRadioButton("Arbitrary Positions.", false);
	
	private JList<Microplate> predefinedMicroplateList = null;
	private JLabel								predefinedMicroplateLabel	= new JLabel("Select microplate type:");
	private JScrollPane predefinedMicroplateScrollPane = null;

	private JLabel								numPositionsSelectionLabel	= new JLabel("Number of positions to measure:");

	private JFormattedTextField					numPositionsSelection		= new JFormattedTextField(StandardFormats.getIntegerFormat());
		
	MicroplatePage(YouScopeClient client)
	{
		this.client = client;
	}

	@Override
	public void loadData(MicroplateJobConfiguration configuration)
	{
		String typeID = configuration.getMicroplatePositions().getMicroplateTypeID();
		boolean foundType = false;
		if(typeID != null)
		{
			ListModel<Microplate> model = predefinedMicroplateList.getModel();
			for(int i=0; i<model.getSize(); i++)
			{
				Object element = model.getElementAt(i);
				if(element instanceof Microplate && ((Microplate)element).getMicroplateID().compareToIgnoreCase(typeID) == 0)
				{
					predefinedMicroplateList.setSelectedIndex(i);
					foundType = true;
					break;
				}
			}
		}
		if(foundType)
		{
			microplatePredefined.setSelected(true);
			
			predefinedMicroplateScrollPane.setVisible(true);
			predefinedMicroplateLabel.setVisible(true);
			
			numberLabel.setVisible(false);
			numberPanel.setVisible(false);
			distanceLabel.setVisible(false);
			distancePanel.setVisible(false);

			numPositionsSelectionLabel.setVisible(false);
			numPositionsSelection.setVisible(false);
		}
		else if(configuration.getMicroplatePositions().isAliasMicroplate())
		{
			arbitraryPositions.setSelected(true);
			
			predefinedMicroplateScrollPane.setVisible(false);
			predefinedMicroplateLabel.setVisible(false);
			
			numberLabel.setVisible(false);
			numberPanel.setVisible(false);
			distanceLabel.setVisible(false);
			distancePanel.setVisible(false);

			numPositionsSelectionLabel.setVisible(true);
			numPositionsSelection.setVisible(true);
		}
		else
		{
			microplateCustom.setSelected(true);
			
			predefinedMicroplateScrollPane.setVisible(false);
			predefinedMicroplateLabel.setVisible(false);
			
			numberLabel.setVisible(true);
			numberPanel.setVisible(true);
			distanceLabel.setVisible(true);
			distancePanel.setVisible(true);

			numPositionsSelectionLabel.setVisible(false);
			numPositionsSelection.setVisible(false);
		}
		
		widthField.setValue(configuration.getMicroplatePositions().getWellWidth());
		heightField.setValue(configuration.getMicroplatePositions().getWellHeight());
		numXField.setValue(configuration.getMicroplatePositions().getNumWellsX());
		numYField.setValue(configuration.getMicroplatePositions().getNumWellsY());
		numPositionsSelection.setValue(configuration.getMicroplatePositions().getNumWellsX());
	}

	@Override
	public boolean saveData(MicroplateJobConfiguration configuration)
	{
		if(microplatePredefined.isSelected())
		{
			Microplate type = predefinedMicroplateList.getSelectedValue();
			if(type != null)
			{
				configuration.getMicroplatePositions().setMicroplateType(type);
			}
		}
		else if(microplateCustom.isSelected())
		{
			configuration.getMicroplatePositions().setNumWellsX(((Number)numXField.getValue()).intValue());
			configuration.getMicroplatePositions().setNumWellsY(((Number)numYField.getValue()).intValue());
			configuration.getMicroplatePositions().setWellHeight(((Number)heightField.getValue()).doubleValue());
			configuration.getMicroplatePositions().setWellWidth(((Number)widthField.getValue()).doubleValue());
		}
		else if(arbitraryPositions.isSelected())
		{
			configuration.getMicroplatePositions().setNumPositions(((Number)numPositionsSelection.getValue()).intValue());
		}
		return true;
	}

	@Override
	public void setToDefault(MicroplateJobConfiguration configuration)
	{
		// Do nothing.

	}

	@Override
	public String getPageName()
	{
		return "Microplate Type";
	}

	private void initializeMicroplateTypes()
	{
		
		
		class PredefinedMicroplateTypesCellRenderer extends JLabel implements ListCellRenderer<Microplate> 
		{
			/**
			 * Serial Version UID
			 */
			private static final long	serialVersionUID	= 239161111656492466L;

			@Override
			public Component getListCellRendererComponent(JList<? extends Microplate> list, Microplate value, int index, boolean isSelected, boolean cellHasFocus)
		    {
				String text = value.getMicroplateName();
		        setText(text);
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
		        setEnabled(list.isEnabled());
		        setFont(list.getFont());
		        setOpaque(true);
		        return this;
		     }
		 }

		predefinedMicroplateList.setCellRenderer(new PredefinedMicroplateTypesCellRenderer());
	}
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		List<Microplate> microplateTypes = client.getAddonProvider().getMicroplateTypes();
		
		predefinedMicroplateList = new JList<Microplate>(microplateTypes.toArray(new Microplate[microplateTypes.size()]));
		
		predefinedMicroplateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if(microplateTypes.size() > 0)
			predefinedMicroplateList.setSelectedIndex(0);
		else
			microplatePredefined.setVisible(false);
		initializeMicroplateTypes();
		predefinedMicroplateScrollPane = new JScrollPane(predefinedMicroplateList);
		
		
		GridBagLayout		layout				= new GridBagLayout();
		setLayout(layout);

		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		StandardFormats.addGridBagElement(new JLabel("Microplate type:"), layout, newLineConstr, this);
		ButtonGroup wellTypeButtonGroup = new ButtonGroup();
		wellTypeButtonGroup.add(microplatePredefined);
		wellTypeButtonGroup.add(microplateCustom);
		wellTypeButtonGroup.add(arbitraryPositions);
		StandardFormats.addGridBagElement(microplatePredefined, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(microplateCustom, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(arbitraryPositions, layout, newLineConstr, this);
		ActionListener measurementTypeListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(microplatePredefined.isSelected())
				{
					predefinedMicroplateScrollPane.setVisible(true);
					predefinedMicroplateLabel.setVisible(true);
					
					numberLabel.setVisible(false);
					numberPanel.setVisible(false);
					distanceLabel.setVisible(false);
					distancePanel.setVisible(false);

					numPositionsSelectionLabel.setVisible(false);
					numPositionsSelection.setVisible(false);
				}
				else if(arbitraryPositions.isSelected())
				{
					predefinedMicroplateScrollPane.setVisible(false);
					predefinedMicroplateLabel.setVisible(false);
					
					numberLabel.setVisible(false);
					numberPanel.setVisible(false);
					distanceLabel.setVisible(false);
					distancePanel.setVisible(false);

					numPositionsSelectionLabel.setVisible(true);
					numPositionsSelection.setVisible(true);
				}
				else if(microplateCustom.isSelected())
				{
					predefinedMicroplateScrollPane.setVisible(false);
					predefinedMicroplateLabel.setVisible(false);
					
					numberLabel.setVisible(true);
					numberPanel.setVisible(true);
					distanceLabel.setVisible(true);
					distancePanel.setVisible(true);

					numPositionsSelectionLabel.setVisible(false);
					numPositionsSelection.setVisible(false);
				}
			}
		};
		microplatePredefined.addActionListener(measurementTypeListener);
		arbitraryPositions.addActionListener(measurementTypeListener);
		microplateCustom.addActionListener(measurementTypeListener);
		
		StandardFormats.addGridBagElement(numPositionsSelectionLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(numPositionsSelection, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(predefinedMicroplateLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(predefinedMicroplateScrollPane, layout, newLineConstr, this);
		
		StandardFormats.addGridBagElement(numberLabel, layout, newLineConstr, this);
		numberPanel.add(numXField);
		numberPanel.add(numYField);
		StandardFormats.addGridBagElement(numberPanel, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(distanceLabel, layout, newLineConstr, this);
		distancePanel.add(widthField);
		distancePanel.add(heightField);
		StandardFormats.addGridBagElement(distancePanel, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(new JPanel(), layout, bottomConstr, this);

		setBorder(new TitledBorder("Microplate Type"));
	}
}
