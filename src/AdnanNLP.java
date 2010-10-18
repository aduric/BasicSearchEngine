import index.GenerateIndex;
import index.InvertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import scanner.AbstractTokenizer;
import scanner.TokenizerFactory;
import search.LanguageModel;
import search.QueryResult;
import search.QueryResultComparator;
import search.Search;

/**
 * 
 * A quick class to run the program in console mode, if Gtk is not available.
 * 
 * Assumptions: This class will first read in the EXISTING dictionary.txt, postings. txt and docids.txt to load the index into memory
 * 
 * Type in -q <query file> to run an experiment with both the Vector Space Model and the Language model on all the queries in that file.
 * This will generate a <query file>_results.txt with the results.
 * 
 * @param args
 */
public class AdnanNLP {

	
	static int pagerCurrentIndex = 0;
	static InvertedIndex ii = null;
	static ArrayList<QueryResult> ranking = null;
	
	
	public static void main(String[] args) {
		//Check to see if the user wants to run an experiment with a query file in the form of -q <query file>
		if(args.length > 1 && args[0].equals("-q")) {
			System.err.println("Welcome to AdnanNLP. You have chosen to do an experiment on an exisiting query file.");
			System.err.println("Please wait while the index loads.");
			File serialInvertedIndex = new File("ii_serial");
			
			double param = 0.0;
			if(args.length > 2) { param = Double.parseDouble(args[2]); }
			
			/*if(serialInvertedIndex.exists()) {
				System.err.println("Using serialized inverted index.");
		        FileInputStream fis = null;
		        ObjectInputStream objis = null;
		        try {
		        	fis = new FileInputStream(serialInvertedIndex);
		        	objis = new ObjectInputStream(fis);
		        	ii = (InvertedIndex)objis.readObject();
		        	objis.close();
		        }
		        catch(IOException e) {
		        	e.printStackTrace();
		        }
		        catch(ClassNotFoundException e) {
		        	e.printStackTrace();
		        }
			}
			else {
				System.err.println("Creating serialized inverted index.");
		        //Load the index into memory
		        ii = GenerateIndex.loadIndex("dictionary.txt", "postings.txt", "docids.txt");
		        FileOutputStream fos = null;
		        ObjectOutputStream objos = null;
		        try {
		        	fos = new FileOutputStream(serialInvertedIndex);
		        	objos = new ObjectOutputStream(fos);
		        	objos.writeObject(ii);
		        	objos.close();
		        }
		        catch(IOException e) {
		        	e.printStackTrace();
		        }
			}
			
			System.err.println("Creating serialized inverted index.");
			
			*/
			
	        //Load the index into memory
	        ii = GenerateIndex.loadIndex("dictionary.txt", "postings.txt", "docids.txt");
			
	        System.err.format("dictionary: %d, postings: %d, docids: %d\n", ii.dictionaryTerms.size(), ii.postings.length, ii.docidsID.size());
	        
	        //System.exit(0);
	        
	        //Load the index into memory
	        //ii = GenerateIndex.loadIndex("dictionary.txt", "postings.txt", "docids.txt");
			
	        System.err.println("Index loaded, beginning query experiment with parameters: " + param);
	        
			
			//Parse the queries file and pick up all the 'titles'
			try {
				File queryFile = new File(args[1]);
				
				//Create the output "results.txt" file
				File[] outFile = new File[2];
				outFile[0] = new File("results_vs.txt");
				outFile[1] = new File("JM_results.txt");
				PrintWriter[] pw = new PrintWriter[2];
				
				for(int fc = 0; fc < 2; fc++) {
				
					if(!outFile[fc].exists())
						outFile[fc].createNewFile();
					
					
					pw[fc] = new PrintWriter(outFile[fc]);
				}
				
			if(queryFile.exists()) {
				AbstractTokenizer queryTokenizer =  TokenizerFactory.createTokenizer(TokenizerFactory.TokenizerType.SGMLTokenizer, new BufferedReader(new InputStreamReader(new FileInputStream(queryFile))));
				TreeMap<Integer, String> queries = GenerateIndex.createQueryIndex(queryTokenizer);
					
					//TreeMap<Integer, String> queries = new TreeMap<Integer, String>();
					//queries.put(310, "Radio Waves and Brain Cancer");
					
					//for(String s: queries) System.out.println(s);
					
				for(param = 0.0; param <= 10000.0; param+=1000.0) {	
					File testingFile = new File(String.format("%s%.1f.txt", "DP_results_", param));	
					
					if(!testingFile.exists())
						testingFile.createNewFile();
					
					System.out.println("Writing results to file: " + testingFile);
					
					
					PrintWriter pwTest = new PrintWriter(testingFile);
					
					//Do the actual search for all the queries in the arraylist
					pagerCurrentIndex = 0;
					int queryNum = 0;	
					for(Map.Entry<Integer, String> myQuery: queries.entrySet()) {
						//myQuery = "poliomyelitis and post-polio";
						//long startTime = System.currentTimeMillis();
						//ArrayList<QueryResult> rankingM1 = Search.performSearch(myQuery.getValue().toLowerCase(), ii);
						//long m1Time = System.currentTimeMillis();
						ArrayList<QueryResult> rankingM2 = LanguageModel.performSearch(myQuery.getValue().toLowerCase(), ii, param, LanguageModel.searchType.DirichletPrior);
						//long m2Time = System.currentTimeMillis();
						//Sort the results by the similarity ranking in descending order
						//Collections.sort(rankingM1, new QueryResultComparator("simScore"));
						Collections.sort(rankingM2, new QueryResultComparator("simScore"));
						
						//print first 10 results
						//pager(pagerCurrentIndex, System.out);
						
						//print the query results from both models - if there are less than 10 results, print just the results
						/*
						pw.format("Query: [%d] %d %s\n", queryNum, myQuery.getKey(), myQuery.getValue());
						pw.format("%2s %s %24s\n", "#", "M1", "M2");
						int r1size = rankingM1.size();
						int r2size = rankingM2.size();
						for(int c = 0; c < ((((r1size < r2size)?r1size:r2size) < 10)?((r1size < r2size)?r1size:r2size):10); c++) 
							pw.format("%2d %s(%4g) %s(%4g)\n", c+1, rankingM1.get(c).docID, rankingM1.get(c).simScore, rankingM2.get(c).docID, rankingM2.get(c).simScore);
						//Print out the time it took for each search
						pw.format("T  %g s %20g s\n", (double)(m1Time-startTime)/1000.0, (double)(m2Time-m1Time)/1000.0);
						*/
						/*
						for(int c = 0; c < rankingM1.size(); c++) {
							pw[0].format("%d %d %s %d %f %s\n", myQuery.getKey(), c+1, rankingM1.get(c).docID, 0, rankingM1.get(c).simScore, "STANDARD");
						
						}
						pw[0].flush();
						*/
						
						for(int c = 0; c < rankingM2.size(); c++) {
							pwTest.format("%d %d %s %d %f %s\n", myQuery.getKey(), c+1, rankingM2.get(c).docID, 0, rankingM2.get(c).simScore, "STANDARD");
						
						}
						pwTest.flush();
						
						queryNum++;
						//pw.println();
						
						//if(queryNum > 1) break;
					}
					pwTest.close();
					System.err.println("Done creating " + testingFile);
					//break;
				}
				}
				else
					System.err.println("File "+ queryFile +"does not exist.");
			
				
			
			
			} catch(IOException e) {
				System.err.println(e);
			}
		}
		else { //No arguments supplied, plain keyword search
			
			System.err.println("Welcome to AdnanNLP. Please enter a query or enter 'q' to quit.");
			
			System.err.println("Please wait while the index loads.");
			
	        //Load the index into memory
	        ii = GenerateIndex.loadIndex("dictionary.txt", "postings.txt", "docids.txt");
			
	        System.err.println("Index loaded, you may enter a query.");
	        
			Scanner scan = new Scanner(System.in);
			
			String myQuery = "";
			
			while(!myQuery.equals("q")) {
				//Search and print results
				System.err.print("Please enter a new query: ");
				myQuery = scan.nextLine();
				if(myQuery.equals("q")) break;
				
				//Get the ranking by doing a vector space search
				ranking = Search.performSearch(myQuery, ii);
				
				//Sort the results by the similarity ranking in descending order
				Collections.sort(ranking, new QueryResultComparator("simScore"));
				System.err.println("Found: "+ranking.size()+" results.");
				
				if(ranking.size() <= 0) continue;
				System.err.println("Query Results: Type 'm' for more results (if any). Enter any other key for another query.");
				
				//While user presses the RETURN key, show more results. If there are no more results, go back to 0
				String page;
				do {
					if(pager(pagerCurrentIndex, System.out) == 1) pagerCurrentIndex = 0; else pagerCurrentIndex+=10;
				} while((page = scan.next()).equals("m"));
	
				System.err.println("");
				
			}
		}
		
		
		System.err.println("Thank you for using the program.");

	}
	
	/**
	 * 
	 * Print the results from a certain start position
	 * 
	 */
	private static int pager(int start, PrintStream ps) {
		//print out the results
		for(int r = start; r < start+10; r++) {
			if(r >= ranking.size() || r < 0) return 1;
			ps.format("%d: %s\n", r, ranking.get(r).toString());
		}
	
		return 0;
	}
}
