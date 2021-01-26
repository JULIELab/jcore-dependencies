package uk.ac.man.documentparser.input;

import java.io.File;
import java.util.Set;

public class MedlineIndexFactory implements InputFactory {
	
	private File medlineBaseDir;
	
	private Set<String> validIDs = null;
	
	public MedlineIndexFactory(File medlineBaseDir, Set<String> validIDs){
		this.medlineBaseDir = medlineBaseDir;
		this.validIDs = validIDs;
	}
	
	public MedlineIndexFactory(File baseDir){
		this.medlineBaseDir = baseDir;
	}
	
	public DocumentIterator parse(StringBuffer data) {
		throw new IllegalStateException("not implemented");
	}

	public DocumentIterator parse(String file) {
		return parse(new File(file));
	}

	public DocumentIterator parse(File file) {
		return new MedlineIndex(medlineBaseDir,file, validIDs);
	}
}
