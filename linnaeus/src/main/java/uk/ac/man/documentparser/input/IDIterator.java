package uk.ac.man.documentparser.input;

import martin.common.StreamIterator;
import uk.ac.man.documentparser.dataholders.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class IDIterator implements DocumentIterator {

	private int nextItem;
	private ArrayList<String> list;

	public IDIterator(File f) {
		this.list = new ArrayList<String>();
		this.nextItem = 0;
		
		for (String l : new StreamIterator(f))
			list.add(l);
	}

	@Override
	public void skip() {
		nextItem++;
	}

	@Override
	public boolean hasNext() {
		return nextItem < list.size();
	}

	@Override
	public Document next() {
		String id = list.get(nextItem++);
		return new Document(id,null,null,null,null,null,null,null,null,null,null,null,null,null,null);
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
