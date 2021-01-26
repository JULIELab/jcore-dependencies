package uk.ac.man.documentparser.input;

import uk.ac.man.documentparser.dataholders.Document;

import java.util.Iterator;

public interface DocumentIterator extends Iterator<Document>, Iterable<Document>{
	public void skip();
}
