package edu.upenn.cit594.datamanagement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import edu.upenn.cit594.util.CovidData;


/**
 * Reads Covid file into List of CovidData objects 
 * @author andrii podhornyi 
 *
 */
public class CovidCSVParser extends CovidParser {

	/**
	 * Required fields indicators 
	 */
	private int dateField, zipField, partialField, fullField;

	public CovidCSVParser(String filename) {
		super(filename);
	}

	/**
	 * Parses provided file line by line. Creates {@code CovidData} object for each valid row.
	 * Rows are considered valid if follow zip code ^\\d{5}$ pattern and YYYY-MM-DD hh:mm:ss timestamp pattern 
	 * All other fields default to 0 on cast failure 
	 * @return List of valid CovidData objects  
	 * @throws IOException may be thrown by internal reader
	 */
	@Override
	public List<CovidData> getCovidData() throws IOException {
		
		logger.log(filename);
		
		LinkedList<CovidData> data = new LinkedList<>();
		try(FileReader fr = new FileReader(filename); BufferedReader br = new BufferedReader(fr)){
			
			// CSVLexer returns formatted rows line by line 
			CSVLexer reader = new CSVLexer(br);
			
			// Since first row is header, then indexes of required fields should be extracted 
			setFieldsIndexes(reader.readRow());
			String[] fields;
			
			// Lexer returns null when EOF reached 
			while((fields = reader.readRow()) != null) {
				int zipCode = 0, partiallyVac = 0, fullyVac = 0;
				
				// matcher is pre-compiled and therefore is reset with a new string every time 
				if(zipMatcher.reset(fields[zipField]).find())
					zipCode = Integer.parseInt(fields[zipField]);
				else continue;
				
				// lines with invalid timestamps are ignored 
				if(!dateMatcher.reset(fields[dateField]).find()) continue;
				
				// cats given fields to ints. Defaults to 0 on failure 
				partiallyVac = castInt(fields[partialField]); 
				fullyVac = castInt(fields[fullField]);
				data.add(new CovidData(zipCode, fields[dateField].split(" ")[0], partiallyVac, fullyVac));
			}
		}
		return data;
	}
	
	/**
	 * Helper function to cast given string to integer 
	 * @param value String to cast 
	 * @return casted int on success, 0 on failure 
	 */
	private int castInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}


	/**
	 * Initial set-up of indexes for required fields
	 * @param data String[] of field names 
	 */
	private void setFieldsIndexes(String[] data) {
		
		// ignore empty files 
		if(data == null) return;
		int size = data.length;
		for(int i = 0; i < size; i++) {
			
			// check if index matches one of required field's name case-insensitively  
			if(data[i].equalsIgnoreCase("zip_code")) zipField = i;
			else if(data[i].equalsIgnoreCase("etl_timestamp")) dateField = i;
			else if(data[i].equalsIgnoreCase("partially_vaccinated")) partialField = i;
			else if(data[i].equalsIgnoreCase("fully_vaccinated")) fullField = i;
		}
	}
}
