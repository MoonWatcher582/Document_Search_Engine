package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 * @author Sesh Venugopal
 * 
 */

class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	public static void main(String[] args) throws FileNotFoundException{
		LittleSearchEngine lse = new LittleSearchEngine();
		/*System.out.println(lse.getKeyWord("Word"));
		System.out.println(lse.getKeyWord("night,"));
		System.out.println(lse.getKeyWord("question??"));
		System.out.println(lse.getKeyWord("Could"));
		System.out.println(lse.getKeyWord("test-case"));*/
		try{
			lse.makeIndex("docs.txt", "noisewords.txt");
		}catch(FileNotFoundException e){
			throw new FileNotFoundException("make sure file exists");
		}
		
	}
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		Scanner sc = new Scanner(new File(docFile));
		HashMap<String,Occurrence> loadedKeyWords = new HashMap<String,Occurrence>();
		while (sc.hasNext()){
			String word = sc.next();
			word = getKeyWord(word);
			if(word != null){
				Occurrence o = loadedKeyWords.get(word);
				if(o != null){
					o.frequency++;
				}else{
					loadedKeyWords.put(word,new Occurrence(docFile,1));
				}
			}
		}
		return loadedKeyWords;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		HashMap<String, Occurrence> map = kws;
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()){
			String s = it.next();
			Occurrence occ = map.get(s);
			ArrayList<Occurrence> occs = keywordsIndex.get(s);
			if(occs != null){
				for(int i = 0; i < occs.size(); i++){
					Occurrence curr = occs.get(i);
					if(curr.document == occ.document){
						curr.frequency++;
						break;
					}else{
						occs.add(occ);
						insertLastOccurrence(occs);
						break;
					}
				}
			}else{
				occs = new ArrayList<Occurrence>();
				occs.add(occ);
				keywordsIndex.put(s, occs);
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		String partWord1 = "";
		String partWord2 = "";
		for(int i = 0; i < word.length(); i++){
			char c = word.charAt(i); 
			if(Character.isLetter(c)){
				partWord1 += c;
			}else{
				partWord2 = word.substring(i);
				for(int j = 0; j < partWord2.length(); j++){
					char d = partWord2.charAt(j);
					if(Character.isLetter(d)){
						return null;
					}
				}
				break;
			}
		}
		if(noiseWords.get(partWord1.toLowerCase()) == null){
			if(partWord1 != ""){
				return partWord1.toLowerCase();
			}else{
				return null;
			}
		}else{
			return null;
		}
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		if(occs.get(0) == null || occs.get(1) == null){
			return null;
		}
		ArrayList<Integer> midpoints = new ArrayList<Integer>();
		int max = occs.size()-2;
		int min = 0;
		int key = occs.size()-1;
		while (max >= min){
			int mid = (int)Math.ceil((max+min)/2);
			midpoints.add(mid);
			if (occs.get(mid).frequency > occs.get(key).frequency){
				min = mid + 1;
			}else if (occs.get(mid).frequency < occs.get(key).frequency){
				max = mid - 1;
			}else if(occs.get(mid).frequency == occs.get(key).frequency){
				min = mid + 1;
			}
	    }
		Occurrence move = occs.get(occs.size()-1);
		occs.remove(occs.size()-1);
		occs.add(max+1,move);
		return midpoints;
	}
	
	//DONE, test
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<String> top5 = new ArrayList<String>();
		ArrayList<Occurrence> occsKw1 = keywordsIndex.get(kw1);
		ArrayList<Occurrence> occsKw2 = keywordsIndex.get(kw2);
		if(occsKw1 == null && occsKw2 == null){
			return null;
		}
		ArrayList<Occurrence> whole = new ArrayList<Occurrence>();
		whole.addAll(occsKw1);
		whole.addAll(occsKw2);
		whole = mergeSort(whole);
		int i = 0, count = 0;
		while(i < whole.size() && count < 5){
			Occurrence curr = whole.get(i);
			String docName = curr.document;
			if(top5.indexOf(docName) == -1){
				top5.add(docName);
				count++;
				i++;
			}else{
				i++;
				continue;
			}
		}
		return top5;
	}
	private ArrayList<Occurrence> mergeSort(ArrayList<Occurrence> unsorted){
		 if(unsorted.size() <= 1){
                 return unsorted;
         }
         ArrayList<Occurrence> sorted = new ArrayList<Occurrence>();
         ArrayList<Occurrence> left = new ArrayList<Occurrence>();
         ArrayList<Occurrence> right = new ArrayList<Occurrence>();
         int middle = unsorted.size()/2;
         for(int i = 0; i < unsorted.size(); i++){
                 if(i < middle){
                         left.add(unsorted.get(i));
                 }else{
                         right.add(unsorted.get(i));
                 }
         }
         left = mergeSort(left); 
         right = mergeSort(right);
         sorted = merge(left, right);
         return sorted;
	}
	private ArrayList<Occurrence> merge(ArrayList<Occurrence> left, ArrayList<Occurrence> right){
        ArrayList<Occurrence> mergedList = new ArrayList<Occurrence>();
        while(left.size() > 0 || right.size() > 0){
                if(left.size() > 0 && right.size() > 0){
                        if(left.get(0).frequency > right.get(0).frequency){
                                mergedList.add(left.get(0));
                                left.remove(0);
                        }else{
                        	mergedList.add(right.get(0));
                            right.remove(0);
                        }
                }else if(left.size() > 0){
                	mergedList.add(left.get(0));
                    left.remove(0);
                }else if(right.size() > 0){
                    mergedList.add(right.get(0));
                    right.remove(0);
                }
        }
        return mergedList;
	}
}