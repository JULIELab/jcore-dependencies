package uk.ac.man.documentparser.input.util;

import java.util.Iterator;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.input.DocumentIterator;

public class CleanUnicode implements DocumentIterator {

	private DocumentIterator documents;

	public CleanUnicode(DocumentIterator documents){
		this.documents = documents;
	}

	@Override
	public void skip() {
		documents.skip();
	}

	@Override
	public boolean hasNext() {
		return documents.hasNext();
	}

	@Override
	public Document next() {
		Document d = documents.next();

		d.setTitle(clean(d.getTitle()));
		d.setAbs(clean(d.getAbs()));
		d.setBody(clean(d.getBody()));
		d.setRawContent(clean(d.getRawContent()));

		return d;
	}

	private String clean(String s){
		if (s == null)
			return null;
		
		StringBuffer sb = new StringBuffer(s);
		
		for (int i = 0; i < sb.length(); i++){
			if (sb.codePointAt(i) == 8211 || sb.codePointAt(i) == 157)
				sb.setCharAt(i, '-');
			if (sb.codePointAt(i) < 32 && sb.codePointAt(i) != 10)
				sb.setCharAt(i, ' ');
			if (sb.codePointAt(i) > 126)
				sb.setCharAt(i, ' ');
		}
		
		return sb.toString();
	}
	
	@Override
	public void remove() {
		documents.remove();
	}

	@Override
	public Iterator<Document> iterator() {
		return this;
	}
}
