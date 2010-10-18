package index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * Once the index files have been loaded, we use this inverted index object to point to the
 * terms index, the postings index and the document index
 * 
 * @author adnan
 *
 */
public class InvertedIndex implements Serializable {

	//public ArrayList<HashMap<String, Integer>> dictionary;
	public ArrayList<String> dictionaryTerms;
	public ArrayList<Integer> dictionaryPositions;
	public ArrayList<String> docidsID;
	public ArrayList<Integer> docidsPos;
	public ArrayList<Integer> docidsNumTokens;
	public int[][] postings;
	public double[] documents;
	public long C;
	
	public InvertedIndex(ArrayList<String> dictionaryTerms, 
							ArrayList<Integer> dictionaryPositions, 
							ArrayList<String> docidsID,
							ArrayList<Integer> docidsPos, 
							ArrayList<Integer> docidsNumTokens,
							int[][] postings, 
							double[] documents) {
			this.dictionaryTerms = dictionaryTerms;
			this.dictionaryPositions = dictionaryPositions;
			this.docidsID = docidsID;
			this.docidsPos = docidsPos;
			this.docidsNumTokens = docidsNumTokens;
			this.postings = postings;
			this.documents = documents;
			this.C = this.postings.length;
	}
	
	public int getTotalNumberOfDocuments() {
		return docidsID.size();
	}
	
	public int getDocumentFrequency(String q) {
		int qIndex = dictionaryTerms.indexOf(q);
		if(qIndex < 0) return -1;
		int df1 = dictionaryPositions.get(qIndex);
		int df2;
		if(qIndex >= dictionaryTerms.size())
			df2 = dictionaryPositions.get(dictionaryPositions.size()-1);
		else
			df2 = dictionaryPositions.get(qIndex+1);
		
		//System.err.format("qIndex = %d, df1 = %d, df2 = %d\n", qIndex, df1, df2);
		
		return (df2-df1);
	}
	
	public int getPostingPosition(String q) {
		int qIndex = dictionaryTerms.indexOf(q);
		if(qIndex < 0) return -1;
		int pos = dictionaryPositions.get(qIndex);
		
		return pos;
	}
	
	public String getDocID(int index) {
		if(index < 1) return this.docidsID.get(0);
		else return this.docidsID.get(index-1);
	}

	public int getDocPos(int index) {
		if(index < 1) return this.docidsPos.get(0);
		else return this.docidsPos.get(index-1);
	}
	
	public int getNumTokens(int index) {
		if(index < 1) return this.docidsNumTokens.get(0);
		return this.docidsNumTokens.get(index-1);
	}
		
	
	//Get the average probability that that a document contains a specific term, given that term
	public double getPAvg(String term) {
		int postingPos = this.getPostingPosition(term);
		int df = this.getDocumentFrequency(term);
		double Psum = 0.0;
		
		//System.err.format("postingPos: %d, df: %d\n", postingPos, df);
		
		for(int docCount = postingPos; docCount < (df + postingPos); docCount++) {
			Psum += ((double)this.postings[docCount][1]);// / (double)this.getNumTokens(this.postings[docCount][0]));
		}
		
		//Pavg = sum(dtf / dl) / df
		double Pavg = Psum / this.C;//(double)df;
		
		return Pavg;
	}
	
	//gets all the documents that contain at least 1 term in the given query and initializes each with 1.0
	public TreeMap<Integer, Double> getDocMatrix(String[] query) {
		
		TreeMap<Integer, Double> DocMatrix = new TreeMap<Integer, Double>(); 
		
		for(String s: query) {
			int postingPos = this.getPostingPosition(s);
			int df = this.getDocumentFrequency(s);
			for(int docCount = postingPos; docCount < (df + postingPos); docCount++) {
				int docNumInPostings = this.postings[docCount][0];
				if(!DocMatrix.containsKey(docNumInPostings))
					DocMatrix.put(docNumInPostings, 1.0);
			}
		}
		
		return DocMatrix;
	}
	
	//Get the posting position of the term's document, if it exists
	public int getTermInDocumentPos(String term, int document) {
		int postingPos = this.getPostingPosition(term);
		int df = this.getDocumentFrequency(term);
		
		for(int docCount = postingPos; docCount < (df + postingPos); docCount++) {
			if(this.postings[docCount][0] == document) 
				return docCount;
		}
		
		//The given term does not exist in the given document 
		return -1;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DICTIONARY\n");
		for(int c = 0; c < this.dictionaryTerms.size(); c++) {
			sb.append(String.format("%s %d\n", this.dictionaryTerms.get(c), this.dictionaryPositions.get(c)));
		}
		sb.append("POSTINGS\n");
		for(int c = 0; c < this.postings.length; c++) {
			sb.append(String.format("%d %d\n", this.postings[c][0], this.postings[c][1]));
		}
		sb.append("DOCIDS\n");
		for(int c = 0; c < this.docidsID.size(); c++) {
			//System.out.format("%s\n", this.docids.get(c));
			sb.append(String.format("%s %d\n", this.docidsID.get(c), this.docidsPos.get(c)));
		}
		sb.append("DOCUMENTS\n");
		for(int c = 0; c < this.documents.length; c++) {
			//System.out.format("%d %f\n", c, this.documents[c]);
			sb.append(String.format("%d %f\n", c, this.documents[c]));
		}
		
		return sb.toString();
		
	}
	
}
