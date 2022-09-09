package edu.upenn.cit594.util;

/**
 * Stores values parsed from population file 
 * @author andrii podhornyi 
 *
 */
public class PopulationData {

	/**
	 * 5-digit zip code 
	 */
	private final int zipCode;
	
	/**
	 * Population number of this zip code
	 */
	private final int population;
	
	public PopulationData(int zipCode, int population) {
		this.zipCode = zipCode;
		this.population = population;
	}

	public int getZipCode() {
		return zipCode;
	}

	public int getPopulation() {
		return population;
	}
}
