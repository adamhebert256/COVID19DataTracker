package edu.upenn.cit594.datamanagement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.util.CovidData;

/**
 * Base class for covid readers 
 * @author andrii podhornyi 
 *
 */
public abstract class CovidParser {

	
	/**
	 * Name of the file to be parsed 
	 */
	protected String filename;
	
	/**
	 * Logger instance
	 */
	 protected Logger logger = Logger.getLoggerInstance();
	
	
	/**
	 * pre-compiled date pattern 
	 */
	private Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
	protected Matcher dateMatcher = datePattern.matcher("");
	
	/**
	 * pre-compiled zip pattern 
	 */
	private Pattern zipPattern = Pattern.compile("^[0-9]{5}$");
	protected Matcher zipMatcher = zipPattern.matcher("");
	
	
	
	public CovidParser(String filename) {
		this.filename = filename;
	}
	
	/**
	 * Parses file into list of CovidData objects 
	 * @return List of CovidData objects
	 * @throws Exception may be thrown by internal reader or parser 
	 */
	public abstract List<CovidData> getCovidData() throws Exception;
}
