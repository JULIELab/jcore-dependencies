package uk.ac.man.documentparser.input;

import java.io.File;

public class BMCXMLFactory implements InputFactory {

	public DocumentIterator parse(String file) {
		return new BMCXML(new File(file));
	}

	public DocumentIterator parse(File file) {
		return new BMCXML(file);
	}

	public DocumentIterator parse(StringBuffer data) {
		return new BMCXML(data);
	}

}
