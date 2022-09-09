package edu.upenn.cit594.datamanagement;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Storage class for arguments passed to main 
 * @author andrii podhornyi 
 *
 */
public class ArgumentStorage {
	
	/**
	 * Internal set that will hold names of arguments 
	 */
	private HashSet<String> names = new HashSet<>();
	
	/**
	 * Initializes internal set with names of arguments to main passed as array 
	 * @param arguments String[] of arguments to main 
	 */
	public ArgumentStorage(String[] arguments) {
		
		// pattern to extract name of argument from string 
		Pattern pattern = Pattern.compile("^--(?<name>.+?)=");
		Matcher matcher = pattern.matcher("");
		for(String argument : arguments) {
			
			// check for validity is done in main. Here we only split them and add to set 
			matcher.reset(argument).find();
			this.names.add(matcher.group("name"));
		}
	}
	
	/**
	 * Returns set of parameter names 
	 * @return set of strings 
	 */
	public Set<String> getArguments(){
		return names;
	}
}
