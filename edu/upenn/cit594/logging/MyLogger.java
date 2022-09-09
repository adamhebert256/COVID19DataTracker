package edu.upenn.cit594.logging;

import java.io.*;


public class MyLogger {
	private PrintWriter out;
	
	//1. private constructor
	private MyLogger() {}
	
	//2. singleton instance
	private static MyLogger instance = new MyLogger();
	
	// 3. singleton accessor method
	public static MyLogger getLoggerInstance() { return instance ; }
	
	/**
	 * set or change output destination for logger
	 * @author Adam Hebert
	 * @param filname
	 */
	public void setDestination(String FileName) throws Exception {
		//closes last file if one exists
		try {
			out.close();
		}
		catch(Exception e) {
		}
		//if no log file was inputted, sets out = null
		if (FileName == null) {
			out = null;
			return;
		}
		
		File logger = new File(FileName);
		boolean loggerExists = logger.exists();
		//if the logger file already exists
		if (loggerExists) {
			boolean loggerWritable = logger.canWrite();
			//checks if we can write to the existing log file
			if (!loggerWritable) {
				throw new Exception("Log file cannot be opened for writing.");
			}
			FileWriter myFileWriter = new FileWriter(FileName, true);
			out = new PrintWriter(myFileWriter, true);
		}
		//if the log file does not already exist
		else {
			//attempts to create the log file
			try {
				logger.createNewFile();
				boolean loggerWritable = logger.canWrite();
				if (!loggerWritable) {
					throw new Exception("Log file cannot be created for writing.");
				}
				FileWriter myFileWriter = new FileWriter(FileName);
				out = new PrintWriter(myFileWriter, true);
			}
			//throws exception if we can't initialize the inputted log file name
			catch(Exception e) {
				throw new Exception("The inputted log file cannot be correctly initialized.");
			}
		}
	}
	

	/**
	 * writes events to the logger
	 * @author Adam Hebert
	 * @param message to write
	 */
	public void log(String msg){
		Long timestamp = System.currentTimeMillis();
		if(out == null) {
			System.err.println(timestamp.toString() + " " + msg);
			System.err.flush();
			
		}
		else {
			out.println(timestamp.toString() + " " + msg);
			out.flush();
		}
	}
}