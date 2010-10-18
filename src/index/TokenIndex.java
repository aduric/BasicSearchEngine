package index;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import scanner.Token;

/**
 * 
 * 
 * The InvertedIndex class. Holds all the words that have been parsed from the documents in the scanner.
 * It extends the HashMap class and holds the words as a key-value pair: <String, Token>
 * where the LinkedList is the listing of the documentIDs in which the word appears
 * 
 * 
 * @author aduric
 *
 */
public class TokenIndex extends HashMap<String, Token> {
	
	private static final long serialVersionUID = 42L;

	
	/**
	 * 
	 * Simple InvertedIndex constructor. Basically creates a HashMap
	 *
	 */
	public TokenIndex() {
		super();
	}
	
	
	/**
	 * 
	 * TODO: Constructor to build an InvertedIndex from a CSV-type file specified in fromFile
	 * 
	 * @param fromFile
	 */
	public TokenIndex(String fromFile) {
		this();
	}
	
	/**
	 * 
	 * Inserts a word into the InvertedIndex
	 * 
	 */
	public Token put(String word, Token tok) {
		
		return super.put(word, tok);
		
	}
	
	/**
	 * 
	 * Gets the Token object that contains information about a certain word
	 * 
	 * @param word
	 * @return
	 */
	public Token getToken(Object word) {
		
		 return super.get(word); 
		
	}
	/**
	 * 
	 * Adds a word to the InvertedIndex based on the document ID. This method searches for the existance of a word and if it does not find it, inserts it.
	 * 
	 * @param word - The TOKEN that needs to be checked and inserted into the index.
	 * @param documentID
	 * @return
	 */
	public int add(Object word, int documentID) {
		Token term;
		//Is this token in the index?
		if(super.containsKey(((Token)word).getText().toLowerCase())) {
			term = this.getToken(((Token)word).getText().toLowerCase());
			//Token is in the index, check the documentID to see if we need to add another documentID or if we just need to increment the term frequency
			if(term.containsDoc(documentID))
				//Document ID found so we just increment the term frequency for that document
				term.incrementTermFreq(documentID);
			else {
				//Document ID is not found, therefore we need to add it
				term.addDoc(documentID);
				super.put(((Token)word).getText().toLowerCase(), term);
			}
		} else {
			//word is not in the InvertedIndex, so add it, along with the document number
			term = (Token)word;
			term.addDoc(documentID);
			super.put(((Token)word).getText().toLowerCase(), term);
		}
		
		return 0;
	}
	
	
	public long getTotalEntries() {
		
		long totEntries = 0;
		
		for(Map.Entry me: this.entrySet()) {
			totEntries += ((Token)me.getValue()).getNumDocs();
		}
		
		return totEntries;
	}
	
	public String toString() {
		String out = "";
		
		for(Map.Entry me: super.entrySet())
			out = out + String.format("%s(%s) - %s\n", ((Token)me.getValue()).getType(), ((Token)me.getValue()).getText(), ((Token)me.getValue()).getDocs());
		
		return out;
	}
	
	
	

}
