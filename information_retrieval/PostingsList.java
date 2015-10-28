package search;

/**
 * an implementation of postings list based on a singly linked list
 * 
 * @author dkauchak
 */
public class PostingsList implements QueryResult{
	private Node head = null;
	private Node tail = null;
	private int occurrences = 0;
	
	/**
	 * add a document ID to this posting list
	 *
	 * @param docID the docID of the document being added
	 */
	public void addDoc(int docID){
		if( head == null ){
			head = new Node(docID, 1);
			tail = head;
		}else{
			tail.setNext(new Node(docID, 1));
			tail = tail.next();
		}
		
		occurrences++;
	}
	
	// Normalization functions
	// ---------------------------------------------------------------
	/**
	 * Apply boolean term normalization to this postings list
	 */
	public void booleanNormalizeTermFrequencies(){
		Node temp = head;
		
		while( temp != null ){
			if( temp.weight > 0 ){
				temp.weight = 1;
			}
			
			temp = temp.next();
		}
	}
	
	/**
	 * Apply log frequency normalization to this postings list
	 */
	public void logNormalizeTermFrequencies(){
		Node temp = head;
		
		while( temp != null ){
			temp.weight = 1+Math.log(temp.weight);			
			temp = temp.next();
		}
	}
	
	/**
	 * multiply all of the weights by log(number_of_docs/number_of_docs_with_this_term)
	 */
	public void idfWeight(int numDocs){
		double idfWeight = Math.log((double)numDocs/occurrences);
		
		Node temp = head;
		
		while( temp != null ){
			temp.weight *= idfWeight;
			temp = temp.next();
		}
	}
	
	/**
	 * the array weights should have a weight for each document ID, that is
	 * weights[docID] is the weight for docID
	 * 
	 * @param weights
	 */
	public void genericWeight(double[] weights){
		Node temp = head;
		
		while( temp != null ){
			temp.weight *= weights[temp.docID];
			temp = temp.next();
		}
	}
	
	/**
	 * AGAIN, THIS IS NOT THE RIGHT WAY TO DO THIS, HOWEVER, IT WILL WORK
	 * 
	 * @param docLengths
	 */
	public void accumulateDocumentLengths(double[] docLengths){
		Node temp = head;
		
		while( temp != null ){
			docLengths[temp.docID] += temp.weight * temp.weight;
			temp = temp.next;
		}
	}
	
	// ---------------------------------------------------------------
	
	/**
	 * add one to the tf count for the last entry in the posting list
	 * as a sanity check, we'll require the previous docID to be passed in
	 * and check this against the last element in our posting list
	 * 
	 * @param docID
	 */
	public void incrementTFOfLastEntry(int docID){
		if( docID != tail.docID() ){
			throw new RuntimeException("PostingsList::addOneToLastEntry - Tried to add to wrong docID " + 
					                    docID + " vs. " + tail.docID() );
		}
		
		tail.incrementTF();
	}
	
	//float scores[N] = 0
	//for each query term t
	//calculate wt,q 
	//for each entry in t’s postings list: docID, wt,d
	//scores[docID] += wt,q * wt,d
	//return top k components of scores
	
	/**
	 * THIS IS PROBABLY NOT THE BEST WAY TO DO THIS	
	 * 
	 * The version I gave you decouplee the Index class and the PostingsList class
	 * but I didn't have time to update my solution :)
	 * 
	 * @param result
	 * @param termWeight
	 */
	public void addToResult(Accumulator result, double termWeight){
		Node temp = head;
		
		while( temp != null ){
			result.addScore(temp.docID, temp.weight * termWeight);
			
			temp = temp.next();
		}
	}
	
	/**
	 * Given a postings list, return the NOT of the postings list, i.e.
	 * a postings list that contains all document ids not in "list"
	 * 
	 * document IDs should range from 0 to maxDocID
	 * 
	 * @param list the postings list to NOT
	 * @param maxDocID the maximum allowable document ID
	 * @return not of the posting list
	 */
	public static PostingsList not(PostingsList list, int maxDocID){
		PostingsList returnMe = new PostingsList();
		
		int prev_entry = -1;

		Node posting = list.head;
		
		while( posting != null ){
			// add all the docIDs from prev_entry to the current entry
			for( int i = prev_entry+1; i < posting.docID(); i++ ){
				returnMe.addDoc(i);
			}
			
			prev_entry = posting.docID();
			posting = posting.next();
		}
		
		// add all the remaining entries from the last doc in the posting list to the
		// last available doc
		for( int i = prev_entry+1; i <= maxDocID; i++ ){
			returnMe.addDoc(i);
		}
		
		return returnMe;
	}
	
	/**
	 * Given two postings lists, return a new postings list that contains the AND
	 * of the postings, i.e. all the docIDs that occur in both posting1 and posting2
	 * 
	 * @param posting1
	 * @param posting2
	 * @return the AND of the postings lists
	 */
	public static PostingsList andMerge(PostingsList posting1, PostingsList posting2){
		PostingsList merged = new PostingsList();
		
		if( posting1 != null && posting2 != null &&
			posting1.size() > 0 && posting2.size() > 0 ){
			
			Node p1 = posting1.head;
			Node p2 = posting2.head;
			
			while( p1 != null && p2 != null ){
				if( p1.docID() == p2.docID() ){
					// we found a match, so add it to the list
					merged.addDoc(p1.docID());
					p1 = p1.next();
					p2 = p2.next();
				}else if( p1.docID() < p2.docID() ){
					// move up p1
					p1 = p1.next();
				}else{
					// move up p2
					p2 = p2.next();
				}
			}
		}
		
		return merged;
	}
	
	/**
	 * Given two postings lists, return a new postings list that contains the OR
	 * of the postings, i.e. all those docIDs that occur in either posting1 and posting2
	 * 
	 * @param posting1
	 * @param posting2
	 * @return the OR of the postings lists
	 */
	public static PostingsList orMerge(PostingsList posting1, PostingsList posting2){
		PostingsList merged = new PostingsList();
		
		Node p1 = null;
		Node p2 = null;
		
		if( posting1 != null ){
			p1 = posting1.head;
		}
		
		if( posting2 != null ){
			p2 = posting2.head;
		}
			
		while( p1 != null && p2 != null ){
			if( p1.docID() == p2.docID() ){
				// we found a match, so add it to the list
				merged.addDoc(p1.docID());
				p1 = p1.next();
				p2 = p2.next();
			}else if( p1.docID() < p2.docID() ){
				// move up p1 and add it
				merged.addDoc(p1.docID());
				p1 = p1.next();
			}else{
				// move up p2 and add it
				merged.addDoc(p2.docID());
				p2 = p2.next();
			}
		}
			
		if( p1 != null && p2 != null ){
			throw new RuntimeException("Houston, we have a problem...");
		}
			
		while( p1 != null ){
			merged.addDoc(p1.docID());
			p1 = p1.next();
		}
			
		while( p2 != null ){
			merged.addDoc(p2.docID());
			p2 = p2.next();
		}
			
		return merged;
	}
		
	/**
	 * @return the number of docIDs for this posting list
	 */
	public int size(){
		return occurrences;
	}
	
	/**
	 * From the linked list structure, generate an integer array containing 
	 * all of the document ids.  This will make our life easy when we want to 
	 * print out the ids.  (another option would have been to write an iterator, but
	 * this is easier).
	 * 
	 * @return
	 */
	public int[] getIDs(){
		if( head == null ){
			return null;
		}else{
			int[] ids = new int[occurrences];
			Node temp = head;
			int i = 0;
			
			while( temp != null ){
				ids[i] = temp.docID();
				temp = temp.next();
				i++;
			}
			
			return ids;
		}
	}
	
	/**
	 * Since this is for boolean queries, the score should be 1.0 for all
	 * documents that are in this posting list
	 */
	public double[] getScores(){
		if( head == null ){
			return null;
		}else{
			double[] scores = new double[occurrences];
			Node temp = head;
			int i = 0;
			
			while( temp != null ){
				scores[i] = temp.weight();
				temp = temp.next();
				i++;
			}
			
			return scores;
		}
	}
		
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		
		if( head != null ){
			Node temp = head;
			
			while( temp != null ){
				buffer.append(temp.toString() + " ");
				temp = temp.next();
			}
		}
		
		return buffer.toString();
	}
	
	/**
	 * A private node class for creating a linked list
	 * within the PostingsList class
	 * 
	 * @author dkauchak
	 *
	 */
	private class Node{
		private Node next = null;
		private int docID;
		private double weight;
		
		public Node(int docID, double weight){
			this.docID = docID;
			this.weight = weight;
		}
		
		public Node next(){
			return next;
		}
		
		public int docID(){
			return docID;
		}
		
		public double weight(){
			return weight;
		}
		
		public void incrementTF(){
			weight++;
		}
		
		public void setNext(Node next){
			this.next = next;
		}
		
		public String toString(){
			return docID + ":" + weight;
		}
	}
}