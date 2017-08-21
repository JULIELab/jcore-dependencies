package uk.ac.man.documentparser.input;

import java.io.File;

public class OTMIFactory implements InputFactory {

	public OTMIFactory(){
	}

	public DocumentIterator parse(StringBuffer data) {
		throw new IllegalStateException("not implemented");
	}

	public DocumentIterator parse(String file) {
		return parse(new File(file));
	}

	public DocumentIterator parse(File file) {
		return new OTMI(file);
	}
}
