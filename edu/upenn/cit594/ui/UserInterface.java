package edu.upenn.cit594.ui;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.processor.Processor;

/**
 * user interface tier 
 * @author adam hebert 
 *
 */
public class UserInterface {
	
	private Logger logger = Logger.getLoggerInstance();
	private Processor processor;
	
	public UserInterface(Processor processor) {
		this.processor  = processor;
	}
	
	public void start() throws IOException {
		Scanner myScanner = new Scanner(System.in);
		String userInput = " ";
		while(!userInput.equals("0")) {	
			selectionMenu();
			userInput = myScanner.nextLine();
			logger.log(userInput);
	
			String dateInput;
			int zipcodeInput;
			switch(userInput) {
				case "0":
					continue;
				case "1":
					TreeSet<String> availableDataSets = processor.getAvailableDataSets();
					System.out.println("\nBEGIN OUTPUT");
					for(String dataSet: availableDataSets) {
						System.out.println(dataSet);
					}
					System.out.println("END OUTPUT\n");
					break;
				case "2":
					if(processor.isTotalPopulationAvailable()) {
						System.out.println("\nBEGIN OUTPUT");
						System.out.println(processor.getTotalPopulationForAllZipCodes());
						System.out.println("END OUTPUT\n");
					}
					else System.out.println("Population data set was not provided\n");
					break;
				case "3":
					if(processor.areVaccinationsAvailable()) {
						System.out.println("Type the word partial to get data on partial vaccinations, or type the word full to get data on full vaccinations.");
						boolean partial = getPartialOrFullInput(myScanner);
						System.out.println("Type the date you would like to get data on in the exact format: YYYY-MM-DD");
						dateInput =  getDateInput(myScanner);
						TreeMap<Integer, Double> partialOrFullVaccinationsPerCapita = processor.getPartialOrFullVaccinationsPerCapita(partial, dateInput);
						System.out.println("\nBEGIN OUTPUT");
						displayPartialOrFullVaccinationsPerCapita(partialOrFullVaccinationsPerCapita);
						System.out.println("END OUTPUT\n");
					}
					else System.out.println("Some data sets are missing\n");
					break;
				case "4":
					if(processor.isAverageMarketAvailable()) {
						System.out.println("Enter the 5 digit ZIP code you would like to get the average market value for properties in");
						zipcodeInput = getZipcode(myScanner);
						System.out.println("\nBEGIN OUTPUT");
						System.out.println(processor.getAverageMarketValue(zipcodeInput));
						System.out.println("END OUTPUT\n");
					}
					else System.out.println("Property data is not available\n");
					break;
				case "5":
					if(processor.isAverageLivableAreaAvailable()) {
						System.out.println("Enter the 5 digit ZIP code you would like to get the average total livable area for properties in");
						zipcodeInput = getZipcode(myScanner);
						
						System.out.println("\nBEGIN OUTPUT");
						System.out.println(processor.getAverageTotalLivableArea(zipcodeInput));
						System.out.println("END OUTPUT\n");
					}
					else System.out.println("Property data is not available\n"); 
					break;	
				case "6":
					if(processor.isTotalMarketAvailable()) {
						System.out.println("Enter the 5 digit ZIP code you would like to get total market vaue of properties, per capita, in");
						zipcodeInput = getZipcode(myScanner);
						
						System.out.println("\nBEGIN OUTPUT");
						System.out.println(processor.getMarketValuePerCapita(zipcodeInput));
						System.out.println("END OUTPUT\n");
					}
					else System.out.println("Some data sets are missing\n");
					break;
				case "7":
					if(processor.isLSpacePPersonAvailable()) {
						System.out.println("Enter the 5 digit ZIP code you would like to get data for");
						zipcodeInput = getZipcode(myScanner);
						System.out.println("Type the date you would like to get data on in the exact format: YYYY-MM-DD");
						dateInput =  getDateInput(myScanner);	
						
						System.out.println("\nBEGIN OUTPUT");
						System.out.println(processor.getLivableSpacePerUnvaccinatedPerson(zipcodeInput, dateInput));
						System.out.println("END OUTPUT\n");
					}
					else System.out.println("Some data sets are missing\n");
					break;
				default:
					System.out.println("Error, you did not enter a number from 0-7.\n");
					continue;
			}
		}
		myScanner.close();
	}
	
	private void displayPartialOrFullVaccinationsPerCapita(TreeMap<Integer, Double> partialOrFullVaccinationsPerCapita) {
		if(partialOrFullVaccinationsPerCapita == null) {
			System.out.println("Could not perform operation due to incomplete dataset");
		}
		else {
			if (partialOrFullVaccinationsPerCapita.entrySet().size() == 0) {
				System.out.println(0);
			}
			else {
				for(Entry<Integer, Double> entry: partialOrFullVaccinationsPerCapita.entrySet()) {
					System.out.print(entry.getKey() + " ");
					System.out.printf("%.4f", entry.getValue());
					System.out.println();		
				}
			}
		}
	}
	
	private String getDateInput(Scanner myScanner) throws IOException {
		System.out.print("> ");
		String dateInput = myScanner.nextLine();
		logger.log(dateInput);
		Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
		Matcher dateMatcher = datePattern.matcher(dateInput);
		boolean dateMatchCheck = dateMatcher.find();
		while(!dateMatchCheck) {
			System.out.println("Please enter a valid date in the format YYYY-MM-DD.");
			System.out.print("> ");
			dateInput = myScanner.nextLine();
			logger.log(dateInput);
			dateMatcher = datePattern.matcher(dateInput);
			dateMatchCheck = dateMatcher.find();
		}
		return dateInput;
	}
	
	private boolean getPartialOrFullInput(Scanner myScanner) throws IOException {
		System.out.print("> ");
		String userInput = myScanner.nextLine();
		boolean partial;
		logger.log(userInput);
		while(!userInput.equals("partial") && !userInput.equals("full")){
			System.out.println("You did not enter the word partial or full. Please try again.");
			System.out.println("Type the word partial to get data on partial vaccinations, or type the word full to get data on full vaccinations.");
			System.out.print("> ");
			userInput = myScanner.nextLine();
			logger.log(userInput);
		}
		if(userInput.equals("partial")) {
			partial = true;
		}
		else {
			partial = false;
		}
		return partial;
	}
	
	private int getZipcode(Scanner myScanner) throws IOException {
		System.out.print("> ");
		String zipcodeInput = myScanner.nextLine();
		logger.log(zipcodeInput);
		Pattern zipCodePattern = Pattern.compile("^[0-9]+$");
		Matcher zipCodeMatcher = zipCodePattern.matcher(zipcodeInput);
		boolean zipCodeMatchCheck = zipCodeMatcher.find();
		while(!zipCodeMatchCheck || zipcodeInput.length() != 5) {
			System.out.println("Please enter a valid 5 digit zipcode.\n>");
			zipcodeInput = myScanner.nextLine();
			logger.log(zipcodeInput);
			zipCodeMatcher = zipCodePattern.matcher(zipcodeInput);
			zipCodeMatchCheck = zipCodeMatcher.find();
		}
		
		int zipcodeIntInput = Integer.parseInt(zipcodeInput);
		return zipcodeIntInput;
	}
	
	private void selectionMenu(){
		System.out.println("Please select an action by typing an action number (from the list below) and hitting return");
		System.out.println("0. Exit the program.");
		System.out.println("1. Show the avaialable data sets.");
		System.out.println("2. Show the total population for all ZIP Codes");
		System.out.println("3. Show the total vaccinations per capita for each ZIP Code for the speicifed date.");
		System.out.println("4. Show the average market value for properties in a specified ZIP Code.");
		System.out.println("5. Show the average total livable area for properties in a specified ZIP Code.");
		System.out.println("6. Show the total market vaue of properties, per capita, for a specified ZIP Code.");
		System.out.println("7. Show the results of your custom feature (the amount of livable space in a given zip code per completely unvaccinated person on a given date).");
		System.out.print("> ");
		System.out.flush();
	}

	public static void displayError(String string) {
		System.out.println(string);
	}
	
}