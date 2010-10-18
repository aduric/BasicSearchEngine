package search;

import java.util.Comparator;

public class QueryResultComparator implements Comparator<Object> {
	
	private String compareType = "";
	
	public QueryResultComparator(String cType) {
		this.compareType = cType;
	}
	
	public int compare(Object a, Object b) {
		if(this.compareType.equalsIgnoreCase("simScore")) {//comparing by the similarity score
			if(((QueryResult)a).simScore > ((QueryResult)b).simScore) return -1;
			else if(((QueryResult)a).simScore < ((QueryResult)b).simScore) return 1;
			else return 0;
		}
		else return 0;
	}

}
