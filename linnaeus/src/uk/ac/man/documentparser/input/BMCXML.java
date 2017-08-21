package uk.ac.man.documentparser.input;

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;

import uk.ac.man.documentparser.dataholders.Document;

public class BMCXML implements DocumentIterator {
	private File file;
	
	private String[] validTextLocations = new String[]{
			"art/fm/abs",
			"art/bdy",
			"art/fm/bibl/title"
	};
	
	//private String idstart = "<pubid idtype=\"doi\">";
	//private String idend = "</pubid>";
	private String idstart = "<ui>";
	private String idend = "</ui>";

	
	public BMCXML(File file){
		this.file = file;
	}

	public BMCXML(@SuppressWarnings("unused") StringBuffer data) {
		throw new IllegalStateException("not implemented");
	}

	public void skip() {
		file = null;
	}

	public boolean hasNext() {
		return file != null;
	}

	public Document next() {
		if (file == null)
			throw new NoSuchElementException();
		
		Document d = null; //TODO: fix
		
		file = null;
		
		return d; 
	}

	public void remove() {
		throw new IllegalStateException("not implemented");

	}

	public Iterator<Document> iterator() {
		return this;
	}

}
