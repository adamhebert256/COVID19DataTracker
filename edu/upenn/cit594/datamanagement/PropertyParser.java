package edu.upenn.cit594.datamanagement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.util.PropertyData;

/**
 * Parses given properties file 
 * @author andrii podhornyi 
 *
 */
public class PropertyParser {

	/**
	 * Name of the file to be parsed 
	 */
	private String filename;
	
	/**
	 * Required fields indicators 
	 */
	private int zipField, marketField, areaField;

	
	public PropertyParser(String filename) {
		this.filename = filename;
	}

	/**
	 * Parses provided file line by line. Adds new {@code PropertyData} object to the list 
	 * of object only if zip_code field's first 5 chars are digits. If market and area fields are invalid,
	 * then null values are passed to the PropertyData constructor
	 * @return List of PropertyData objects 
	 * @throws IOException may be thrown by internal reader 
	 */
	public List<PropertyData> getPropertyData() throws IOException {
		
		Logger.getLoggerInstance().log(filename);
		
		// zip code pattern 
		Pattern pattern = Pattern.compile("^\\d{5}");
		Matcher matcher = pattern.matcher("");
		
		// holds parse results 
		LinkedList<PropertyData> data = new LinkedList<>();
		
		try(FileReader fr = new FileReader(filename); BufferedReader br = new BufferedReader(fr)){
			
			// lexer returns properly formatted rows 
			CSVLexer reader = new CSVLexer(br);
			
			// since first row is header, we need to initially set indexes of required fields 
			setFieldsIndexes(reader.readRow());
			String[] fields;
			
			// null is returned after EOF is reached 
			while((fields = reader.readRow()) != null) {
				int zipCode;
				
				// mather is pre-compiled and therefore just reset with new string every iteration 
				if(matcher.reset(fields[zipField]).find())
					zipCode = Integer.parseInt(fields[zipField].substring(0, 5));
				else continue;
				
				// try to cast data to doubles. Null is set on fail 
				Double marketValue = castDouble(fields[marketField]);
				Double livableArea = castDouble(fields[areaField]);
				data.add(new PropertyData(zipCode, marketValue, livableArea));
			}
		}
		return data;
	}
	
	/**
	 * Helper function to cast doubles 
	 * @param value String to be casted 
	 * @return casted value on success, null on failure 
	 */
	private Double castDouble(String value) {
		try {
			return Double.parseDouble(value);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}


	/**
	 * Sets indexes of columns needed for program 
	 * @param data row to be searched 
	 */
	private void setFieldsIndexes(String[] data) {
		
		// ignore empty files 
		if(data == null) return;
		int size = data.length;
		for(int i = 0; i < size; i++) {
			
			// search for specific field name case-insensitive 
			if(data[i].equalsIgnoreCase("zip_code")) zipField = i;
			else if(data[i].equalsIgnoreCase("market_value")) marketField = i;
			else if(data[i].equalsIgnoreCase("total_livable_area")) areaField = i;
		}
	}
}
