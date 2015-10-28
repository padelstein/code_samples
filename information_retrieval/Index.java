package search;

import java.util.Collections;
import java.util.Hashtable;
import java.util.ArrayList;

import search.data.DocumentReader;


/**
 * this holds the actual index and also provides the interface to the search
 * engine for querying the index
 * 
 * @author dkauchak
 */
public class Index {
	public static enum TERM_MODIFIER {n, l, b};
	public static enum DOC_MODIFIER {n, t};
	public static enum LENGTH_MODIFIER {n, c};
	
	private int maxDocID;
	private Hashtable<String,PostingsList> index;
	
	/**
	* Creates a new index based on the documents supplied by the document reader.
	* The different modifiers dictate how the terms are weights, etc.
	* 
	* @param reader containing the documents
	* @param termModifier different term schemes
	* @param docModifier different document weighting schemes
	* @param lengthModifier different length normalization schemes
	*/
	public Index(DocumentReader reader, TERM_MODIFIER termModifier, DOC_MODIFIER docModifier, 
			LENGTH_MODIFIER lengthModifier){
		
		index = combinePostings(collectEntries(reader));
		normalizeIndex(termModifier, docModifier, lengthModifier);
	}
	
	/**
	 * Given a text query, issues the query against the index
	 * and generates the set of ranked results
	 * 
	 * @param textQuery the text query
	 * @return the ranked results
	 */
	public VectorResult rankedQuery(Query query){
		Hashtable<String, Integer> queryTerms = new Hashtable<String, Integer>();
		
		for( String term: query.getText() ){
			if( !queryTerms.containsKey(term) ){
				queryTerms.put(term, new Integer(0));
			}
			
			queryTerms.put(term, queryTerms.get(term) + 1);
		}
		
		Accumulator acc = new Accumulator();
		
		for( String term: queryTerms.keySet() ){
			if( index.containsKey(term) ){
				index.get(term).addToResult(acc, queryTerms.get(term));
			}
		}
		
		return acc.getResults();
	}
	
	/**
	 *  Given a boolean query (see the handout for what types of boolean
	 *  queries are valid), return a PostingsList containing the document
	 *  IDs that match the query.  If no documents match, you should still return a
	 *  PostingsList, but it will not have any document ids.
	 * 
	 * @param textQuery
	 * @return
	 */
	public PostingsList booleanQuery(String textQuery){
		ArrayList<Object> process = getPostingsLists(textQuery);
		
		while( process.size() != 1 ){
			// remove the last three entries
			// perform the appropriate operation
			// and add the result back on to the process queue
			
			PostingsList p1 = (PostingsList)process.remove(process.size()-1);
			BooleanQueryEntry connectorEntry = (BooleanQueryEntry)process.remove(process.size()-1);
			PostingsList p2 = (PostingsList)process.remove(process.size()-1);
			
			if( connectorEntry.isAnd() ){
				process.add(PostingsList.andMerge(p1, p2));
			}else{
				process.add(PostingsList.orMerge(p1, p2));
			}
		}
		
		return (PostingsList)process.get(0);
	}
	
	
	/**
	 * Grab all of the query terms and "not" any variables that we need to
	 * the process array will consist of alternating PostingsList entries
	 * and BooleanQueryEntry entries
	 * 
	 * @param textQuery
	 * @return
	 */
	private ArrayList<Object> getPostingsLists(String textQuery){
		ArrayList<BooleanQueryEntry> query = BooleanQueryEntry.processQuery(textQuery);

		
		ArrayList<Object> process = new ArrayList<Object>(query.size());
		
		for( BooleanQueryEntry entry: query ){
			if( entry.isConnector() ){
				process.add(entry);
			}else{
				// get the postings list
				if( entry.isNegated() ){
					if( index.containsKey(entry.getTerm()) ){
						process.add(PostingsList.not(index.get(entry.getTerm()), maxDocID));
					}else{
						process.add(PostingsList.not(new PostingsList(), maxDocID));
					}
				}else{
					if( index.containsKey(entry.getTerm()) ){
						process.add(index.get(entry.getTerm()));
					}else{
						process.add(new PostingsList());
					}
				}
			}
		}
		
		return process;
	}
	
	/**
	 * Read through the documents and generate the term/docID pairs as
	 * the first step in creating the index
	 * 
	 * @param reader
	 * @return
	 */
	private ArrayList<docEntry> collectEntries( DocumentReader reader ){
		// read through the files and generate a long list of
		// docEntries
		ArrayList<docEntry> pairs = new ArrayList<docEntry>();

		while( reader.hasNext() ){
			Document doc = reader.next();
			int docID = doc.getDocID();

			for( String token: doc.getText() ){
				pairs.add(new docEntry(token, docID));
			}

			if( docID % 100 == 0 ){
				System.out.println(docID);
			}

			if( docID > maxDocID ){
				maxDocID = docID;
			}
		}

		return pairs;
	}

	/**
	 * Given a list of docEnty objects generate postings list for these entries
	 * (the second step in creating the index)
	 * 
	 * @param pairs
	 * @return
	 */
	private Hashtable<String,PostingsList> combinePostings(ArrayList<docEntry> pairs){
		// stable sort the pairs by term
		Collections.sort(pairs);

		// create the actual postings list
		Hashtable<String,PostingsList> finalIndex = new Hashtable<String,PostingsList>();

		String prevTerm = "";
		int prevDocID = -1;
		PostingsList currentList = null;
		
		// go through the sorted pairs of docEnty's (i.e.docId, Token) and create the postings
		// lists
		for( docEntry entry: pairs ){
			if( !entry.getToken().equals(prevTerm) ){
				if( currentList != null ){
					finalIndex.put(prevTerm, currentList);
				}

				prevTerm = entry.getToken();
				currentList = new PostingsList();
				currentList.addDoc(entry.getDocID());
			}else if( entry.getDocID() != prevDocID ){
				// add another docID to the postings list
				currentList.addDoc(entry.getDocID());
			}else{
				currentList.incrementTFOfLastEntry(entry.getDocID());
			}

			prevDocID = entry.getDocID();
		}
		
		// add the final postings list
		finalIndex.put(prevTerm, currentList);

		return finalIndex;
	}
	
	/**
	 * Apply any weighting/normalization techniques to the index
	 * 
	 * @param termModifier
	 * @param docModifier
	 * @param lengthModifier
	 */
	private void normalizeIndex(TERM_MODIFIER termModifier, DOC_MODIFIER docModifier, 
			LENGTH_MODIFIER lengthModifier){
		// first, do term normalization
		if( termModifier.equals(TERM_MODIFIER.l) ){
			for( String term: index.keySet() ){
				index.get(term).logNormalizeTermFrequencies();
			}
		}else if( termModifier.equals(TERM_MODIFIER.b) ){
			for( String term: index.keySet() ){
				index.get(term).booleanNormalizeTermFrequencies();
			}
		}
		
		// then, do term/document weighting
		if( docModifier.equals(DOC_MODIFIER.t) ){
			for( String term: index.keySet() ){
				index.get(term).idfWeight(maxDocID+1);
			}
		}
		
		// finally, do any length normalization
		if( lengthModifier.equals(LENGTH_MODIFIER.c) ){
			// first, get all of the sum of the squared of the weights for each document
			double[] docLengths = new double[maxDocID+1];
			
			for( String term: index.keySet() ){
				index.get(term).accumulateDocumentLengths(docLengths);
			}
			
			// then, normalize by the square root of the lengths
			for( int i = 0; i < docLengths.length; i++ ){
				docLengths[i] = 1/Math.sqrt(docLengths[i]);
			}
			
			// finally, apply the weights to the postings lists
			for( String term: index.keySet() ){
				index.get(term).genericWeight(docLengths);
			}
		}
	}

	public String toString(){
		StringBuffer buffer = new StringBuffer();
		
		for( String token : index.keySet() ){
			buffer.append(token + " - " + index.get(token).toString() + "\n");
		}
		
		return buffer.toString();
	}
	
	/**
	 * The docEntry class will be usee to keep track of an entry as we parse through the
	 * text of the form:
	 *     token, docID
	 * 
	 * @author dkauchak
	 *
	 */
	private class docEntry implements Comparable<docEntry>{
		private int docID;
		private String token;

		public docEntry(String token, int docID){
			this.docID = docID;
			this.token = token;
		}


		public int getDocID() {
			return docID;
		}

		public String getToken() {
			return token;
		}

		public int compareTo(docEntry anotherDoc) {
			return token.compareTo(anotherDoc.getToken());
		}

		public String toString(){
			return token + "\t" + docID;
		}
	}
}