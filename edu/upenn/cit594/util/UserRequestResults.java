package edu.upenn.cit594.util;

import java.util.TreeMap;

import edu.upenn.cit594.datamanagement.ResultHolder;

/**
 * Util class for ResultHolder classes storage
 * Implements memoization techniques 
 * @author andrii podhornyi 
 *
 */
public class UserRequestResults {
	
	
	/**
	 * Storage for the user's requests of partial and full vaccinations per capita 
	 * @author Adam Hebert 
	 */
	public ResultHolder<String, TreeMap<Integer, Double>> getPartialOrFullVaccinationsPerCapita = new ResultHolder<>();
	
	/**
	 * Storage for the user's requests of average market value 
	 */
	public ResultHolder<Integer, Long> getAverageMarketValue = new ResultHolder<>();
	
	/**
	 * Storage for the user's requests of average total livable area  
	 */
	public ResultHolder<Integer, Long> getAverageLivableArea = new ResultHolder<>();
	
	/**
	 * Storage for the user's requests of total market value per capita 
	 */
	public ResultHolder<Integer, Long> getTotalMarketValue = new ResultHolder<>();
	
	/**
	 * Storage for free activity task
	 */
	public ResultHolder<String, Long> getlivableSpacePerUnvaccinatedPerson = new ResultHolder<>();
}