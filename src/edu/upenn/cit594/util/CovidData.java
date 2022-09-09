package edu.upenn.cit594.util;

/**
 * Storage class for Covid data
 * @author andrii podhornyi 
 *
 */
public class CovidData {

	/**
	 * represents the zip code
	 */
	private final int zipCode;
	
	/**
	 * parsed date in YYYY-MM-DD format 
	 */
	private final String date;
	
	/**
	 * Number of partially vaccinated persons for this zip code 
	 */
	private final int partiallyVaccinated;
	
	/**
	 * Number of fully vaccinated persons for this zip code
	 */
	private final int fullyVaccinated;
	
	public CovidData(int zipCode, String date, int partiallyVaccinated, int fullyVaccinated) {
		this.zipCode = zipCode;
		this.date = date;
		this.partiallyVaccinated = partiallyVaccinated;
		this.fullyVaccinated = fullyVaccinated;
	}
	
	public String getDate() {
		return date;
	}

	public int getPartiallyVaccinated() {
		return partiallyVaccinated;
	}

	public int getFullyVaccinated() {
		return fullyVaccinated;
	}

	
	public int getZipCode() {
		return zipCode;
	}
}
