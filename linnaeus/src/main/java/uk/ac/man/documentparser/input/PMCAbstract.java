package uk.ac.man.documentparser.input;

import uk.ac.man.documentparser.dataholders.Document;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PMCAbstract implements DocumentIterator {
	private DocumentIterator pmcDocs;
	private DocumentIterator medlineDocs;

	int lastReturned = -1;
	
	public PMCAbstract(DocumentIterator pmcDocs, DocumentIterator medlineDocs){
		this.pmcDocs = pmcDocs;
		this.medlineDocs = medlineDocs;
	}
	
	public void skip() {
		if (pmcDocs != null && pmcDocs.hasNext())
			pmcDocs.skip();
		else if (medlineDocs != null && medlineDocs.hasNext())
			medlineDocs.skip();
		else
			throw new NoSuchElementException();
	}

	public boolean hasNext() {
		return (pmcDocs != null && pmcDocs.hasNext()) || (medlineDocs != null && medlineDocs.hasNext());
	}

	public Iterator<Document> iterator() {
		return this;
	}

	public Document next() {
		if (pmcDocs != null && pmcDocs.hasNext()){
			Document d = pmcDocs.next();
			lastReturned = 0;
			
			return new Document(d.getID(), d.getTitle(), d.getAbs(), null, null, null, d.getYear(), d.getJournal(), d.getType(), d.getAuthors(), d.getVolume(), d.getIssue(), d.getPages(), null, null);
		}
		
		if (medlineDocs != null && medlineDocs.hasNext()){
			lastReturned = 1;
			return medlineDocs.next();			
		}
		
		throw new NoSuchElementException();
	}

	public void remove() {
		if (lastReturned == -1)
			throw new NoSuchElementException();
		else if (lastReturned == 0)
			pmcDocs.remove();
		else if (lastReturned == 1)
			medlineDocs.remove();
		
		lastReturned = -1;
	}
}
