package uk.ac.man.documentparser.misc;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.input.Medline;

import martin.common.ArgParser;

public class MedlineIndexer {
	public static void compute(File file, String filename, Set<String> completedIDs) {
		try {
			BufferedReader inStream = new BufferedReader(new FileReader(file));
			
			String line = inStream.readLine();
			int start = 0;
			int end = 0;
			int passed = 0;
			StringBuffer doc=null;
			
			final String startTag = "<MedlineCitation Owner=";
			final String endTag = "</MedlineCitation>";
			final String startXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MedlineCitationSet>";
			final String endXML = "</MedlineCitationSet>";
			
			while (line != null){
				int localstart = 0;
				
				if (line.indexOf(startTag, localstart) != -1){
					localstart = line.indexOf(startTag, localstart);
					start = passed + localstart;
					doc = new StringBuffer();

					while (!line.contains(endTag)){
						passed += line.length();
						doc = doc.append(line.substring(localstart));
						localstart = 0;
						line = inStream.readLine();
					}
					
					end = line.indexOf(endTag) + endTag.length();
					doc = doc.append(line.substring(0,end));
					end += passed;
					
					doc = new StringBuffer(startXML + doc.toString() + endXML);
										
					Document d = new Medline(doc).next();
					
					int hasAbstract = d.getAbs() != null ? 1 : 0;
					String ISSN = d.getJournal() != null && d.getJournal().getISSN() != null ? d.getJournal().getISSN() : "-";
					String journalTitle = d.getJournal() != null && d.getJournal().getTitle() != null ? d.getJournal().getTitle() : "-";
					String journalTitleAbbrev = d.getJournal() != null && d.getJournal().getAbbrev() != null ? d.getJournal().getAbbrev() : "-";

					if (!completedIDs.contains(d.getID())){
						System.out.println(d.getID() + "\t" + filename + "\t" + start + "\t" + end + "\t" + d.getYear() + "\t" + hasAbstract + "\t" + ISSN + "\t" + journalTitle + "\t" + journalTitleAbbrev);
						completedIDs.add(d.getID());
					}
				}
				
				passed += line.length();
				line = inStream.readLine();
			}
			
			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void run(File dir, int ignoreNumChars, Set<String> completedIDs){
		File[] contents = dir.listFiles();
		Arrays.sort(contents);
		
		for (int i = 0; i < contents.length; i++){
			if (contents[i].isDirectory())
				run(contents[i], ignoreNumChars, completedIDs);
			else
				compute(contents[i], contents[i].getAbsolutePath().substring(ignoreNumChars), completedIDs);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);

		if (args.length == 0 || ap.containsKey("help")){
			System.out.println("Usage:");
			System.out.println("medlineindexer.jar --medlineDir <base dir>");
			System.exit(-1);
		}

		File dir = ap.getFile("medlineDir");

		System.out.println("#PMID\tfile\tstart\tend\tpublication year\tabstract presence\tjournal");
		
		run(dir, dir.getAbsolutePath().length()+1, new HashSet<String>());
	}
}
