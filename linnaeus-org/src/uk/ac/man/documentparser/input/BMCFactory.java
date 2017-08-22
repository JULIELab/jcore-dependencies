package uk.ac.man.documentparser.input;

import java.io.File;

public class BMCFactory implements InputFactory {

	private String[] dtdLocation;
	
	public BMCFactory(String[] dtd){
		this.dtdLocation = dtd;
	}
	
	public DocumentIterator parse(String file) {
		return new BMC(file,dtdLocation);
	}
	public DocumentIterator parse(StringBuffer data) {
		return new BMC(data,dtdLocation);
	}

	public DocumentIterator parse(File file) {
		return new BMC(file,dtdLocation);
	}

}
