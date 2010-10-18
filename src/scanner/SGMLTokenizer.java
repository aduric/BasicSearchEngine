
package scanner;

import java.util.ArrayList;
import java.util.Stack;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * 
 * This class is a 'wrapper class' that interfaces the Lexer. It creates and calls the Lexer's functions.
 * It returns only the tokens that are specified and skips everything else.
 * 
 * TODO?
 * Addition. This will load up the stopwords file into an ArrayList and compare each token to the stopword list. If the token is a stopword
 * then we toss it out. This should significantly shrink the .OUT file that we produce.
 * 
 * @author aduric
 *
 */
public class SGMLTokenizer extends AbstractTokenizer {
	
	private Reader r;
	private Lexer lexer;
	
	private long dataRead;
	//private long totalData;

	private static String[] RELEVANT_TAGS = {"TEXT","DOC","DOCNO","HEADLINE","P","TOP","NUM","TITLE","DESC","NARR"};
	//private final String[] NON_VALID_TOKENS = {"SPACE","NEWLINE","QUOTE","QUOTES","DBLQUOT","NUM","NUMBER","DIGIT","PHONE","HYPHENS","INSENTP","LDOTS","FRAC","FRAC2"};
	private final String[] NON_VALID_TOKENS = {};
	private ArrayList<String> stopWords;
	
	private static Stack<String> tagStack = new Stack<String>();
	private static boolean relevantContent = true;
	private static String irrelevantTag = "";

	
	/**
	 * 
	 * @param f The file containing the text to be tokenized
	 * @throws IOException
	 */
	public SGMLTokenizer(Reader f) throws IOException {
		
		this.r = f;
		this.lexer = new Lexer(r);
		
		this.dataRead = 0;
		
		//Load up the stopwords if the file exists
		//this.stopWords = new ArrayList<String>();
		//this.loadStopWords("stopwords.txt");
	}

	public boolean hasNext() {
		return !lexer.hasNext();
	}

	public Object next() {
		try {
			Token t = (Token)lexer.next();
			/*
			 * Do some processing...
			 *  
			 */
			
			if(t != null && !contains(t.getType(), NON_VALID_TOKENS)) {
			  	// Check the markup for tag consistency through the use of a global stack  
			  	String poppedString = "";
			  	if(t.getType().contains("OPEN")) {
			  		tagStack.push(t.getType());
			  		if(!contains(t.getType().substring(t.getType().indexOf("-")+1), RELEVANT_TAGS)) {
			  			relevantContent = false;
			  			irrelevantTag = t.getType();
			  		}

			  	}
			  	else if(t.getType().contains("CLOSE")) {
			  		//Need to check if the top of the stack contains an OPEN-... If it does, we pop it.
			  		//System.out.println("TAG: "+tokenType.replace("CLOSE","OPEN"));
			  		if(tagStack.peek().equals(t.getType().replace("CLOSE","OPEN"))) {
			  			poppedString = tagStack.pop();
			  		}
			  	}
				
				//We can actually now return this token  
				if(relevantContent) {
					//return t;
					//DOCUMENT types
					if(t.getType().equals("OPEN-DOCNO")) 
						//DOCNO indicates new document, so we scan the next token which is the document ID and return it with its label
						return String.format("$DOC\n"); //((Token)lexer.next()).getText());
					
					else if(t.getType().equals("OPEN-HEADLINE")) 
						//HEADLINE indicates the title of the document, so we need to parse the title until we hit the CLOSE-HEADLINE
						return String.format("$TITLE\n");
					
					else if(t.getType().equals("OPEN-TEXT")) 
						return String.format("$BODY");
					
					else if(t.getType().equals("OPEN-DATE"))
						return String.format("$DATE");
					
					else if(t.getType().equals("OPEN-LENGTH"))
						return String.format("$LENGTH");
					
					//QUERY types
					else if(t.getType().equals("OPEN-NUM")) 
						return String.format("$QRY\n");
					
					else if(t.getType().equals("OPEN-TITLE")) 
						return String.format("$TITLE\n");
					
					else if(t.getType().equals("OPEN-DESC")) 
						return String.format("$DESC");
					
					else if(t.getType().equals("OPEN-NARR"))
						return String.format("$NARR");
					
					
					//We don't want tags in the final output
					else if(!(t.getType().contains("OPEN") || t.getType().contains("CLOSE"))) { 
						
						return t.getText();
					}
				}
				else if(poppedString.equals(irrelevantTag)) {
					relevantContent = true;
					irrelevantTag = "";
				}
				return "";
			}
			else
				return "";
			
			
		/*if(t != null) {
			
			//DOCUMENT types
			if(t.getType().equals("OPEN-DOCNO")) 
				//DOCNO indicates new document, so we scan the next token which is the document ID and return it with its label
				return String.format("$DOC\n"); //((Token)lexer.next()).getText());
			
			else if(t.getType().equals("OPEN-HEADLINE")) 
				//HEADLINE indicates the title of the document, so we need to parse the title until we hit the CLOSE-HEADLINE
				
				return String.format("$TITLE\n");
			
			else if(t.getType().equals("OPEN-TEXT")) 
				return String.format("$BODY");
			
			else if(t.getType().equals("OPEN-DATE"))
				return String.format("$DATE");
			
			else if(t.getType().equals("OPEN-LENGTH"))
				return String.format("$LENGTH");
			
			//QUERY types
			else if(t.getType().equals("OPEN-NUM")) 
				return String.format("$QRY\n");
			
			else if(t.getType().equals("OPEN-TITLE")) 
				return String.format("$TITLE\n");
			
			else if(t.getType().equals("OPEN-DESC")) 
				return String.format("$DESC");
			
			else if(t.getType().equals("OPEN-NARR"))
				return String.format("$NARR");
			
			
			//We don't want tags in the final output
			else if(!(t.getType().contains("OPEN") || t.getType().contains("CLOSE"))) { 
				
				return t.getText();
			}
		}
			
			return "";
		*/
			
		} catch(IOException e) {
			return null;
		}
	}
	
	
	/**
	 * 
	 * This returns a bigger chunk of text (capacity) long rather than going token-by-token
	 * 
	 * @return
	 */
	public String BufferedNext(int capacity) {
		
		StringBuilder sBuild = new StringBuilder(capacity);
		
		long dataCounter = 0;
		
		//System.out.format("%f / %f = %f\n", (double)dataCounter, (double)capacity, (double)dataCounter / (double)capacity);
		
		while(this.hasNext() && dataCounter <= capacity) {
			//append capacity-long characters by calling next()
			sBuild.append((String)this.next());
			dataCounter += this.lexer.yylength();
			//System.out.format("%d / %d = %f < %f\n", dataCounter, capacity, (double)dataCounter / (double)capacity, (double)1);
		}
		
		return sBuild.toString();
		
	}
	
	/**
	 * 
	 * Returns how much data has been read
	 * 
	 * @return
	 */
	public long dataRead() {
	
		//update the chars read
		this.dataRead = (long)lexer.yychar();
		return this.dataRead; 
	}
	
	/**
	 * This method looks for a file given in the parameter and if it exists, loads up the contents into the stopWords list
	 *
	 */
	public void loadStopWords(String stopfile) {
		
		
		try {
		
			File stopwordsFile = new File(stopfile);
			
			//Only if the stopwords file exists can we add it to the list
			if(stopwordsFile.exists()) {
			
				BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(stopwordsFile)));
			
				String s = "";
			
				while((s = buff.readLine()) != null) {
					//put it into the ArrayList
					this.stopWords.add(s);
				}
			}
			
		} catch(IOException e) {
			System.out.println("Something went wrong with reading the stopwords file: " + stopfile);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * returns the line number that the head of the lexer is currently at
	 * 
	 * @return
	 */
	public int getLine() {
		
		return lexer.getLine();
		
	}
	
	public void remove() {
		// Don't need this
	}
	
	//Because String arrays dont't have this method...Not optimised but oh well
	private static boolean contains(String s, String[] sa) {
		if(s == null) return true;
		for(String p: sa) { 
			if(s.contains(p)) return true;
		}
		return false;
	}

}
