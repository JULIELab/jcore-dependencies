package uk.ac.man.documentparser.input;

import java.io.File;

public class PMCIndexFactory implements InputFactory {
	
	private File pmcBaseDir;
	private String[] dtds;
	
	public PMCIndexFactory(File pmcBaseDir, String[] dtds){
		this.pmcBaseDir = pmcBaseDir;
		this.dtds = dtds;
	}
	
	public DocumentIterator parse(StringBuffer data) {
		throw new IllegalStateException("not implemented");
	}

	public DocumentIterator parse(String file) {
		return parse(new File(file));
	}

	public DocumentIterator parse(File file) {
		return new PMCIndex(pmcBaseDir,file,dtds);
	}
}
