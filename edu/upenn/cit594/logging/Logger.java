package edu.upenn.cit594.logging;

import java.io.FileWriter;
import java.io.IOException;

public class Logger {
	
	private FileWriter writer;

	private Logger() {}
	
	private static Logger instance = new Logger();
	
	/**
	 * Returns singleton instance of logger class 
	 * @return
	 */
	public static Logger getLoggerInstance() {
		return instance;
	}
	
	/**
	 * Sets the file to which the log events are written. If
	 * filename is null, output is directed to System.err 
	 * @param filename name of file to write to 
	 * @throws IOException may be thrown by internal writer 
	 */
	public void setPath(String filename) throws IOException {
		if(writer != null) writer.close();
		if(filename == null) writer = null;
		else writer = new FileWriter(filename, true);
	}
	
	/**
	 * Logs event to provided file. Use {@code setPath} to set the destination file.
	 * Otherwise all log events are written to System.err
	 * @param text String to be written as log event 
	 * @throws IOException may be thrown by internal writer 
	 */
	public void log(String text) throws IOException {
		
		// add current time of event 
		String timedValue = System.currentTimeMillis() + " " + text + '\n'; 
		
		// write to file if provided and to err otherwise 
		if(writer == null) System.err.print(timedValue);
		else {
			writer.write(timedValue);
			writer.flush();
		}
	}

}
