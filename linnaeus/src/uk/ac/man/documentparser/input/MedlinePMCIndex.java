package uk.ac.man.documentparser.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;

import uk.ac.man.documentparser.dataholders.Document;

public class MedlinePMCIndex implements DocumentIterator {
	private File medlineBaseDir;
	private BufferedReader indexStream;

	private String currentFile;
	private BufferedReader currentFileStream;
	private int currentFilePassed;
	private String currentLine;

	private final String startXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MedlineCitationSet>";
	private final String endXML = "</MedlineCitationSet>";

	private String nextEntry;

	private int minYearFilter;
	private Set<String> validIDs;
	private File pmcBaseDir;
	private String[] dtds;

	public MedlinePMCIndex(File medlineBaseDir, File pmcBaseDir, File indexFile, String[] dtds, int minYearFilter, Set<String> validIDs){
		this.medlineBaseDir = medlineBaseDir;
		this.pmcBaseDir = pmcBaseDir;
		this.dtds = dtds;
		
		this.minYearFilter = minYearFilter;
		this.validIDs = validIDs;

		try{
			this.indexStream = new BufferedReader(new FileReader(indexFile));
		} catch (FileNotFoundException e){
			System.err.println("The file " + indexFile.getAbsolutePath() + " could not be found. Exiting.");
			System.exit(-1);
		}
		
		fetchNextLine();
	}

	public Iterator<Document> iterator() {
		return this;
	}

	private void fetchNextLine(){
		try {

			boolean read = true;

			while (read){
				nextEntry = indexStream.readLine();

				read = false;

				if (nextEntry != null){
					String[] fields = nextEntry.split(",");

					if (nextEntry.startsWith("#")){
						read = true;
					} else if ((minYearFilter != -1) && (minYearFilter > Integer.parseInt(fields[12]))){
						read = true;
					} else if (validIDs != null && !validIDs.contains(fields[0])){
						read = true;
					}
				}
			}
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public Document next() {
		Document doc = null;
		try {
			if (nextEntry != null){
				String[] fields = nextEntry.split(",");

				String medlinefile = fields[2];
				int start = Integer.parseInt(fields[3]);
				int end = Integer.parseInt(fields[4]);

				if (!medlinefile.equals(currentFile)){
					if (currentFileStream != null)
						currentFileStream.close();

					this.currentFile = medlinefile;
					this.currentFileStream = new BufferedReader(new FileReader(new File(medlineBaseDir,medlinefile)));
					this.currentFilePassed = 0;
					this.currentLine = currentFileStream.readLine();
				}

				while (start >= currentFilePassed + currentLine.length()){
					currentFilePassed += currentLine.length();
					currentLine = currentFileStream.readLine();
				}

				StringBuffer data = new StringBuffer(startXML);
				int localStart = Math.max(start - currentFilePassed,0);
				//int localEnd = Math.min(end - currentFilePassed, currentLine.length());

				while (end > currentFilePassed + currentLine.length()){
					data.append(currentLine.substring(localStart));
					currentFilePassed += currentLine.length();
					localStart = Math.max(start - currentFilePassed,0);
					currentLine = currentFileStream.readLine();
				}

				data.append(currentLine.substring(localStart, end - currentFilePassed));
				data.append(endXML);

				doc = new Medline(data).next();
				Document pmcDoc = new PMC(new File(pmcBaseDir,fields[5]).getAbsolutePath(),dtds,fields[1],fields[6].equals("1"),fields[7].equals("1"),fields[8].equals("1"),fields[9].equals("1")).next();
				
				doc.setBody(pmcDoc.getBody());
				doc.setRawContent(pmcDoc.getRawContent());
				doc.setRaw_type(pmcDoc.getRaw_type());

				fetchNextLine();

			} else {
				indexStream.close();
			}
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return doc;
	}

	public boolean hasNext() {
		return nextEntry != null;
	}

	public void remove() {
		throw new IllegalStateException("Not implemented.");
	}

	public void skip() {
		fetchNextLine();
	}
}
