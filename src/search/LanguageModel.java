package search;

import index.InvertedIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * The language model search class. Uses Jelinek-Mercer smoothing or Dirichlet prior smoothing techniques.
 * 
  */
public class LanguageModel {

	//value used for smoothing
	//public static double ALPHA = 0.2; //Jelinek-Mercer
	//public static double MU = 0; //Dirchlet prior smoothing value
	
	public static enum searchType {
		JelinekMercer, DirichletPrior;
	}
	
	
	//The smoothing function used (Jelinek-Mercer smoothing and Dirichlet Prior smoothing functions)
	private static double smoothingFunction(double param, int dtf, int numTokens, double Pavg, searchType smoothingType) {
		double result = 0.0;
		
		//System.err.format("Input: dtf=%d, numTokens=%d, Pavg=%f\n", dtf, numTokens, Pavg);
		
		if(smoothingType == searchType.JelinekMercer)
			result =  ((1.0 - param) * ((double)dtf / (double)numTokens)) + (param * Pavg);
		else if(smoothingType == searchType.DirichletPrior)
			result = (dtf + (param * Pavg)) / ((double)numTokens + param);
		
		//System.err.println("result: " + result);
		return result;
	}
	
	//gives the list of unique document IDs where the terms in the "query" appear in and their cosine similarity measures
	public static ArrayList performSearch(String query, InvertedIndex ii, double param, searchType smoothing) {

		HashMap<Integer, Double> documentRanking = new HashMap<Integer, Double>();
		TreeMap<Integer, Double> DocMatrix;
		
		int N = ii.getTotalNumberOfDocuments();
		String[] terms = query.split(" ");
		
		//System.out.format("docids.size() = %d, documents.length = %d\n",ii.docidsID.size(), ii.documents.length);
		
		//We need to initialize the DocMatrix so that we have all the documents in the collection that contain the given query terms
		
		//System.out.println("Init Doc Matrix");
		DocMatrix = ii.getDocMatrix(terms);
		
		//double[] DocMatrix = new double[N];
		//for(int dm = 0; dm < DocMatrix.length; dm++) DocMatrix[dm] = 1.0;
		
		//System.out.println("Searching Terms...");
		for(String q: terms) {
			
			//System.err.println("Searching for: " + q);
			
			//Get the position of the term's posting data
			int df = ii.getDocumentFrequency(q);
			
			if(df < 0) continue;//the token doesn't exist in the index, go to the next token
				 
			int postingPos = ii.getPostingPosition(q);
		
			double Pavg = ii.getPAvg(q);
			
			//System.out.println("Calculating weights");
			//Collections.sort(Collections.list(dset));
			
			int offset = 0;
			for(Map.Entry dm: DocMatrix.entrySet()) {
				//if this term is any a document in this set, calculate the probability of generating that term from the document
				//System.err.println("Current document: " + dm.getKey());
				//double maxLikelihoodEstimate = 0.0;
				//postingPos = ii.getTermInDocumentPos(q, (Integer)dm.getKey());
				int numTokens = ii.getNumTokens((Integer)dm.getKey());
				int dtf = 0;
				
				if(ii.postings[postingPos+offset][0] == (Integer)dm.getKey()) {
					//System.err.println("Posting: " + ii.postings.length + ", DocMatrix:length=" + DocMatrix.size() + ", " + ii.postings[postingPos+offset][0] + " postingPos=" + postingPos + " offset=" + offset);
					dtf = ii.postings[postingPos+offset][1];
					offset++;
				}
				
				double newVal = smoothingFunction(param, dtf, numTokens, Pavg, smoothing);
				dm.setValue((Double)dm.getValue() * newVal);
				
				
				/*if(ii.postings[postingPos][0] == (Integer)dm.getKey()) {
				//if(postingPos > 0) {	
					int dtf = ii.postings[postingPos][1];
				
					//double newVal = Math.pow(maxLikelihoodEstimate, 1.0 - ALPHA) * Math.pow(Pavg, ALPHA);
					double newVal = smoothingFunction(param, dtf, docNumInPostings, Pavg, ii, smoothing);
					dm.setValue((Double)dm.getValue() * newVal);
					
					//increment posting pos to next one
					df--;
					if(df > 0) postingPos++;
				
				}
				else {//multiply by a smoothing "background" probability 
					double newVal = smoothingFunction(param, 0, docNumInPostings, Pavg, ii, smoothing);
					dm.setValue((Double)dm.getValue() * newVal);
				}*/
				
			}
			
			
			//get the term frequency for each document and calculate the weights
			/*for(int docCount = postingPos; docCount < (df + postingPos); docCount++) {
				int dtf = ii.postings[docCount][1];
				
				//(P(t | d)^(1-alpha) * Pavg(t)^alpha
				int docNumInPostings = ii.postings[docCount][0];
				double maxLikelihoodEstimate = (double)dtf / (double)ii.getNumTokens(docNumInPostings);
				
				double newVal = Math.pow(maxLikelihoodEstimate, 1.0 - ALPHA) * Math.pow(Pavg, ALPHA);
				
				//double goodTuringTF;
				//goodTuringTF = (double)(dtf + 1); //* ((double)ii.getNumDocsWithGivenNumTokens(dtf+1) / (double)ii.getNumDocsWithGivenNumTokens(dtf));
								
				/*
				if(!DocMatrix.containsKey(docNumInPostings))
					DocMatrix.put(docNumInPostings, 1.0);
				double oldVal = DocMatrix.get(docNumInPostings);
				oldVal *= newVal;
				DocMatrix.put(docNumInPostings, oldVal);
				
				
				System.out.format("docCount = %d, dtf = %d, Pavg = %f, maxLikelihoodEstimate = %f, newVal = %f, oldVal = %f\n", docCount, dtf, Pavg, maxLikelihoodEstimate, newVal, oldVal);
			}*/
			
		}
		//System.out.println("Narmalizing.");
		//Add the 'normalized' similarity measure to the ranking array
		double valueSum = 0.0;
		for(double d: DocMatrix.values()) 
			valueSum += d;
		
		//System.out.println("Ranking");
		for(Map.Entry dm: DocMatrix.entrySet()) {
			//System.out.format("\n", (Double)dm.getValue(), queryVector, (Integer)dm.getKey(), ii.documents[(Integer)dm.getKey()]);
			double totalDocWeight = (Double)dm.getValue() / valueSum;
			documentRanking.put((Integer)dm.getKey(), totalDocWeight);
		}
		//put all the information together an build a Results arrayList
		ArrayList<QueryResult> results = new ArrayList<QueryResult>();
		for(Map.Entry mp: documentRanking.entrySet()) {
			
			results.add(new QueryResult((Double)mp.getValue(), (Integer)mp.getKey(), ii.getDocID((Integer)mp.getKey()), ii.getDocPos((Integer)mp.getKey())));
		}
		
		//System.out.println("Done");
		
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
	
