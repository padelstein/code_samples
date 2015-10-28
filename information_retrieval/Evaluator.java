package search.evaluation;

import java.util.ArrayList;

/**
 * This class is used to evaluate the ranked results from a search engine
 * 
 * @author dkauchak
 *
 */
public class Evaluator {
	/** 
	 * Calculate recall at k.
	 * 
	 * @param relevantIDs the list of relevant ids for this query (may be in any order)
	 * @param returnedIDs the list of IDs returned from the system in order
	 * @param k
	 * @return Returns the recall at k
	 */
	public static double recall(Integer[] relevantIDs, int[] returnedIDs, int k){
		double numRelevant = 0;
		if (relevantIDs == null) {
			System.out.println("null array");
		}
		
		for( int i = 0; i < Math.min(returnedIDs.length, k); i++) {
			for(int j = 0; j < relevantIDs.length; j++) {
				if(relevantIDs[j] == returnedIDs[i]) {
					numRelevant += 1.0;
					break;
				}
			}
		}
		if (relevantIDs.length == 0) {
			return 0;
		}
		return numRelevant/relevantIDs.length;
	}
	
	/** 
	 * Calculate precision at k.
	 * 
	 * @param relevantIDs the list of relevant ids for this query (may be in any order)
	 * @param returnedIDs the list of IDs returned from the system in order
	 * @param k
	 * @return Returns the precision at k
	 */
	public static double precision(Integer[] relevantIDs, int[] returnedIDs, int k){
		double numRelevant = 0.0;
		
		for( int i = 0; i < Math.min(returnedIDs.length, k); i++) {
			for(int j = 0; j < relevantIDs.length; j++) {
				if(relevantIDs[j] == returnedIDs[i]) {
					numRelevant += 1.0;
					break;
				}
			}
		}
		
		return numRelevant/k;
	}

	/** 
	 * Calculate R-Precision
	 * 
	 * @param relevantIDs the list of relevant ids for this query (may be in any order)
	 * @param returnedIDs the list of IDs returned from the system in order
	 * @return Returns the R-Precision
	 */
	public static double rPrecision(Integer[] relevantIDs, int[] returnedIDs){
		double numRelevant = 0.0;
		
		for( int i = 0; i < Math.min(returnedIDs.length, relevantIDs.length); i++) {
			for(int j = 0; j < relevantIDs.length; j++) {
				if(relevantIDs[j] == returnedIDs[i]) {
					numRelevant += 1.0;
					break;
				}
			}
		}
		if (relevantIDs.length == 0) {
			return 0.0;
		}
		return numRelevant/relevantIDs.length;
	}

	
	
	/** 
	 * Calculate MAP
	 * 
	 * @param relevantIDs the list of relevant ids for this query (may be in any order)
	 * @param returnedIDs the list of IDs returned from the system in order
	 * @return Returns the MAP
	 */
	public static double map(Integer[] relevantIDs, int[] returnedIDs){
		
		ArrayList<Double> results = new ArrayList<Double>();
		
		Double numRelevant = 0.0;
		
		for( int i = 0; i < returnedIDs.length; i++) {
			for(int j = 0; j < relevantIDs.length; j++) {
				if(relevantIDs[j] == returnedIDs[i]) {
					numRelevant += 1.0;
					results.add(numRelevant/(i+1));
					break;
				}
			}
		}
		Double total = 0.0;
		for ( Double result : results) {
			total += result;
		}
		if (numRelevant == 0) {
			return 0;
		}
		return total/numRelevant;
	}
}
