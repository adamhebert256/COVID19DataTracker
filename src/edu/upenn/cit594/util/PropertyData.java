package edu.upenn.cit594.util;

/**
 * Represents values retireved from properties file
 * @author andrii podhornyi 
 *
 */
public class PropertyData {

	/**
	 *  5-digit zip code 
	 */
	private final int zipCode;
	
	/**
	 * Market value of property for the given zip Code.
	 * If value is missing in origin, then this variable is null 
	 */
	private final Double marketValue;
	
	/**
	 * Total livable area for given zip Code.
	 * If value is missing in origin, then this variable is null
	 */
	private final Double totalLivableArea;
	
	public PropertyData(int zipCode, Double marketValue, Double totalLivableArea) {
		this.zipCode = zipCode;
		this.marketValue = marketValue;
		this.totalLivableArea = totalLivableArea;
	}

	public int getZipCode() {
		return zipCode;
	}

	public Double getMarketValue() {
		return marketValue;
	}

	public Double getTotalLivableArea() {
		return totalLivableArea;
	}
	
	
}
