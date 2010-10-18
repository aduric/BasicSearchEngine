package scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;


/**
 * 
 * Tokenize from .OUT files with the stopwords
 * 
 * 
 * @author aduric
 *
 */
public class DictionaryTokenizer extends AbstractTokenizer {

	
	private String[] NON_VALID_TOKENS = {"SPACE","NEWLINE","QUOTE","QUOTES","DBLQUOT","NUM","NUMBER","DIGIT","PHONE","HYPHENS","INSENTP","LDOTS","FRAC","FRAC2","PERIOD","QUESTION","EXCLAMATION","EQUALS","PLUS","PIPE","TILDE","OPBRAC","CLBRAC","OPBRACE","CLBRACE","OPPAREN","CLPAREN","SLASH"};
	
	ArrayList<String> stopWords;
	
	private Reader r;
	private Lexer lexer;
	
	private long dataRead;
	
	//hack to generate the whole $DOC and other delimeters I might need
	//private boolean ministack = false;
	private boolean isReturnable;
	
	public DictionaryTokenizer(Reader f) {
		this.r = f;
		this.lexer = new Lexer(r);
		
		this.dataRead = 0;
		
		Arrays.sort(NON_VALID_TOKENS);
		
		//Load up the stopwords if the file exists
		this.stopWords = new ArrayList<String>();
		this.loadStopWords("stopwords.txt");
	
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
	
	
	@Override
	public long dataRead() {
		
		//update the chars read
		this.dataRead = (long)lexer.yychar();
		return this.dataRead; 
	}

	@Override
	public boolean hasNext() {
		
		return !lexer.hasNext();
	}

	@Override
	public Object next() {
		//Gets the next token from the lexer...The input comes from .OUT files in this case
		
		try {
			Token t = (Token)lexer.next();
			/*
			 * Do some processing...
			 *  
			 */
			if(t != null) {
				
				//We have to look at tokens that come after a dollar sign, they are either meta-symbols (which we need) or not
				if(!isReturnable) {
					if(t.getText().equals("DOC")) {//document delimiter
						isReturnable = true;
						return new Token("$DOC", null);
					}
					else { 
						isReturnable = true;
						return null;
					}
				}
				else if(t.getText().equals("$")) {//However, we do need to know where each document begins
					isReturnable = false;
					return null;
				}
				//If the tokenizer finds a meta-symbol or a stopword, or a topekentype that we don't want, ignore that token
				else if(this.stopWords.contains(t.getText().toLowerCase()) || contains(t.getType(), NON_VALID_TOKENS) || !isReturnable) {
					return null;
				}
				//Only return a token iff we are past the TITLE symbol
				else return t;
				
			}
			
			
		} catch(IOException e) {
			return null;
		}
		
		
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * 
	 * returns the line number that the head of the lexer is currently at
	 * TODO: lexer no keeping track of line numbers, have to recompile the lexer!!
	 * 
	 * @return
	 */
	public int getLine() {
		
		return lexer.getLine();
		
	}
	
	//Binary search on a sorted array. The 'sa' string array MUST be sorted
	private static boolean contains(String s, String[] sa) {
		if(s == null) return true;
		/*for(String p: sa) { 
			if(s.contains(p)) return true;
		}*/
		if(Arrays.binarySearch(sa, s) > 0) return true;
		return false;
	}

}
