package search;

/**
 * 
 * Once a search has been done, this class will hold all the information about a specific result:
 * 
 * ranking, similarity score, document num, document id, document line number
 * 
 * @author adnan
 *
 */
public class QueryResult {
	
	public double simScore;
	public int docNum;
	public String docID;
	public int docLine;
	public String docTitle;
	public String docBrief;
	
	public QueryResult(double simScore, int docNum, String docID, int docLine) {
		this.simScore=simScore;
		this.docNum=docNum;
		this.docID=docID;
		this.docLine=docLine;
	}
	
	public String toString() {
		return String.format("Document number: %d\nDocument ID: %s\nLine in Document: %d\nSimilarity Score: %g\n", this.docNum, this.docID, this.docLine, this.simScore);
	}
	

}
