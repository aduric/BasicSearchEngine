package search;

import index.InvertedIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
  *<p>
  * This class searches the given inverted index (given by ii) for the given terms (given by keywords)
  * </p>
  * <p>
  * This search utilizes the Vector Space search model. It searches the inverted index that has been loaded
  * and creates an ArrayList holding QueryResult objects. These objects act as holders of search result information.
  * </p>
  * <p>
  * Then these are sorted and shown to the user in the DocumentBrowser.
  * </p>
  */
public class Search {

	//gives the list of unique document IDs where the terms in the "query" appear in and their cosine similarity measures
	public static ArrayList performSearch(String query, InvertedIndex ii) {

		HashMap<Integer, Double> documentRanking = new HashMap<Integer, Double>();
		
		int N = ii.getTotalNumberOfDocuments();
		String[] terms = query.split(" ");
		HashMap<Integer, Double> DocMatrix = new HashMap<Integer, Double>();
		
		//System.out.format("docids.size() = %d, documents.length = %d\n",ii.docidsID.size(), ii.documents.length);
		
		//Calculate the query vector
		double queryVector = 0.0;
		for(String q: terms) {
			//System.out.println("Searching for: " + q);
			
			//Get the position of the term's posting data
			int df = ii.getDocumentFrequency(q);
			
			if(df < 0) continue;//the token doesn't exist in the index, go to the next token
				 
			int postingPos = ii.getPostingPosition(q);
			double idf = Math.log10((double)df / (double)N);
			
			//System.out.format("N = %d, df = %d, idf = %f, pos = %d\n",N, df, idf, postingPos);
			
			int qtf = numTerms(terms, q);
			double queryWeight = (double)qtf * idf;
			
			queryVector+=Math.pow(queryWeight, 2.0);
				
			//System.out.format("qtf = %d, queryWeight = %f, queryVector = %f\n", qtf, queryWeight, queryVector);
			
			//get the term frequency for each document and calculate the weights
			for(int docCount = postingPos; docCount < (df + postingPos); docCount++) {
				int dtf = ii.postings[docCount][1];
				double termWeight = (double)dtf * idf;
				
				if(!DocMatrix.containsKey(ii.postings[docCount][0]))
					DocMatrix.put(ii.postings[docCount][0], 0.0);
				double oldVal = DocMatrix.get(ii.postings[docCount][0]);
				oldVal += (queryWeight * termWeight);
				DocMatrix.put(ii.postings[docCount][0], oldVal);
				
				//System.out.println("=============================================================================");
				//System.out.format("dtf = %d, termWeight = %f, oldVal = %f\n", dtf, termWeight, oldVal);
				
			}
			
		}
		
		queryVector = Math.sqrt(queryVector);
		
		//compute the similarity measure
		for(Map.Entry dm: DocMatrix.entrySet()) {
			double cosineSim = (Double)dm.getValue() / (queryVector * Math.sqrt(ii.documents[(Integer)dm.getKey()]));
			//System.out.format("Q * D = %f, |Q| = %f, documents[] = %d, |D| = %f\n", (Double)dm.getValue(), queryVector, (Integer)dm.getKey(), ii.documents[(Integer)dm.getKey()]);
			documentRanking.put((Integer)dm.getKey(), cosineSim);
		}
		
		//put all the information together an build a Results arrayList
		ArrayList<QueryResult> results = new ArrayList<QueryResult>();
		for(Map.Entry mp: documentRanking.entrySet()) {
			
			results.add(new QueryResult((Double)mp.getValue(), (Integer)mp.getKey(), ii.getDocID((Integer)mp.getKey()), ii.getDocPos((Integer)mp.getKey())));
		}
		
		return results;
		
	}
	
	
	private static int numTerms(String[] terms, String s) {
		int tf = 0;
		for(String t: terms) {
			if(t.equalsIgnoreCase(s)) tf++;
		}
		return tf;
	}


}