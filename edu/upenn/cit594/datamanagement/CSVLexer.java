package edu.upenn.cit594.datamanagement;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


/**
 * {@code CSVReader} provides a stateful API for streaming individual CSV rows
 * as arrays of strings that have been read from a given CSV file.
 *
 * @author andrii podhornyi 
 */
public class CSVLexer {
	private final Reader reader;

	
	/**
	 * Variable to store the number of fields in the table. Is updated only once
	 * after first row is processed 
	 */
	private int numOfFields = 4;
	
	/**
	 * Indicates if the first row was processed or not. If true, then 
	 * {@code checkNumOfFields} will always return true.
	 */
	private boolean isFirstRow = true;

	/**
	 * Indicates the current state of the reader.
	 * BOF and EOF indicate the current state of the file.
	 * CR, LF and NEW_FIELD represent retrieved '\r', '\n' or ',' respectively.
	 * INSIDE_DQOTE and OUTSIDE_DQUOTE are both the states of '"' retrieved from the file and indicate
	 * if field is either currently enclosed in DQUOTE or not.
	 * TEXT is any ASCII char aside from '\n \r , "'.
	 * @author andrii
	 *
	 */
	private enum State {
		INSIDE_DQUOTE, OUTSIDE_DQUOTE, CR, LF, EOF, BOF, TEXT, NEW_FIELD 
	}

	private State state = State.BOF;

	public CSVLexer(Reader reader) {
		this.reader = reader;
	}

	/**
	 * This method uses the class's {@code CharacterReader} to read in just enough
	 * characters to process a single valid CSV row, represented as an array of
	 * strings where each element of the array is a field of the row. If formatting
	 * errors are encountered during reading, this method throws a
	 * {@code CSVFormatException} that specifies the exact point at which the error
	 * occurred.
	 *
	 * @return a single row of CSV represented as a string array, where each
	 *         element of the array is a field of the row; or {@code null} when
	 *         there are no more rows left to be read.
	 * @throws IOException when the underlying reader encountered an error
	 * @throws CSVFormatException when the CSV file is formatted incorrectly
	 */
	public String[] readRow() throws IOException {

		if(state == State.EOF) return null;
		
		// represents current row and stores retrieved fields 
		ArrayList<String> fields = new ArrayList<>(numOfFields);
		
		// retrieves fields from the file and adds them to the row.
		processRow(fields);
		
		// sets the number of required fields in each row after first row is processed 
		int size = fields.size();
		if(isFirstRow) {
			numOfFields = size;
			isFirstRow = false;
		}
		
		// returns null if the last line of the file is single EOF
		if(state == State.EOF && size == 0) return null;
		return fields.stream().toArray(String[] :: new);
	}


	/**
	 * Reads current line of the file until EOF or LF is reached, or Exception is thrown.
	 * Adds new fields to the current row. If false is returned, then grammar error occurred. 
	 * @param fields Linked list that stores already added fields. Represents a CSV row.
	 * @return true if no invalid input in the current row. False otherwise.
	 * @throws IOException may be triggered by internal reader.
	 */
	private boolean processRow(ArrayList<String> fields) throws IOException {

		int c = 0;
		boolean status = false;
		StringBuilder sb = new StringBuilder();
		while(true) {
			c = reader.read();
			switch(c) {
			case -1:
				status = checkForEOF(fields, sb);
				if(status) return true;
				break;
			case ',':
				status = processComma(fields, sb);
				if(state != State.INSIDE_DQUOTE) sb = new StringBuilder();
				break;
			case 13:
				status = processCR(fields, sb);
				break;
			case 10:
				status = processLF(fields, sb);
				if(status && state == State.LF) return true;
				break;
			case '"':
				status = processDqoute(sb);
				break;
			default:
				status = processText(c, sb);
			}
			if(!status) {
				return false;
			}
		}
	}


	/**
	 * Appends char to the current field if not preceded with CR or OUTSIDE_DQUOTE. 
	 * @param c current char to be added to the field.
	 * @param sb instance of {@code StringBuilder} class that hold the data of the current field.
	 * @return false if preceded by CR or OUTSIDE_DQUOTE. True otherwise.
	 */
	private boolean processText(int c, StringBuilder sb) {

		switch (state) {
		case CR:
		case OUTSIDE_DQUOTE:
			return false;
		case INSIDE_DQUOTE:
			sb.append((char)c);
			break;
		default:
			sb.append((char)c);
			state = State.TEXT;
		}
		return true;
	}


	/**
	 * If appears at the beginning of the new field, then considered enclosing Quote and changes State
	 * to the INSIDE_DQOUTE. If appears twice consecutively with State = INSIDE_DQUOTE, then new '"' is added to the field.
	 * Returns false if not the first char in the field.
	 * @param sb instance of {@code StringBuilder} class that hold the data of the current field.
	 * @return false only if preceded by CR or TEXT (Not inside DQUOTE). True otherwise 
	 */
	private boolean processDqoute(StringBuilder sb) {

		switch (state) {
		case NEW_FIELD:
		case LF:
		case BOF:
			state = State.INSIDE_DQUOTE;
			break;
		case INSIDE_DQUOTE:
			state = State.OUTSIDE_DQUOTE;
			break; 
		case OUTSIDE_DQUOTE:
			state = State.INSIDE_DQUOTE;
			sb.append('"');
			break;
		default: return false;

		}
		return true;
	}


	/**
	 * Adds \n to the field if it is enclosed in DQUOTE. Otherwise checks the number of fields
	 * currently present in the row. New field is only appended if LF not preceded by CR and enough fields are already
	 * present in the current row. If preceded by CR, then simply updates the State to LF and returns. 
	 * @param fields Linked list that stores already added fields. Represents a CSV row.
	 * @param sb instance of {@code StringBuilder} class that hold the data of the current field.
	 * @return false only if the number of fields including current is less then in the first row of the file. True otherwise.
	 */
	private boolean processLF(List<String> fields, StringBuilder sb) {

		switch (state) {
		case INSIDE_DQUOTE:
			sb.append('\n');
			break;
		default:
			fields.add(sb.toString());
		case CR:
			state = State.LF;
			break;
		}
		return true;
	}


	/**
	 * Adds \r to the field if it is enclosed in DQUOTE. Otherwise checks the number of fields in the current row.
	 * In case of success adds new field and changes the state to CR.
	 * @param fields Linked list that stores already added fields. Represents a CSV row.
	 * @param sb instance of {@code StringBuilder} class that hold the data of the current field.
	 * @return false only if preceded by another CR or not enough fields are in the current row. True otherwise.
	 */
	private boolean processCR(List<String> fields, StringBuilder sb) {

		switch (state) {
		case INSIDE_DQUOTE:
			sb.append('\r');
			break;
		case CR:
			return false;
		default:
			state = State.CR;
			fields.add(sb.toString());
			break;
		}
		return true;
	}


	/**
	 * adds ',' to the current field if enclosed in the DQUOTE. Otherwise checks the number of fields
	 * present in the current row. In case of success adds a new field to the row and changes State to NEW_FIELD.
 	 * @param fields Linked list that stores already added fields. Represents a CSV row.
	 * @param sb instance of {@code StringBuilder} class that hold the data of the current field.
	 * @return false only if preceded by CR or wrong number of fields is already present in a row. True otherwise.
	 */
	private boolean processComma(List<String> fields, StringBuilder sb) {

		switch(state) {
		case INSIDE_DQUOTE:
			sb.append(',');
			break;
		case CR: return false;
		default:
			fields.add(sb.toString());
			state = State.NEW_FIELD;
			break;
		}
		return true;
	}


	/**
	 * Changes internal State to EOF. Adds field to the row only if not preceded by LF.
	 * Returns false if preceded by CR / BOF.
 	 * @param fields Linked list that stores already added fields. Represents a CSV row.
	 * @param sb instance of {@code StringBuilder} class that hold the data of the current field.
	 * @return false if preceded with CR/BOF or occurred inside DQUOTE. True otherwise. 
	 */
	private boolean checkForEOF(List<String> fields, StringBuilder sb) {

		switch(state) {
		case NEW_FIELD:
		case TEXT:
		case OUTSIDE_DQUOTE:
			fields.add(sb.toString());
		case LF:
			state = State.EOF;
			break;
		default:
			state = State.EOF;
			return false;
		}
		return true;
	}

}
