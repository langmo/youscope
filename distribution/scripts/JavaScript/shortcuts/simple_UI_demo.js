/* * * * * * * * * * * * * * * * * * * * * * * * * * Oscillating Input  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

 * This script is written in JavaScript and exemplifies how to control the YouScope UI by scripting.
 * It shows a little text message in a frame, together with a button to close the frame.
 * This example script was written by Moritz Lang
 * and is licensed under the GNU GPL.

 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

// Create the frame.
frame = youscopeClient.createFrame();
// Set the title of the frame.
frame.setTitle("Example Frame");

// Create the close button
okButton = new javax.swing.JButton("Close");
// Add action listener to button
listenerFct = { actionPerformed: function(arg0) { frame.setVisible(false); } };
listener = new java.awt.event.ActionListener(listenerFct);
okButton.addActionListener(listener);

// Create the text box
textBox = new javax.swing.JTextArea();
textBox.setText("This frame exemplifies\nhow the YouScope GUI can be controlled\nby a JavaScript script.\nThe whole frame, including the functionality\nof the \"Close\" button\nare implemented by scripting. For more information,\nsee the documentation.");
textBox.setEditable(false);

// Add the button and the textbox to the frame
contentPane = new javax.swing.JPanel(new java.awt.BorderLayout);
contentPane.add(new javax.swing.JScrollPane(textBox), java.awt.BorderLayout.CENTER);
contentPane.add(okButton, java.awt.BorderLayout.SOUTH);
frame.setContentPane(contentPane);

// Display frame in its preferred size.
frame.pack();
frame.setVisible(true);
