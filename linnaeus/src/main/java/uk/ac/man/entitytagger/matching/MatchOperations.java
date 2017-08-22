package uk.ac.man.entitytagger.matching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import martin.common.Function;
import martin.common.Misc;
import martin.common.Pair;
import martin.common.compthreads.IteratorBasedMaster;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.input.DocumentIterator;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.doc.TaggedDocument;
import uk.ac.man.entitytagger.doc.TaggedDocument.Format;
import uk.ac.man.entitytagger.matching.matchers.ConcurrentMatcher;

public class MatchOperations {

	public static void runToDir(Matcher matcher, DocumentIterator documents, int numThreads, int report, File outDir, Logger logger){
		ConcurrentMatcher tm = new ConcurrentMatcher(matcher,documents);
		IteratorBasedMaster<TaggedDocument> master = new IteratorBasedMaster<TaggedDocument>(tm,numThreads);
		new Thread(master).start();

		int numNullDocuments = 0;

		try{

			int counter = 0; 

			while (master.hasNext()){
				TaggedDocument td = master.next();
				if (td != null){

					String id = td.getOriginal().getID();

					ArrayList<Mention> matches = td.getAllMatches();
					Mention.saveToFile(matches, new File(outDir,id+".tags"));
					/*BufferedWriter outStream = new BufferedWriter(new FileWriter(new File(outDir,id+".tags")));
					outStream.write("#species,document,start,end,text,extra\n");

					ArrayList<Match> matches = td.getAllMatches();
					for (int i = 0; i < matches.size(); i++){
						outStream.write(matches.get(i).toString() + "\n");
					}

					outStream.close();*/
				} else {
					numNullDocuments++;
				}

				if (report != -1 && ++counter % report == 0)
					logger.info("%t: Tagged " + counter + " documents.\n");
			}
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		if (numNullDocuments > 0)
			logger.warning("Number of null documents: " + numNullDocuments + "\n");
		logger.info("%t: Completed.");
	}

	public static void runHTML(Matcher matcher, DocumentIterator documents, int numThreads, File htmlFile, int report, Logger logger, Format format, boolean link, Function<Pair<String>> alternativeTagFunction) {

		ConcurrentMatcher tm = new ConcurrentMatcher(matcher,documents);
		IteratorBasedMaster<TaggedDocument> master = new IteratorBasedMaster<TaggedDocument>(tm,numThreads);
		new Thread(master).start();

		int numNullDocuments = 0;

		logger.info("%t: Starting HTML tagging.\n");

		try {
			BufferedWriter outStream = new BufferedWriter(new FileWriter(htmlFile));

			if (format == Format.HTML)
				outStream.write("<html><body link=\"black\" alink=\"black\" vlink=\"black\" hlink=\"black\">\n");

			int counter = 0;

			while (master.hasNext()){
				TaggedDocument td = master.next();

				if (td != null){
					if (format == Format.HTML){
						if (td.getAllMatches().size() > 0){
							outStream.write("<b><a href=\"http://130.88.91.13/cgi-bin/search?q=" + td.getOriginal().getID() + "\">" + td.getOriginal().getID() + "</a></b><br>\n");

							String str = td.toHTML(link, alternativeTagFunction).toString();
							str = str.replace("\n", "<br>");

							outStream.write(str);
							outStream.flush();

							outStream.write("<p><hr><p>");
						}
					} else if (format == Format.XMLTags){
						outStream.write(TaggedDocument.toHTML(td.getContent(), td.getRawMatches(), Format.XMLTags, link, alternativeTagFunction).toString());
						outStream.flush();
					} else {
						throw new IllegalStateException("should not have reached this stage");
					}
				} else {
					numNullDocuments++;
				}

				if (report != -1 && ++counter % report == 0)
					logger.info("%t: Tagged " + counter + " documents.\n");
			}

			if (format == Format.HTML)
				outStream.write("</body></html>");
			outStream.close();

			logger.info("%t: HTML tagging completed.\n");

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}		

		if (numNullDocuments > 0)
			logger.warning("Number of null documents: " + numNullDocuments + "\n");
		logger.info("%t: Completed.");
	}

	public static void runToFile(Matcher matcher, DocumentIterator documents, int numThreads, int report, File outFile, Logger logger){
		ConcurrentMatcher tm = new ConcurrentMatcher(matcher,documents);
		IteratorBasedMaster<TaggedDocument> master = new IteratorBasedMaster<TaggedDocument>(tm,numThreads);
		new Thread(master).start();

		int numNullDocuments = 0;

		logger.info("%t: Tagging...\n");

		try{
			BufferedWriter outStream = new BufferedWriter(new FileWriter(outFile));

			outStream.write("#entity\tdocument\tstart\tend\ttext\tcomment\n");

			int counter = 0; 

			while (master.hasNext()){
				TaggedDocument td = master.next();
				ArrayList<Mention> matches = td.getAllMatches();
				if (matches != null){
					matches = Misc.sort(matches);
					for (Mention m : matches){
						outStream.write(m.toString()+ "\n");
					}
				} else {
					numNullDocuments++;
					logger.warning("null document," + td.getOriginal().getID() + "\n");
				}

				if (report != -1 && ++counter % report == 0)
					logger.info("%t: Tagged " + counter + " documents.\n");

				outStream.flush();
			}

			logger.info("%t: Tagging completed.\n");

			outStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		if (numNullDocuments > 0)
			logger.warning("Number of null documents: " + numNullDocuments + "\n");
		logger.info("%t: Completed.\n");
	}

	public static TaggedDocument matchDocument(Matcher matcher, Document doc){
		String rawText = doc.toString();

		List<Mention> matches = matcher.match(rawText, doc);

		if (matches == null)
			return new TaggedDocument(doc,null,null,matches,rawText);

		for (Mention m : matches){
			m.setDocid(doc.getID());
		}

		if (doc.isIgnoreCoordinates()){
			for (int i = 0; i < matches.size(); i++){
				Mention m = matches.get(i);
				m.setStart(-1);
				m.setEnd(-1);
			}
		}

		return new TaggedDocument(doc,null,null,matches,rawText);
	}

	private static PreparedStatement initDBTable(Connection dbConn, String table, Logger logger, boolean clear) {
		try{

			Statement stmt = (Statement) dbConn.createStatement();

			if (clear){
				logger.info("%t: Creating tables...");
				stmt.execute("DROP TABLE IF EXISTS `" + table + "`;");
				stmt.execute("CREATE TABLE  `" + table + "` (" +
						"`entity` varchar(255) default NULL," +
						"`document` varchar(255) default NULL,"+
						"`start` int(32) default NULL,"+
						"`end` int(32) default NULL,"+
						"`text` varchar(255) default NULL,"+
						"`comment` varchar(255) default NULL,"+
						"KEY `index_0` (`document`)"+
				") ENGINE=MyISAM DEFAULT CHARSET=utf8;");
				logger.info(" done.\n");
			}

			PreparedStatement pstmt = dbConn.prepareStatement("REPLACE INTO " + table + " (entity,document,start,end,text,comment) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

			return pstmt;
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	public static void runDB(Matcher matcher, DocumentIterator documents, int numThreads, String table, int report, Logger logger, Connection dbConn, boolean clear){
		ConcurrentMatcher tm = new ConcurrentMatcher(matcher,documents);
		IteratorBasedMaster<TaggedDocument> master = new IteratorBasedMaster<TaggedDocument>(tm,numThreads);
		new Thread(master).start();

		PreparedStatement pstmt = initDBTable(dbConn, table, logger, clear);

		try{
			int c = 0;
			int d = 0;

			while (master.hasNext()){
				TaggedDocument td = master.next();
				ArrayList<Mention> matches = td.getAllMatches();
				if (matches != null){
					for (Mention m : matches){
						m.addToPstmtBatch(pstmt);
						d++;

						if (d % 100 == 0)
							pstmt.executeBatch();
					}
				}

				if (report != -1 && ++c % report == 0)
					logger.info("%t: Tagged " + c + " documents.\n");
			}

			pstmt.executeBatch();
			pstmt.close();

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		logger.info("%t: Completed.");
	}

	public static void runHTML(Matcher matcher, DocumentIterator documents, int numThreads, File htmlFile, int report, Logger logger, Format format) {
		runHTML(matcher, documents, numThreads, htmlFile, report, logger, format, true);
	}

	public static void runHTML(Matcher matcher, DocumentIterator documents, int numThreads, File htmlFile, int report, Logger logger, Format format, boolean link) {
		runHTML(matcher, documents, numThreads, htmlFile, report, logger, format, link, null);
	}

	public static void runOutWithContext(Matcher matcher,
			DocumentIterator documents, int numThreads, int report,
			File file, Logger logger, int preLength, int postLength) {

		ConcurrentMatcher cMatcher = new ConcurrentMatcher(matcher, documents);
		IteratorBasedMaster<TaggedDocument> master = new IteratorBasedMaster<TaggedDocument>(cMatcher, numThreads);
		master.startThread();

		int c = 0;

		try{
			BufferedWriter outStream = new BufferedWriter(new FileWriter(file));
			outStream.write("#entity\tdocument\tstart\tend\ttext\tcomment\tpre\tpost\n");
			logger.info("%t: Tagging...\n");
			for (TaggedDocument td : master){
				Document d = td.getOriginal();
				String text = d.toString();
				List<Mention> matches = td.getAllMatches();

				for (Mention m : matches){
					int s = m.getStart();
					int e = m.getEnd();

					String pre = text.substring(Math.max(0, s-preLength), Math.min(text.length(), s)).replace('\n', ' ').replace('\r',' ');
					//String term = text.substring(Math.max(0,s),Math.min(text.length(), e));
					String post = text.substring(Math.max(0,e), Math.min(text.length(), e+postLength)).replace('\n', ' ').replace('\r',' ');

					outStream.write(m.toString() + "\t" + pre + "\t" + post + "\n");
				}

				outStream.flush();

				if (report != -1 && ++c % report == 0)
					logger.info("%t: Processed " + c + " documents.\n");
			}	
			logger.info("%t: Completed.");
			outStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
