package scanner;

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
 * It returns only the tokens that are specified and skips everything else
 * 
 * @author aduric
 *
 */
public class Tokenizer extends AbstractTokenizer {
	
	private Reader r;
	private Lexer lexer;
	//This is very innificient to check for tokens taht aren't needed at this stage. Should be done in the lexer I think.
	private final String[] NON_VALID_TOKENS = {"SPACE","NEWLINE","QUOTE","QUOTES","DBLQUOT"};
	private static String[] RELEVANT_TAGS = {"TEXT","DOC","DOCNO","HEADLINE","P"};
	private static Stack<String> tagStack = new Stack<String>();
	private static boolean relevantContent = true;
	private static String irrelevantTag = "";
	private long dataRead;
	
	/**
	 * 
	 * @param f The file containing the text to be tokenized
	 * @throws IOException
	 */
	public Tokenizer(Reader f) throws IOException {
		
		this.r = f;
		this.lexer = new Lexer(r);
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
					return t;
				}
				else if(poppedString.equals(irrelevantTag)) {
					relevantContent = true;
					irrelevantTag = "";
				}
				
				return null;
			}
			else
				return null;
		} catch(IOException e) {
			return null;
		}
	}
	
	public Stack<String> getStack() {
		return tagStack;
	}
	
	public long dataRead() { 
		//update the chars read
		this.dataRead = (long)lexer.yychar();
		return this.dataRead; 
	}

	public void remove() {
		// Don't need this
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
	
	//Because String arrays dont't have this method...Not optimised but oh well
	private static boolean contains(String s, String[] sa) {
		if(s == null) return true;
		for(String p: sa) { 
			if(s.contains(p)) return true;
		}
		return false;
	}

}
