package com.sivakg2000.talk.log;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
/**
 * Simple example of creating a Log4j appender that will
 * write to a JTextArea. 
 */
public class LabelAppender extends WriterAppender {
	
	static private JLabel jLabel = null;
	
	/** Set the target JTextArea for the logging information to appear. */
	static public void setLabel(JLabel jLabel) {
		LabelAppender.jLabel = jLabel;
	}
	@Override
	/**
	 * Format and then append the loggingEvent to the stored
	 * JTextArea.
	 */
	public void append(LoggingEvent loggingEvent) {
		final String message = this.layout.format(loggingEvent);

		// Append formatted message to textarea using the Swing Thread.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//jTextArea.append(message);
                            jLabel.setText(message);
			}
		});
	}
}
