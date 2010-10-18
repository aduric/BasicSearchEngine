package scanner;

import java.util.TreeMap;
import java.util.Map;

public class Token {

	private String tokenText;
	private String tokenType;
	
	//This HashMap represents the documents that the token is in, and the number of times this token is in a document
	//HashMap<unique document ID, term frequency in that document>
	private TreeMap<Integer, Integer> tokenDocs;
	
	
	public Token(String text, String type) {
		this.tokenText = text;
		this.tokenType = type;
		this.tokenDocs = new TreeMap<Integer, Integer>();
	}
	
	public Token(String text, String type, TreeMap<Integer, Integer> myDocs) {
		this.tokenText = text;
		this.tokenType = type;
		this.tokenDocs = myDocs;
	}

	public String getText() { return this.tokenText; }
	public String getType() { return this.tokenType; }
	public TreeMap<Integer, Integer> getDocs() { return this.tokenDocs; }
	
	/**
	 * 
	 * Does this token contain the document ID?
	 * 
	 * @param docID
	 * @return
	 */
	public boolean containsDoc(int docID) {
		return tokenDocs.containsKey(docID);
	}
	
	/**
	 * 
	 * Add the document ID to the tokenDocs HashMap and sets the initial term frequency to 1
	 * 
	 * @param docID
	 */
	public void addDoc(int docID) {
		this.tokenDocs.put(docID, 1);
	}
	
	/**
	 * 
	 * Get the number of documents for this token
	 * 
	 * @param key
	 * @return
	 */
	public int getNumDocs() {
		
		return this.tokenDocs.size();
	}
	
	public int getTotalFreq() {
		
		int totalFreq = 0;
		
		for(Map.Entry me: this.getDocs().entrySet()) {
			totalFreq += ((Integer)me.getValue()).intValue();
		}
		
		return totalFreq;
		
	}
	
	/**
	 * 
	 * Increments the term frequency for the docID doc
	 * 
	 * @param docID
	 */
	public void incrementTermFreq(int docID) {
		int tf = this.tokenDocs.get(docID);
		tf++;
		this.tokenDocs.put(docID, tf);
	}
	
	public String toString() {
		return String.format("%s (%s)", this.getText(), this.getType());
	}


}