/**
 * 
 */
package ch.ethz.csb.youscope.starter;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author langmo
 */
class ErrorConsumer
{
    static void consumeException(String description, Exception exception)
    {
    	exception.printStackTrace();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(description), BorderLayout.NORTH);
        String message = "";
        Throwable iter = exception;
        for (int i = 0;; i++)
        {
            if (i > 0)
                message += "\n";
            message += iter.getClass().getSimpleName() + ": " + iter.getMessage();
            iter = iter.getCause();
            if (iter == null)
                break;
        }
        JTextArea exceptionDescription = new JTextArea(message);
        exceptionDescription.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(exceptionDescription);
        scrollPane.setPreferredSize(new Dimension(700, 200));
        panel.add(scrollPane, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(null, panel, "YouScope could not start", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
