package uk.ac.man.documentparser.input;

import java.io.File;

public class MedlineFactory implements InputFactory {

	public MedlineFactory(){
	}
	
	public DocumentIterator parse(StringBuffer data) {
		return new Medline(data);
	}

	public DocumentIterator parse(String file) {
		return new Medline(file);
	}

	public DocumentIterator parse(File file) {
		return new Medline(file);
	}
}
