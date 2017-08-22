package uk.ac.man.documentparser.input;

import java.io.File;

public class PMCFactory implements InputFactory {
	private String[] dtdLocations;
	
	public PMCFactory(String[] dtdLocations){
		this.dtdLocations = dtdLocations;
	}
	
	public DocumentIterator parse(String file) {
		return new PMC2(new File(file),dtdLocations);
	}

	public DocumentIterator parse(File file) {
		return new PMC2(file,dtdLocations);
	}

	public DocumentIterator parse(StringBuffer data) {
		return new PMC2(data,dtdLocations);
	}
}
