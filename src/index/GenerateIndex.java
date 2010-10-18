package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import scanner.AbstractTokenizer;
import scanner.Token;
import scanner.Tokenizer;

/**
 * 
 * 
 * This class generates an InvertedIndex data structure based on some collection of documents
 * To figure out where the documents are delimited, we have a constant called DOC_DELIM defined
 * 
 * 
 * @author aduric
 *
 */
public class GenerateIndex {
	
	public static final String DOC_DELIM = "$DOC";
	public static final String QRY_DELIM = "$QRY";	
	
	public static void createIndex(AbstractTokenizer parser) {
	
	
		//This hashmap keeps track of the document IDs and their position in the input file
		//We need this information later to generate the II output files
		TokenIndex ii;
		ArrayList<HashMap<String, Integer>> documentTable = new ArrayList<HashMap<String, Integer>>();
		ArrayList<Integer> docidsNumTokens = new ArrayList<Integer>();
		
		//counts the index of the current document
		int currentDocIndex = 0;
		//the current token counter of the document
		int tmpctr = 0;
		
		//	create the InvertedIndex
		ii = new TokenIndex();
		
		//System.out.println("Creating Index");
						
		while(parser.hasNext()) {
			//return the next token and add it to the InvertedIndex if it meets the necessary preconditions (ie, valid word token, not stopword, etc...)
			Token p = (Token)parser.next();
			//Add only the string to the II for now

			if(p!=null) {
				/*
				 * The Tokenizer will pass SGML-type tags. We need this to determine where a document ends
				 */
				if(p.getText().equalsIgnoreCase(DOC_DELIM)) {
					//save the line number given by the lexer. This gives us the document position
					//NOTE that we are storing the actual unique Name ID of the document.
					//Since we can guarantee that the name will come right after the $DOC tag, we can just make a call to .next()
					Token tmpt;
					while((tmpt = (Token)parser.next()) == null){} //Bizzare way of doing things...but I'm out of ideas right now  
					HashMap<String, Integer> docMap = new HashMap<String, Integer>(); 
					docMap.put(tmpt.getText(), new Integer(parser.getLine()));
					documentTable.add(docMap);
					if(docidsNumTokens.size() <= currentDocIndex || currentDocIndex == 0)
						docidsNumTokens.add(tmpctr);
					else
						docidsNumTokens.set(currentDocIndex, tmpctr);
					
					currentDocIndex++;
					tmpctr = 0;
					
				} else tmpctr++; 
				//Add Token to the inverted index. Note that the II expects (Token, int) as parameters and NOT (String, int)
				ii.add(p, currentDocIndex);
				
			}

		}
		
		
		System.out.println(currentDocIndex);
		
		//Generate the index files by reading in the just-created index (_dictionary.txt, _postings.txt, _docids.txt)
		
		
		try {
			
			File files[] = new File[3];
			files[0]= new File("dictionary.txt");
			files[1] = new File("postings.txt");
			files[2] = new File("docids.txt");
			
			//Need to check each file for existance and create it if it DNE
			for(File f: files) {
				if(!f.exists()) f.createNewFile();
				if(!f.canWrite()) throw new IOException();
			}
			
			PrintWriter pw[] = new PrintWriter[3];
			
			pw[0] = new PrintWriter(files[0]);
			pw[1] = new PrintWriter(files[1]);
			pw[2] = new PrintWriter(files[2]);
			
			//pw[0] - Print the dictionary file <term> <document frequency>
			//pw[1] - Print the postings file <docno> <term frequency>
			pw[0].println(ii.size());
			pw[1].println(ii.getTotalEntries());

System.err.println("number of terms: " + ii.size());
System.err.println("number of posting entries: " + ii.getTotalEntries());
System.err.println("number of documents: " + documentTable.size());			

			for(Map.Entry me: ii.entrySet()) {
				pw[0].format("%s %s\n", me.getKey().toString().toLowerCase(), ((Token)me.getValue()).getNumDocs());
				
				for(Map.Entry pos: ((Token)me.getValue()).getDocs().entrySet()) {
					pw[1].format("%s %s\n", pos.getKey(), pos.getValue());
				}
				
			}
			
			pw[0].flush();
			pw[1].flush();
	
			//pw[2] - Print the docids file
			pw[2].println(documentTable.size());
			for(int dtc = 0; dtc < documentTable.size(); dtc++) {
				HashMap<String, Integer> hm = documentTable.get(dtc);
				for(Map.Entry dt: hm.entrySet()) pw[2].format("%s %s %d\n", dt.getKey(), dt.getValue(), docidsNumTokens.get(dtc));
			}
			pw[2].flush();
			
			
		} catch(FileNotFoundException fn) {
			System.out.println("Error: One of the index files has not been found.");
		} catch(IOException e) {
			System.out.println("Error: Something went wrong with index file creation.");
		}
		
		
		System.out.println("Index files have been successfully created.");
		
	
	}
	
	/**
	 * 
	 * This class takes the 3 input files (dictionary, postings and docids)
	 * into static arrays for reference
	 * 
	 * @return
	 */
	public static InvertedIndex loadIndex(String dictionaryFile, String postingsFile, String docidsFile) {
				
		//ArrayList<HashMap<String, Integer>> dictionary = null;
		ArrayList<String> dictionaryTerms = null;
		ArrayList<Integer> dictionaryPositions = null;
		ArrayList<String> docidsID = null;
		ArrayList<Integer> docidsPos = null;
		ArrayList<Integer> docidsNumTokens = null;
		int[][] postings = null;
		double[] documents = null;
		
		try {
		
			//Need to check existance of the 3 index files
			File files[] = new File[3];
			files[0]= new File(dictionaryFile);
			files[1] = new File(postingsFile);
			files[2] = new File(docidsFile);
			
			for(File f: files) {
				if(!f.exists()) throw new FileNotFoundException();
			}
			
			BufferedReader r[] = new BufferedReader[3];
			
			r[0] = new BufferedReader(new InputStreamReader(new FileInputStream(files[0])));
			r[1] = new BufferedReader(new InputStreamReader(new FileInputStream(files[1])));
			r[2] = new BufferedReader(new InputStreamReader(new FileInputStream(files[2])));
			
			String s1 = "";
			
			//Load up the dictionary and the postings files into the index
			
			//The first line is a count of the totals
			int total_number_of_terms = Integer.parseInt(r[0].readLine());
			int total_number_of_entries = Integer.parseInt(r[1].readLine());
			int total_number_of_docs = Integer.parseInt(r[2].readLine());
			
			//Create the dictionary
			dictionaryTerms = new ArrayList<String>(total_number_of_terms);
			dictionaryPositions = new ArrayList<Integer>(total_number_of_terms);
			
			//Create the postings array
			postings = new int[total_number_of_entries][2];
			
			//Create the documents array
			documents = new double[total_number_of_docs+1];
			//for(int d = 0; d < documents.length; d++) documents[d] = 0.0;
			
			//Posting counter
			int pcount = 0;
			
			while((s1 = r[0].readLine()) != null) {
				String[] line = s1.split(" ");
				
				//if(line.length != 2) continue;
				String term = line[0];
				//System.err.println(term);
				int df = Integer.parseInt(line[1]);
				
				//Add to dictionary array
				//HashMap<String, Integer> tmpmap = new HashMap<String, Integer>();
				//tmpmap.put(term, pcount);//Integer.valueOf(line[1]));
				dictionaryTerms.add(term.toLowerCase());
				dictionaryPositions.add(pcount);
				
				//System.err.format("DICTIONARY: term = %s, pcount = %d, df = %d, postings = %d\n", term, pcount, df, postings.length);
				
				int curr = 0;
				for(curr = 0; curr < df; curr++) {
					
					String[] s2line = r[1].readLine().split(" ");
					
					//if(s2line.length != 2) continue;
					int docNum = Integer.parseInt(s2line[0]);
					int tf = Integer.parseInt(s2line[1]);
					
					//Put info into postings file
					//System.err.format("pcount: %d, curr: %d\n", pcount, curr);
					postings[pcount][0] = docNum;
					postings[pcount][1] = tf;
					
					//add term weight to the document array (for each posting)
					double termWeight = (double)tf * Math.log10((double)df/(double)total_number_of_docs);
					documents[docNum] += Math.pow(termWeight, 2.0);
					
					//System.out.format("POSTING: docNum = %d, tf = %d, termWeight = %f ==> %f\n", docNum, tf, termWeight, Math.pow(termWeight, 2.0));
					pcount++;
				}
				
				//Increment the pcount
				//pcount+=(curr+1);
				//pcount++;
				
			}
			
			//Load up the docids
			String s3 = "";
			
			
			docidsID = new ArrayList<String>(total_number_of_docs);
			docidsPos = new ArrayList<Integer>(total_number_of_docs);
			docidsNumTokens = new ArrayList<Integer>(total_number_of_docs);
			int docCount = 0;
			while((s3 = r[2].readLine()) != null) {
				
				String[] s3line = s3.split(" ");
				
				//Load up the docids
				docidsID.add(s3line[0]);
				docidsPos.add(Integer.valueOf(s3line[1]));
				docidsNumTokens.add(Integer.valueOf(s3line[2]));
				
				
			}
		
		} catch(FileNotFoundException fn) {
			System.out.println("Error: One of the index files has not been found.");
		} catch(IOException e) {
			System.out.println("Error: Something went wrong with index file creation.");
		}
		
		System.out.println("Inverted Index loaded. Ready for search.");
		
		InvertedIndex ii = new InvertedIndex(dictionaryTerms, dictionaryPositions, docidsID, docidsPos, docidsNumTokens, postings, documents); 
		
		
		/*try {
			PrintWriter plog = new PrintWriter(new File("ii.log"));
			plog.println(ii);
		}catch(IOException e) {
			System.out.println(e);
		}*/
		
		//System.out.println(ii);
		
		return ii;
		
	}

	public static TreeMap<Integer, String> createQueryIndex(AbstractTokenizer parser) {
		
		System.out.println("Creating Query Index");
		
		TreeMap<Integer, String> myQuery = new TreeMap<Integer, String>();
		int tmpd = 0;				
		
		while(parser.hasNext()) {
			//get the next token from the query
			String p = (String)parser.next();

			if(p!=null) {
				if(p.equals("QRY")) {
					p = (String)parser.next();
					p = (String)parser.next();
					//myQuery.put(Integer.parseInt(p), null);
					tmpd = Integer.parseInt(p);
				}
				else if(p.equals("TITLE")) {
					p = (String)parser.next();
					String tmps = "";
					while(!(p = (String)parser.next()).equals("$")) {
						tmps+=p;
					}
					myQuery.put(tmpd, tmps.trim().toLowerCase());
				}
				

			}

		}
		
		//return the queries
		return myQuery;

	}

	

}
