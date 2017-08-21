package uk.ac.man.documentparser.input;

import java.io.File;
import java.util.Set;

public class MedlinePMCIndexFactory implements InputFactory {

	File medlineBaseDir;
	private File pmcBaseDir;

	private int minYearFilter=-1;
	private Set<String> validIDs = null;
	private String[] dtds;

	public MedlinePMCIndexFactory(File medlineBaseDir, File pmcBaseDir, String[] dtds, Set<String> validIDs){
		this.medlineBaseDir = medlineBaseDir;
		this.pmcBaseDir = pmcBaseDir;
		this.dtds = dtds;
		this.validIDs = validIDs;
	}

	public MedlinePMCIndexFactory(File baseDir){
		this.medlineBaseDir = baseDir;
	}

	public DocumentIterator parse(StringBuffer data) {
		throw new IllegalStateException("not implemented");
	}

	public DocumentIterator parse(String file) {
		return parse(new File(file));
	}

	public DocumentIterator parse(File file) {
		return new MedlinePMCIndex(medlineBaseDir,pmcBaseDir,file,dtds, minYearFilter, validIDs);
	}

	public void setMinYearFilter(int minYearFilter) {
		this.minYearFilter = minYearFilter;
	}
}
