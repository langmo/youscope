/* * * * * * * * * * * * * * * * * * * * * * * * * * Oscillating Input  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This script is written in JavaScript and exemplifies how to control the YouScope UI by scripting.
 * It shows a little text message in a frame, together with a button to close the frame.
 * This example script was written by Moritz Lang
 * and is licensed under the GNU GPL.
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

// Packages used by this script.
var SwingGui = JavaImporter(Packages.javax.swing,
		Packages.javax.swing.filechooser,
		Packages.javax.swing.border,
		java.awt,
		java.io,
		java.util,
		java.lang,
		java.awt.event);

// Start script
runConfiguration();

/**
* Returns an array of the names of the properties of the respective device.
* @param deviceName name of the device to querry the properties for.
* @return Array of device property names.
**/
function getDeviceProperties(deviceName)
{
	properties = youscopeServer.getMicroscope().getDevice(deviceName).getProperties();
	var propertyNames = Array(properties.length);
	for(i = 0; i < properties.length; i++)
	{
		propertyNames[i] = properties[i].getPropertyID();
	}
	return propertyNames;
}
/**
* Function that sets the items of the second combo box to the properties of the device indicated by the
* first combo box.
* @param deviceComboBox The combo box whose selected item corresponds to the active device's name.
* @param propertyComboBox The combo box whose items should be set to the properties of the device.
**/
function actualizePropertyValues(deviceComboBox, propertyComboBox)
{
	propertyComboBox.removeAllItems(); 
	propertyNames = getDeviceProperties(deviceComboBox.getSelectedItem().toString());
	for(i=0; i<propertyNames.length; i++)
	{
		propertyComboBox.addItem(propertyNames[i]);
	}
}
/**
* Returns the current value of the given device property as a float.
* @param deviceName the name of the device.
* @param propertyName the name of the property.
* @return the current device property's value as a float.
**/
function getPropertyValue(deviceName, propertyName)
{
	return java.lang.Double.parseDouble(youscopeServer.getMicroscope().getDevice(deviceName).getProperty(propertyName).getValue());
}
/**
* Sets the current value of the given device property.
* @param deviceName the name of the device.
* @param propertyName the name of the property.
* @param value the value the device property should be set to (float).
**/
function setPropertyValue(deviceName, propertyName, value)
{
	youscopeServer.getMicroscope().getDevice(deviceName).getProperty(propertyName).setValue(value);
}

/**
* Maximizes the width of the component when it is layed out.
* @param comp The component whose layout should be changed.
**/
function maximizeWidth(comp)
{
	with (SwingGui) 
	{
		comp.setAlignmentX(Component.CENTER_ALIGNMENT);
		comp.setAlignmentY(Component.CENTER_ALIGNMENT);
		dim = comp.getPreferredSize();
		dim.width = Integer.MAX_VALUE;
		comp.setMaximumSize(dim);
	}

}

/**
* Lets the user choose the two device properties which should be correlated agains each other
* and the range of values the device property corresponding to the x-axis should be changed.
**/
function runConfiguration()
{
	with (SwingGui) 
	{
	// Get device names
	devices = youscopeServer.getMicroscope().getDevices();
	var devicesNames = Array(devices.length);
	for(i = 0; i < devices.length; i++)
	{
		devicesNames[i] = devices[i].getDeviceID();
	}

	// Create frame.
	frame = youscopeClient.createFrame();
	// Set the title of the frame.
	frame.setTitle("Correlation between two properties");
	frame.setResizable(false);
	frame.setMaximizable(false);

	// Initialize combo boxes to select the device property for the x-axis
	xDeviceNameField = new JComboBox(devicesNames);
	xPropertyNameField = new JComboBox();
	xDeviceNameListenerFct = { actionPerformed: function(arg0) {actualizePropertyValues(xDeviceNameField, xPropertyNameField); }};
	xDeviceNameListener = new ActionListener(xDeviceNameListenerFct);
	xDeviceNameField.addActionListener(xDeviceNameListener);

	// Initialize panel to choose device property for the x-axis
	xPanel = new JPanel();
	xPanel.setLayout(new BoxLayout(xPanel, BoxLayout.Y_AXIS));
	xDeviceNameLabel = new JLabel("Device:");
	maximizeWidth(xDeviceNameLabel);
	xPanel.add(xDeviceNameLabel);
	xPanel.add(xDeviceNameField);
	xPropertyNameLabel = new JLabel("Property:");
	maximizeWidth(xPropertyNameLabel);
	xPanel.add(xPropertyNameLabel);
	xPanel.add(xPropertyNameField);
	minMaxLabel = new JLabel("<html>Values the X-Axis property should be varied<br />(minValue : stepSize : maxValue)</html>");
	maximizeWidth(minMaxLabel);
	xPanel.add(minMaxLabel);
	minValueField = new JFormattedTextField(0.0);
	stepField = new JFormattedTextField(0.1);
	maxValueField = new JFormattedTextField(1.0);
	valueChooserPanel = new JPanel(new GridLayout(1, 3, 2, 2));
	valueChooserPanel.add(minValueField);
	valueChooserPanel.add(stepField);
	valueChooserPanel.add(maxValueField);
	xPanel.add(valueChooserPanel);
	waitTimeLabel = new JLabel("<html>Time in ms needed for the y-axis device to react:</html>");
	maximizeWidth(waitTimeLabel);
	xPanel.add(waitTimeLabel);
	waitTimeField = new JFormattedTextField(1000);
	xPanel.add(waitTimeField);
	xPanel.setBorder(new TitledBorder("X-Axis Device Property"));

	// Initialize combo boxes to select the device property for the y-axis
	yDeviceNameField = new JComboBox(devicesNames);
	yPropertyNameField = new JComboBox();
	yDeviceNameListenerFct = { actionPerformed: function(arg0) {actualizePropertyValues(yDeviceNameField, yPropertyNameField); }};
	yDeviceNameListener = new ActionListener(yDeviceNameListenerFct);
	yDeviceNameField.addActionListener(yDeviceNameListener);

	// Initialize panel to choose device property for the y-axis
	yPanel = new JPanel();
	yPanel.setLayout(new BoxLayout(yPanel, BoxLayout.Y_AXIS));
	yDeviceLabel = new JLabel("Device:");
	maximizeWidth(yDeviceLabel);
	yPanel.add(yDeviceLabel);
	yPanel.add(yDeviceNameField);
	yPropertyLabel = new JLabel("Property:");
	maximizeWidth(yPropertyLabel);
	yPanel.add(yPropertyLabel);
	yPanel.add(yPropertyNameField);
	yPanel.setBorder(new TitledBorder("Y-Axis Device Property"));

	// Create the run button
	okButton = new javax.swing.JButton("Run Correlation");
	maximizeWidth(okButton);
	// Add action listener to button
	okButtonListenerFct = { actionPerformed: function(arg0) 
	{ 
		// Get selection
		xDevice = xDeviceNameField.getSelectedItem().toString();
		xProperty = xPropertyNameField.getSelectedItem().toString();
		yDevice = yDeviceNameField.getSelectedItem().toString();
		yProperty = yPropertyNameField.getSelectedItem().toString();
		minValue = Double.parseDouble(minValueField.getValue());
		step = Double.parseDouble(stepField.getValue());
		maxValue = Double.parseDouble(maxValueField.getValue());		
		waitTime = Math.round(Double.parseDouble(waitTimeField.getValue()));

		// Save selected values
		youscopeClient.getProperties().setProperty("CSB::Correlation.xDevice", xDevice);
		youscopeClient.getProperties().setProperty("CSB::Correlation.xProperty", xProperty);
		youscopeClient.getProperties().setProperty("CSB::Correlation.yDevice", yDevice);
		youscopeClient.getProperties().setProperty("CSB::Correlation.yProperty", yProperty);
		youscopeClient.getProperties().setProperty("CSB::Correlation.minValue", minValue);
		youscopeClient.getProperties().setProperty("CSB::Correlation.step", step);
		youscopeClient.getProperties().setProperty("CSB::Correlation.maxValue", maxValue);
		youscopeClient.getProperties().setProperty("CSB::Correlation.waitTime", waitTime);

		// Hide this frame
		frame.setVisible(false); 
		
		// Show correlation frame and run correlation.
		runCorrelation(xDevice, xProperty, yDevice, yProperty, minValue, step, maxValue, waitTime); 
	}};
	okButtonListener = new ActionListener(okButtonListenerFct);
	okButton.addActionListener(okButtonListener);

	// Load last settings
	xDeviceNameField.setSelectedItem(youscopeClient.getProperties().getProperty("CSB::Correlation.xDevice", ""));
	actualizePropertyValues(xDeviceNameField, xPropertyNameField);
	xPropertyNameField.setSelectedItem(youscopeClient.getProperties().getProperty("CSB::Correlation.xProperty", ""));
	yDeviceNameField.setSelectedItem(youscopeClient.getProperties().getProperty("CSB::Correlation.yDevice", ""));
	actualizePropertyValues(yDeviceNameField, yPropertyNameField);
	yPropertyNameField.setSelectedItem(youscopeClient.getProperties().getProperty("CSB::Correlation.yProperty", ""));
	minValueField.setValue(youscopeClient.getProperties().getProperty("CSB::Correlation.minValue", 0.0));
	stepField.setValue(youscopeClient.getProperties().getProperty("CSB::Correlation.step", 0.1));
	maxValueField.setValue(youscopeClient.getProperties().getProperty("CSB::Correlation.maxValue", 1.0));
	waitTimeField.setValue(youscopeClient.getProperties().getProperty("CSB::Correlation.waitTime", 1000));

	// Layout all components
	contentPane = new JPanel();
	contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
	contentPane.add(xPanel);
	contentPane.add(yPanel);
	contentPane.add(okButton);
	frame.setContentPane(contentPane);

	// Display frame in its preferred size.
	frame.pack();
	frame.setVisible(true);
	}
}
function runCorrelation(xDevice, xProperty, yDevice, yProperty, minValue, stepSize, maxValue, waitTime)
{
	with (SwingGui) 
	{
	// initialize x and y values
	numValues = Math.floor(1.0 + (maxValue - minValue) / stepSize);
	var xValues = new Array(numValues);
	var yValues = new Array(numValues);
	for(i=0; i<numValues; i++)
	{
		xValues[i] = minValue + i * stepSize;
		yValues[i] = 0.0;
	}
	
	// Create frame.
	frame = youscopeClient.createFrame();
	// Set the title of the frame.
	frame.setTitle("Correlation between " + xDevice + "." + xProperty + " and " + yDevice + "." + yProperty);
	frame.setResizable(false);

	// Create axes to display outcome
	axisFct = 
	{ 
		paintIcon: function(component, g, x, y) 
		{
			try
			{
			width = this.getIconWidth();
			height = this.getIconHeight();

			// Fill Background
			g.setColor(Color.WHITE);
			g.fillRect(x, y, width, height);

			// Let margin around plot.
			margin = 40;
			x+=margin;
			y+=margin;
			width-=2*margin;
			height-=2*margin;

			// Draw axes
			g.setColor(Color.BLACK);
			g.drawLine(x, y - margin/2, x,  y + height + 2); 
			g.drawLine(x - 2, y + height, x + width + margin/2, y + height);
			g.drawLine(x + width, y + height - 1, x + width , y + height + 1);
			g.drawLine(x - 2, y, x + 2, y);
			arrowSize = 4;
			g.fillPolygon(new Array(x + width + margin/2 + 1, x + width + margin/2 -arrowSize, x + width + margin/2-arrowSize), new Array(y + height, y + height -arrowSize, y + height+arrowSize + 1), 3);
			g.fillPolygon(new Array(x + 1, x - arrowSize, x + arrowSize +1), new Array(y - margin/2 - 1, y - margin/2 + arrowSize, y - margin/2 + arrowSize), 3);
			
			// Get min and max y values
			minY = NaN;
			maxY = NaN;
			for(i=0; i<numValues; i++)
			{
				if(isNaN(minY) || yValues[i] < minY)
					minY = yValues[i];
				if(isNaN(maxY) || yValues[i] > maxY)
					maxY = yValues[i];
			}
			if(isNaN(maxY))
				maxY = 1.0;
			if(isNaN(minY))
				minY = 0.0;
			if(minY == maxY)
			{
				if(minY > 0)
					minY = 0.0;
				else if(minY < 0)
					maxY = 0.0;
				else
					maxY = 1.0;
			}	

			// Draw curve
			g.setColor(Color.BLUE);
			for(i=0; i<numValues-1; i++)
			{
				x1 = Math.round((xValues[i] - minValue) / (maxValue - minValue) * width + x);
				y1 = Math.round((1-(yValues[i] - minY) / (maxY - minY)) * height + y);
				x2 = Math.round((xValues[i + 1] - minValue) / (maxValue - minValue) * width + x);
				y2 = Math.round((1-(yValues[i + 1] - minY) / (maxY - minY)) * height + y);
				if(isNaN(x1) || isNaN(y1) || isNaN(x2) || isNaN(y2))
					continue;
				g.drawLine(x1,y1,x2,y2)
			} 
			
			// Draw min and max values
			g.setColor(Color.BLACK);
			formatter = new Formatter();
			stringHeight = g.getFontMetrics().getAscent() - 2;
			minXValueString = (new Formatter()).format("%2.2f", [minValue]);
			minXValueWidth = g.getFontMetrics().stringWidth(minXValueString);
			maxXValueString = (new Formatter()).format("%2.2f", [maxValue]);
			maxXValueWidth = g.getFontMetrics().stringWidth(maxXValueString);
			g.drawString(minXValueString, x - minXValueWidth/2, y + height + stringHeight + 4);
			g.drawString(maxXValueString, x + width - maxXValueWidth/2, y + height  + stringHeight + 4);

			minYValueString = (new Formatter()).format("%2.2f", [minY]);
			minYValueWidth = g.getFontMetrics().stringWidth(minYValueString);
			maxYValueString = (new Formatter()).format("%2.2f", [maxY]);
			maxYValueWidth = g.getFontMetrics().stringWidth(maxYValueString);
			g.drawString(minYValueString, x - minYValueWidth - 2, y + height + stringHeight/2);
			g.drawString(maxYValueString, x - maxYValueWidth - 2, y + stringHeight/2);

			// Draw axes names
			yLabelString = yDevice + "." + yProperty;
			g.drawString(yLabelString, x+6, y-margin/4);
			xLabelString = xDevice + "." + xProperty;
			g.drawString(xLabelString, x+width+4, y+height-4);
			}
			catch(e)
			{
				// Do nothing...
			}
		},
		getIconHeight: function()
		{
			return 500;
		},
		getIconWidth: function()
		{
			return 500;
		}
	};
	axisIcon = new JavaAdapter(Icon, axisFct);
	axisComponent = new JLabel(axisIcon);
	axisComponent.setBorder(new LineBorder(Color.BLACK, 1));
	axisComponent.setOpaque(true);
	axisComponent.setBackground(Color.WHITE);
	axisComponent.setDoubleBuffered(false);

	// Close and save button.
	buttonsPanel = new JPanel(new GridLayout(1,2,2,2));
	saveButton = new JButton("Export to Excel");
	saveButtonListenerFct = { actionPerformed: function(arg0) 
	{ 
		saveData(xDevice, xProperty, yDevice, yProperty, xValues, yValues);
	}};
	saveButton.addActionListener(new ActionListener(saveButtonListenerFct));
	buttonsPanel.add(saveButton);
	closeButton = new JButton("Close");
	closeButtonListenerFct = { actionPerformed: function(arg0) 
	{ 
		frame.setVisible(false);
	}};
	closeButton.addActionListener(new ActionListener(closeButtonListenerFct));
	buttonsPanel.add(closeButton);

	// Layout all components
	contentPane = new JPanel(new BorderLayout());
	contentPane.add(new JLabel("<html><h1>Correlation between " + xDevice + "." + xProperty + " and " + yDevice + "." + yProperty + "</h1></html>"), BorderLayout.NORTH);
	contentPane.add(axisComponent, BorderLayout.CENTER);
	contentPane.add(buttonsPanel, BorderLayout.SOUTH);
	frame.setContentPane(contentPane);

	// Display frame in its preferred size.
	frame.pack();
	frame.setVisible(true);
	}

	correlationExecuter = 
	{
		run : function()
		{
			for(j=0; j<numValues; j++)
			{
				if(!frame.isVisible())
					break;
				setPropertyValue(xDevice, xProperty, xValues[j]);
				java.lang.Thread.sleep(waitTime);
				yValues[j] = getPropertyValue(yDevice, yProperty);
				axisComponent.repaint();
			}
		}
	};
	(new java.lang.Thread(new java.lang.Runnable(correlationExecuter))).start();
}

/**
* Queries the user for a file path and saves the data to this location.
**/
function saveData(xDevice, xProperty, yDevice, yProperty, xValues, yValues)
{
	with (SwingGui) 
	{
	// Query for file where the data should be save to
	folderToOpen = youscopeClient.getProperties().getProperty("CSB:Correlation:LastDataFolder", ".");
	fileChooser = new JFileChooser(folderToOpen);
	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Excel Files (.csv)", ["csv"]));
	fileChooser.setSelectedFile(new File("data.csv"));
	returnVal = fileChooser.showDialog(null, "Save");
	if(returnVal != JFileChooser.APPROVE_OPTION)
		return;
	file = fileChooser.getSelectedFile();
	youscopeClient.getProperties().setProperty("CSB:Correlation:LastDataFolder", file.getParent());
	if(file.exists())
	{
		youscopeClient.sendError("File " + file.toString()+" already exists. To prevent loss of data, please choose another file.");
		return;
	}

	// Create data
	data = "\"" + xDevice + "." + xProperty + "\";\"" + yDevice + "." + yProperty + "\"";
	for(i=0; i<xValues.length; i++)
	{
		data += "\n" + Double.toString(xValues[i]) + ";" + Double.toString(yValues[i]);
	}
	try
	{
		fileStream = new PrintStream(file);
		fileStream.print(data);
		fileStream.close();
	} 
	catch (e)
	{
		youscopeClient.sendError("Could not save file.", e);
		return;
	}
	youscopeClient.sendMessage("Data saved to file " + file.toString()+".");
	}
}
