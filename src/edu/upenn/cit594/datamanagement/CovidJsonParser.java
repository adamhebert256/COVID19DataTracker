package edu.upenn.cit594.datamanagement;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.upenn.cit594.util.CovidData;

/**
 * Parser for Covid files with JSON extension 
 * @author andrii podhornyi 
 *
 */
public class CovidJsonParser extends CovidParser {

	public CovidJsonParser(String filename) {
		super(filename);
	}

	@Override
	public List<CovidData> getCovidData() throws IOException, ParseException {
		
		logger.log(filename);
		
		LinkedList<CovidData> data = new LinkedList<>();
		JSONParser parser = new JSONParser();
		
		try(FileReader fr = new FileReader(filename)){
			
			// parse file to JSONarray and iterate over it 
			for(Object obj : (JSONArray) parser.parse(fr)) {
				
				// Each object should be casted to JSONObject prior to use 
				JSONObject values = (JSONObject) obj;
				Long zip;
				Integer partiallyVac, fullyVac;
				
				// If value is missing or not 5 chars long, then ignore entire object 
				try {
					zip = (Long) values.get("zip_code");
					if(zip == null) continue;
				} catch (Exception e) {continue;} 
				
				if(!zipMatcher.reset(zip.toString()).find()) continue;
				
				// If timestamp does not follow defined pattern, ignore entire field 
				String date;
				try {
					date = (String) values.get("etl_timestamp");
					if(date == null) continue;
				} catch (Exception e) {continue;}
				
				if(!dateMatcher.reset(date).find()) continue;
				
				// these values are defaulted to 0 on cast failure 
				partiallyVac = castInt(values.get("partially_vaccinated"));
				fullyVac = castInt(values.get("fully_vaccinated"));
				data.add(new CovidData(zip.intValue(), date.split(" ")[0], partiallyVac, fullyVac));
			} 
		}
		
		return data;
	}
	
	/**
	 * Casts provided Object to int.
	 * @param value Object to cast 
	 * @return casted value on success, 0 on failure 
	 */
	private int castInt(Object value) {
		if(value == null) return 0;
		try {
			Long castedValue = (Long) value;
			return castedValue.intValue();
		} catch (Exception e) {
			return 0;
		}
	}
}
