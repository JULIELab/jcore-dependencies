package uk.ac.man.documentparser.input;

import java.io.File;

public class TextFileFactory implements InputFactory {

	public TextFileFactory(){
	}

	public DocumentIterator parse(StringBuffer data) {
		throw new IllegalStateException("not implemented");
	}

	public DocumentIterator parse(String file) {
		return parse(new File(file));
	}

	public DocumentIterator parse(File file) {
		return new TextFile(new File[] {file});
	}

}
