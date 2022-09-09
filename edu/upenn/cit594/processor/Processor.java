package edu.upenn.cit594.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.upenn.cit594.datamanagement.ArgumentStorage;
import edu.upenn.cit594.datamanagement.CovidParser;
import edu.upenn.cit594.datamanagement.PopulationParser;
import edu.upenn.cit594.datamanagement.PropertyParser;
import edu.upenn.cit594.datamanagement.ResultHolder;
import edu.upenn.cit594.util.CovidData;
import edu.upenn.cit594.util.PopulationData;
import edu.upenn.cit594.util.PropertyData;
import edu.upenn.cit594.util.UserRequestResults;

/**
 * Logic tier 
 * @author andrii podhornyi
 * @author adam hebert
 *
 */
public class Processor {

	/**
	 * References to data holders 
	 */
	private List<CovidData> covidData;
	private List<PropertyData> propertyData;
	private List<PopulationData> populationData;
	private Set<String> arguments;

	/**
	 * Util class to hold memoization results 
	 */
	private UserRequestResults requestResults = new UserRequestResults();

	/**
	 * Result holder for totalPopulationForAllZipCodes
	 */
	private Long populationResult;

	/**
	 * Intermediate data used to search for values in populations
	 */
	private Map<Integer, Integer> zipCodePopulations;

	
	public Processor(CovidParser covidParser, PopulationParser populationParser,
			PropertyParser propertyParser, ArgumentStorage arguments) throws Exception {
		if(covidParser != null) covidData = covidParser.getCovidData();
		if(populationParser != null) populationData = populationParser.getPopulationData();
		if(propertyParser != null) propertyData = propertyParser.getPropertyData();
		this.arguments = arguments.getArguments();
	}

	
	/**
	 * Returns an TreeSet containing the names of the runtime arguments that were given
	 * @author Adam Hebert
	 * @param none
	 * @return a TreeSet containing the names of the runtime arguments that were given
	 */
	public TreeSet<String> getAvailableDataSets(){
		TreeSet<String> availableDataSets = new TreeSet<String>();
		for(String argument : arguments) {
			if(!argument.equals("log")) {
				availableDataSets.add(argument);
			}
		}
		return availableDataSets;
	}

	/**
	 * Gets the total population across all zipcodes in the data set, utilizing memoization
	 * @author Adam Hebert
	 * @param none
	 * @return the total population across all zipcodes in the data set
	 */
	public Long getTotalPopulationForAllZipCodes() {
		if(populationData == null) return null;
		if (populationResult!=null) {
			return populationResult;
		}
		else {
			long result = calculateTotalPopulationForAllCodes();
			populationResult = result;
			return result;
		}
	}

	/**
	 * Calculates the total population across all zipcodes in the data set
	 * @author Adam Hebert
	 * @param none
	 * @return the total population across all zipcodes in the data set
	 */
	private long calculateTotalPopulationForAllCodes() {
		long totalPopulation = 0;
		for(PopulationData zipCodePopulationData : populationData) {
			totalPopulation += zipCodePopulationData.getPopulation();
		}
		return totalPopulation;
	}

	
	/**
	 * Gets the number of partial or full vaccinations per capita for each zip code on a given date, utilizing memoization
	 * @author Adam Hebert
	 * @param boolean indicating partial of full data is wanted, and a date indicating the date we want data for
	 * @return the number of partial or full vaccinations per capita for each zip code on a given date
	 */
	public TreeMap<Integer, Double> getPartialOrFullVaccinationsPerCapita(boolean partial, String inputDate) {
		if(covidData == null || populationData == null) return null;
		String input = partial + inputDate;
		if (requestResults.getPartialOrFullVaccinationsPerCapita.contains(input)){
			return requestResults.getPartialOrFullVaccinationsPerCapita.get(input);
		}

		if (zipCodePopulations == null) {
			zipCodePopulations  = new HashMap<>();
			for(PopulationData data: populationData) {
				if(data.getPopulation() == 0) continue;
				zipCodePopulations.put(data.getZipCode(), data.getPopulation());
			}
		}

		TreeMap<Integer, Double> partialOrFullVaccinationsPerCapita = new TreeMap<>();

		if(partial) {
			for (CovidData data : covidData) {
				int currPartiallyVaccinated = data.getPartiallyVaccinated();
				int currZipCode = data.getZipCode();
				String currDate = data.getDate();
				if(currDate.equals(inputDate) && currPartiallyVaccinated != 0 && zipCodePopulations.containsKey(currZipCode)) {
					double currPartialVaccinationsPerCapita = (double)currPartiallyVaccinated/zipCodePopulations.get(currZipCode);
					partialOrFullVaccinationsPerCapita.put(currZipCode, currPartialVaccinationsPerCapita);
				}
			}
		}
		else {
			for (CovidData data : covidData) {
				int currFullyVaccinated = data.getFullyVaccinated();
				int currZipCode = data.getZipCode();
				String currDate = data.getDate();
				if(currDate.equals(inputDate) && currFullyVaccinated != 0 && zipCodePopulations.containsKey(currZipCode)) {
					double currFullVaccinationsPerCapita = (double)currFullyVaccinated/zipCodePopulations.get(currZipCode);
					partialOrFullVaccinationsPerCapita.put(currZipCode, currFullVaccinationsPerCapita);
				}
			}
		}
		requestResults.getPartialOrFullVaccinationsPerCapita.put(input, partialOrFullVaccinationsPerCapita);
		return partialOrFullVaccinationsPerCapita;
	}


	/**
	 * Returns total market value divided by the number of valid fieds 
	 * @param zipCode 5-digit int to search for 
	 * @return null if data file is not provided, valid long otherwise 
	 * @author andrii podhornyi 
	 */
	public Long getAverageMarketValue(int zipCode) {
		return getAverage(zipCode, new MarketValueSelector(), requestResults.getAverageMarketValue);
	}

	/**
	 * Returns total livable area divided for current zip code divided by number of properties 
	 * for same zip code 
	 * @param zipCode int representing 5-digit zip code 
	 * @return null if data is not provided, valid long otherwise 
	 * @author andrii podhornyi 
	 */
	public Long getAverageTotalLivableArea(int zipCode) {
		return getAverage(zipCode, new LivableAreaSelector(), requestResults.getAverageLivableArea);
	}

	/**
	 * Returns the average of items as the sum of values of all items divided by number of them 
	 * @param zipCode 5-digit int to search for 
	 * @param selector instance of selector. Returns either partially or fully vaccinated field
	 * @param requestResults memoization technique implementation holding past outputs 
	 * @return null if data file is not available, valid long otherwise 
	 * @author andrii podhornyi 
	 */
	private Long getAverage(int zipCode, Selector selector, ResultHolder<Integer, Long> requestResults) {

		// no valid output is possible without available data 
		if(propertyData == null) return null;

		// try to search for existing output for current input 
		if(requestResults.contains(zipCode)) return requestResults.get(zipCode);
		int propertyCount = 0;
		double total = 0;
		for(PropertyData data : propertyData) {

			// add only values that match given zip code 
			if(data.getZipCode() == zipCode) {
				
				// ignore invalid data
				Double value = selector.getData(data);
				if(value == null) continue;
				propertyCount++;
				total += value;
			}

		}
		Long result = propertyCount == 0 ? 0 : (long) (total / propertyCount);
		requestResults.put(zipCode, result);
		return result;
	}


	/**
	 * Returns total market value for given zip code divided by total population for that zip code
	 * @param zipCode 5-digit int to search for 
	 * @return null if required data from files is missing, valid long otherwise 
	 * @author andrii podhornyi 
	 */
	public Long getMarketValuePerCapita(int zipCode) {

		// immediately return if data is not available 
		if(propertyData == null || populationData == null) return null;

		// try to get output from previous calls 
		if(requestResults.getTotalMarketValue.contains(zipCode)) {
			return requestResults.getTotalMarketValue.get(zipCode);
		}
		long totalPopulation = 0;

		// get total population for given zip code 
		for(PopulationData data : populationData) {
			if(zipCode == data.getZipCode()) totalPopulation += data.getPopulation();
		}

		// return immediately if population records are not available 
		if(totalPopulation == 0) {
			requestResults.getTotalMarketValue.put(zipCode, 0l);
			return 0l;
		};
		double totalMarketValue = 0;

		// get total market value for given zip code 
		for(PropertyData data : propertyData) {
			if(zipCode == data.getZipCode() && data.getMarketValue() != null)
				totalMarketValue += data.getMarketValue();
		}
		Long result = (long) (totalMarketValue / totalPopulation);
		requestResults.getTotalMarketValue.put(zipCode, result);
		return result;
	}

	/**
	 * Gets the amount of livable space in a given zip code per completely unvaccinated person on a given date
	 * @author Adam Hebert
	 * @param the zipcode date is wanted for, as well as the date the data is wanted for 
	 * @return the amount of livable space in a given zip code per completely unvaccinated person on a given date
	 */
	public Long getLivableSpacePerUnvaccinatedPerson(int zipCode, String inputDate) {
		if(covidData == null || populationData == null || propertyData == null) return null;
		String input = zipCode + inputDate;
		if (requestResults.getlivableSpacePerUnvaccinatedPerson.contains(input)){
			return requestResults.getlivableSpacePerUnvaccinatedPerson.get(input);
		}
		//find the total population in the zip code
		long zipCodePopulation = 0;
		for(PopulationData zipCodePopulationData : populationData) {
			if (zipCodePopulationData.getZipCode() == zipCode) {
				zipCodePopulation += zipCodePopulationData.getPopulation();
				break;
			}
		}

		//Find the average livable area per property in zip code
		long averageTotalLivableAreaInZipcode = getAverageTotalLivableArea(zipCode);
		//Find number of properties in zipcode
		Selector selector = new LivableAreaSelector();
		long numberOfPropertiesInZipCode = 0;
		for(PropertyData data : propertyData) {
			if(data.getZipCode() == zipCode) {
				Double value = selector.getData(data);
				if(value == null) continue;
				numberOfPropertiesInZipCode++;
			}
		}
		//find total livable area in zip code
		long totalLivableAreaInZipcode = numberOfPropertiesInZipCode*averageTotalLivableAreaInZipcode;

		//find number of partially vaccinated and number of fully vaccinated people
		TreeMap<Integer, Double> partiallyVaccinatedPeople = getPartialOrFullVaccinationsPerCapita(true, inputDate);
		TreeMap<Integer, Double> fullyVaccinatedPeople = getPartialOrFullVaccinationsPerCapita(false, inputDate);
		Double partiallyVaccinatedPeopleInZipcode = partiallyVaccinatedPeople.get(zipCode);
		Double fullyVaccinatedPeopleInZipcode = fullyVaccinatedPeople.get(zipCode);


		if(partiallyVaccinatedPeopleInZipcode != null && fullyVaccinatedPeopleInZipcode != null && fullyVaccinatedPeopleInZipcode + partiallyVaccinatedPeopleInZipcode > 0) {	
			//find number of unvaccinated people in zip code
			long unvaccinatedPeopleInZipcode = Math.round((zipCodePopulation * (1 - partiallyVaccinatedPeopleInZipcode - fullyVaccinatedPeopleInZipcode)));
			//calculate and return amount of livable space per unvaccinated person in zip code
			long livableSpacePerUnvaccinatedPersonInZipCode =  Math.round(totalLivableAreaInZipcode/unvaccinatedPeopleInZipcode);
			requestResults.getlivableSpacePerUnvaccinatedPerson.put(input, livableSpacePerUnvaccinatedPersonInZipCode);
			return livableSpacePerUnvaccinatedPersonInZipCode;
		}
		else {
			requestResults.getlivableSpacePerUnvaccinatedPerson.put(input, 0l);
			return 0l;
		}
	}
	
	/*
	 * -----------Availability booleans for ui--------------------
	 */
	public boolean isTotalPopulationAvailable() {
		return populationData != null;
	}
	
	public boolean areVaccinationsAvailable() {
		return covidData != null && populationData != null;
	}
	
	public boolean isAverageMarketAvailable() {
		return propertyData != null;
	}
	
	public boolean isAverageLivableAreaAvailable() {
		return propertyData != null;
	}
	
	public boolean isTotalMarketAvailable() {
		return propertyData != null && populationData != null;
	}
	
	public boolean isLSpacePPersonAvailable() {
		return propertyData != null && populationData != null && covidData != null;
	}

} 
