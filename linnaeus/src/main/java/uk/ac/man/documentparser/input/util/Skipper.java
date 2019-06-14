package uk.ac.man.documentparser.input.util;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.input.DocumentIterator;

import java.util.Iterator;

public class Skipper implements DocumentIterator{
	private DocumentIterator docs;
	private int skip;
	private boolean hasNext;
	private Document next;

	public Skipper(DocumentIterator docs, int skip){
		this.docs = docs;
		this.skip = skip;
		fetchNext();
	}

	private void fetchNext() {
		hasNext = docs.hasNext();
		
		if (hasNext){
		next = docs.next();

		for (int i = 0; i < skip; i++)
			if (docs.hasNext())
				docs.skip();
		}
	}

	public void skip() {
		fetchNext();		
	}

	public boolean hasNext() {
		return hasNext;
	}

	public Document next() {
		Document r = next;
		fetchNext();
		return r;
	}

	public void remove() {
		throw new IllegalStateException();
	}

	public Iterator<Document> iterator() {
		return this;
	}
}
