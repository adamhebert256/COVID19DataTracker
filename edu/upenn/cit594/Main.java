package edu.upenn.cit594;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cit594.datamanagement.ArgumentStorage;
import edu.upenn.cit594.datamanagement.CovidCSVParser;
import edu.upenn.cit594.datamanagement.CovidJsonParser;
import edu.upenn.cit594.datamanagement.CovidParser;
import edu.upenn.cit594.datamanagement.PopulationParser;
import edu.upenn.cit594.datamanagement.PropertyParser;
import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.processor.Processor;
import edu.upenn.cit594.ui.UserInterface;

public class Main {


	/**
	 * Immutable set of acceptable arguments  
	 */
	private Set<String> names = Set.of("log", "covid", "population", "properties");
	
	/**
	 * Map to store provided arguments 
	 */
	private HashMap<String, String> arguments = new HashMap<>();
	
	/**
	 * Pattern validation for provided arguments 
	 */
	private Pattern pattern = Pattern.compile("^--(?<name>.+?)=(?<value>.+)$");
	private Matcher matcher = pattern.matcher("");
	
	/**
	 * True if covid file has json extension. False otherwise.
	 */
	private boolean isJson;


	public static void main(String[] args) {
		Main process = new Main();
		try {
			process.run(args);
		} catch (Exception e) {
			UserInterface.displayError("Exception occured during reading from or writing to files");
		}
	}


	private void run(String[] args) throws Exception {

		// First, we split each argument by type and filename 
		for(int i = 0; i < args.length; i++) {
			if(matcher.reset(args[i]).find()) {
				
				// get groups from the matcher 
				String name = matcher.group("name");
				String value = matcher.group("value");
				
				// error occurs when type does not exist in set, present in the map of passed arguments or file can not be read 
				if(arguments.containsKey(name) || !names.contains(name) || !fileReachable(name, value)) {
					UserInterface.displayError("Invalid arguments names provided. Make sure they are valid and not repeated");
					return;
				}
				
				// add new filetype to map 
				arguments.put(name, value);
			}
			else {
				UserInterface.displayError("Arguments should be passed as follows: --name=value");
				return;
			}
		}
		
		// check if covid file can be processed 
		if(!covidFileValidOrNotExist()) {
			UserInterface.displayError("If covid file is provided, then it must have .json or .txt extension"); 
			return;
		}
		
		// set logger and pass arguments as the first log event 
		Logger logger = Logger.getLoggerInstance();
		logger.setPath(arguments.get("log"));
		logger.log(String.join(" ", args));
		
		
		String covidFilemame = arguments.get("covid");
		
		// instantiate covid parser. If filename is not provided, then set it to null/
		// Otherwise instantiate type of parser based on isJson variable 
		CovidParser covidParser = covidFilemame == null ? null :
			(isJson ? new CovidJsonParser(covidFilemame) : new CovidCSVParser(covidFilemame));
		
		// set population parser to new instance or null (it depends if population type is present in map) 
		PopulationParser populationParser = arguments.containsKey("population")
				? new PopulationParser(arguments.get("population")) : null;
		
		// set property parser to new instance or null 
		PropertyParser propertyParser = arguments.containsKey("properties")
				? new PropertyParser(arguments.get("properties")) : null;
		Processor processor = new Processor(covidParser, populationParser, propertyParser, new ArgumentStorage(args));
		UserInterface ui = new UserInterface(processor);
		ui.start();
	}

	
	/**
	 * If covid file is provided, then checks if it has valid extension. 
	 * @return true if file has valid extension or does not exist. False otherwise 
	 */
	private boolean covidFileValidOrNotExist() {
		String filename = arguments.get("covid");

		// this function is sanity check only. If file is not provided, then it should return true anyway 
		if(filename == null) return true;
		int index = filename.lastIndexOf('.');

		// -1 means no extension is provided at all
		if(index == -1) return false;
		String extension = filename.substring(index);
		if(extension.equalsIgnoreCase(".txt")) return true;
		else if(extension.equalsIgnoreCase(".json")) {
			isJson = true;
			return true;
		}
		return false;
	}

	/**
	 * Checks if provided file can be read. If file is of log type, then checks if it exists and can be written 
	 * @param type type of file 
	 * @param filename name of the file 
	 * @return true if file can be processed. False otherwise 
	 */
	private boolean fileReachable(String type, String filename) {
		File file = new File(filename);
		if(type.equals("log")) {
			return !file.exists() || file.canWrite();
		}
		else return file.canRead();
	}
}
