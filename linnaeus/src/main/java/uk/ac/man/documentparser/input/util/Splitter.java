package uk.ac.man.documentparser.input.util;

import martin.common.Pair;
import martin.common.SentenceSplitter;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.input.DocumentIterator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Adaptor Class for splitting documents on x number of sentences
 * @author Martin
 */
public class Splitter implements DocumentIterator {
	private int sentencesPerSplit;
	private DocumentIterator documents;
	private LinkedList<String> currentTexts = new LinkedList<String>();
	private LinkedList<String> currentIDs;

	/**
	 * Create a documentiterator adaptor, which will return documents, with a maximum of "sentencesPerSplit" sentences
	 * Uses martin.common.SentenceSplitter.
	 * @param documents
	 * @param sentencesPerSplit
	 */
	public Splitter(DocumentIterator documents, int sentencesPerSplit){
		this.sentencesPerSplit = sentencesPerSplit;
		this.documents = documents;
		getNext();
	}

	private void getNext() {
		LinkedList<String> resTexts = new LinkedList<String>();
		LinkedList<String> resIDs = new LinkedList<String>();
		
		Document d = documents.next();
		String text = d.toString();
		
		SentenceSplitter ssp = new SentenceSplitter(text);
		
		StringBuffer sb = new StringBuffer();
		int c = 0;
		int x = 0;
		String currentID = d.getID() + "." + (x++) + ".0"; 
		for (Pair<Integer> p : ssp){
			sb.append(text.substring(p.getX(),p.getY()));
			c++;
			if (c == this.sentencesPerSplit){
				resTexts.add(sb.toString());
				resIDs.add(currentID);
				
				currentID = d.getID() + "." + (x++) + "." + p.getY();
				sb = new StringBuffer();
				c = 0;
			}
		}
		if (c > 0){
			resTexts.add(sb.toString());
			resIDs.add(currentID);
		}
		
		this.currentTexts = resTexts;
		this.currentIDs = resIDs;
	}

	@Override
	public void skip() {
		if (!hasNext())
			throw new NoSuchElementException();
		if (this.currentTexts.size() == 0)
			getNext();
		currentTexts.removeFirst();
	}

	@Override
	public boolean hasNext() {
		return currentTexts != null && (currentTexts.size() > 0 || documents.hasNext()); 
	}

	@Override
	public Document next() {
		if (!hasNext())
			throw new NoSuchElementException();
		
		if (currentTexts.size() == 0)
			getNext();
		
		String t = currentTexts.removeFirst();
		String id = currentIDs.removeFirst();
		
		return new Document(id,null,null,null,t,null,null,null,null,null,null,null,null,null,null);
	}

	@Override
	public void remove() {
		throw new IllegalStateException();
	}

	@Override
	public Iterator<Document> iterator() {
		return this;
	}
}
