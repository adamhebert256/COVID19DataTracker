package edu.upenn.cit594.datamanagement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.util.PopulationData;


/**
 * Parses population file into list of PopulationData objects 
 * @author andrii podhornyi 
 *
 */
public class PopulationParser {

	/**
	 * Stores the name of the file to be opened for parsing 
	 */
	private String filename;
	
	/**
	 * Field indicators for required data 
	 */
	private int zipField, populationField;
	
	
	public PopulationParser(String filename) {
		this.filename = filename;
	}
	
	
	/**
	 * Parses file line by line, creates {@code PopulationData} objects for each valid line.
	 * Line is considered valid if zip field is 5-digit code and population field is valid integer  
	 * @return list of PopulationData objects 
	 * @throws IOException can be thrown by internal reader 
	 */
	public List<PopulationData> getPopulationData() throws IOException {
		
		Logger.getLoggerInstance().log(filename);
		
		// pattern for zip field 
		Pattern pattern = Pattern.compile("^\\d{5}$");
		Matcher matcher = pattern.matcher("");
		
		// holds parse results 
		LinkedList<PopulationData> data = new LinkedList<>();
		
		try(FileReader fr = new FileReader(filename); BufferedReader br = new BufferedReader(fr)){
			CSVLexer lexer = new CSVLexer(br);
			
			// first row is always header row, therefore field numbers should be extracted 
			setInitialFields(lexer.readRow());
			String[] fields;
			int zipData, populationData;
			
			// read file until EOF is reached 
			while((fields = lexer.readRow()) != null) {
				
				// pre-defined matcher is reset with current zip field for checking 
				if(matcher.reset(fields[zipField]).find())
					zipData = Integer.parseInt(fields[zipField]);
				else continue;
				
				// entire row is ignored if population is not valid integer 
				try {
					populationData = Integer.parseInt(fields[populationField]);
				} catch (NumberFormatException e) {
					continue;
				}
				data.add(new PopulationData(zipData, populationData));
			}
		}
		return data;
	}


	/**
	 * Sets the indexes of zi_code and population columns retrieved from first input row 
	 * @param fields row to be searched 
	 */
	private void setInitialFields(String[] fields) {
		
		// ignore empty files 
		if(fields == null) return;
		int size = fields.length;
		for(int i = 0; i < size; i++) {
			
			// search for specific field name case-insensitive
			if(fields[i].equalsIgnoreCase("zip_code")) zipField = i;
			else if(fields[i].equalsIgnoreCase("population")) populationField = i;
		}
	}
}
