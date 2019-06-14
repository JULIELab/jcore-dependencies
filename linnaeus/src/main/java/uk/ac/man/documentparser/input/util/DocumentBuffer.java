package uk.ac.man.documentparser.input.util;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.input.DocumentIterator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

public class DocumentBuffer implements DocumentIterator {
	
	private DocumentIterator documents;
	private LinkedList<Document> retrieved;
	private int fetchsize;
	private boolean hasNext;
	private Logger logger;

	public DocumentBuffer(DocumentIterator documents, int fetchsize, Logger logger){
		this.documents = documents;
		this.retrieved = new LinkedList<Document>();
		this.fetchsize = fetchsize;
		this.hasNext = retrieved.size() > 0 || documents.hasNext();
		this.logger = logger;
	}

	public void skip() {
		if (retrieved.size() == 0)
			documents.skip();
		else
			retrieved.poll();
	}

	public boolean hasNext() {
		return hasNext;
	}

	public Document next() {
		if (!hasNext())
			throw new NoSuchElementException();
		
		if (retrieved.size() == 0)
			populateRetrieved();
		
		Document d = retrieved.poll();
		this.hasNext = retrieved.size() > 0 || documents.hasNext();
		
		return d;
	}

	private void populateRetrieved() {
		//logger.info("%t: Retrieving " + fetchsize + " documents...\n");
		for (int i = 0; i < fetchsize; i++)
			if (documents.hasNext())
				retrieved.add(documents.next());
			else
				break;
		//logger.info("%t: Done, retrieved " + retrieved.size() + " documents.\n");
	}

	public void remove() {
		throw new IllegalStateException();
	}

	public Iterator<Document> iterator() {
		return this;
	}
}
