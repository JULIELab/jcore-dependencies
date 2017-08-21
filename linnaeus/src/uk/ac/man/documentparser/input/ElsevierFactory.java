package uk.ac.man.documentparser.input;

import java.io.File;

public class ElsevierFactory implements InputFactory {
	private String[] dtdLocations;
	
	public ElsevierFactory(String[] dtdLocations){
		this.dtdLocations = dtdLocations;
	}
	
	public DocumentIterator parse(String file) {
		return new Elsevier(new File(file),dtdLocations);
	}

	public DocumentIterator parse(File file) {
		return new Elsevier(file,dtdLocations);
	}

	public DocumentIterator parse(StringBuffer data) {
		return new Elsevier(data,dtdLocations);
	}
}
