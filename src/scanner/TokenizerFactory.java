package scanner;


import java.io.Reader;
import java.io.IOException;


/**
 *	
 * This class will create a tokenizer for our specific purpose
 * The parameters are: 1 - For the word tokenizer
 * 						2 - For the SGML tokenizer
 * 
 * @author aduric
 *
 */
public class TokenizerFactory {

	
	public enum TokenizerType {
		Tokenizer,
		SGMLTokenizer,
		DictionaryTokenizer;
	}
	
	public static AbstractTokenizer createTokenizer(TokenizerType type, Reader r) {
		
		
		try {
			switch(type) {
				case Tokenizer:
					return new Tokenizer(r);
				case SGMLTokenizer:
					return new SGMLTokenizer(r);
				case DictionaryTokenizer:
					return new DictionaryTokenizer(r);
				default:
					return null;
			}
		
		} catch(IOException e) {
			System.out.println("Error occured when trying to create a Tokenizer");
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	
}
