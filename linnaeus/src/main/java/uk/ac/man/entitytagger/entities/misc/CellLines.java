package uk.ac.man.entitytagger.entities.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;

import uk.ac.man.entitytagger.EntityTagger;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.generate.DictionaryEntry;
import uk.ac.man.entitytagger.generate.GenerateMatchers;
import uk.ac.man.entitytagger.matching.Matcher;

import martin.common.ArgParser;
import martin.common.Loggers;
import martin.common.SQL;
import martin.common.xml.EntityResolver;
import martin.common.xml.MyNodeList;
import martin.common.xml.XPath;

/**
 * This class will, given a CLKB CellLine database .owl file, parse its contents and output a tab-delimited
 * regular expression file suitable for import by the automaton generating software. Using it, an automaton can be 
 * generated that may be used to locate and identify cell lines.  
 * @author Martin Gerner
 */
public class CellLines {
	private static Node parseXML(File file, String[] dtdLocations){
		try{ 
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			if (dtdLocations != null)
				db.setEntityResolver(new EntityResolver(dtdLocations));

			Node root = db.parse(file);
			root = XPath.getNode("rdf:RDF", root);
			return root;
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	public static void main(String[] args){
		ArgParser ap = new ArgParser(args);

		Logger logger = Loggers.getDefaultLogger(ap);
		File inFile = ap.getFile("in");
		File outFile = ap.getFile("out");
		Connection conn = SQL.connectMySQL(ap, logger, "dictionaries");

		Matcher matcher = EntityTagger.getMatcher(ap, logger);
		Node root = parseXML(inFile,null);

		run(root, outFile, matcher, logger, conn, ap.get("table"));	
	}

	private static String match(String organism, Matcher matcher){
		List<Mention> matches = matcher.match(organism);

		if (matches.size() == 0)
			return "";

		Set<String> s = new HashSet<String>();

		for (Mention m : matches)
			s.add(m.getMostProbableID());

		String res = "";

		for (String str : s)
			if  (res.length() == 0)
				res += str;
			else
				res += "|" + str;

		return res;
	}

	private static void run(Node root, File outFile, Matcher matcher, Logger logger, Connection conn, String table) {
		try{
			logger.info("%t: Running...\n");

			MyNodeList CLDBs = XPath.getNodeList("CLDB", root);
			MyNodeList ATCCs = XPath.getNodeList("ATCC", root);
			MyNodeList CellLine = XPath.getNodeList("CellLine", root);

			logger.info("%t: #CLDBs: " + CLDBs.getLength() + "\n");
			logger.info("%t: #ATCCs: " + ATCCs.getLength() + "\n");
			logger.info("%t: #CellLines: " + CellLine.getLength() + "\n");

			BufferedWriter outStream = outFile != null ? new BufferedWriter(new FileWriter(outFile)) : null;
			if (outStream != null)
				outStream.write("#id\tname\torganism\ttissue\n");

			PreparedStatement pstmt = conn != null ? GenerateMatchers.initVariantTable(conn, table, true) : null;

			for (Node n : CLDBs){
				Node cl = XPath.getNode("isRepositoryFor/CellLine", n);
				run2(cl, pstmt, outStream, matcher);
			}

			for (Node cl : CellLine){
				run2(cl, pstmt, outStream, matcher);
			}

			for (Node n : ATCCs){
				Node cl = XPath.getNode("isRepositoryFor/CellLine", n);
				run2(cl, pstmt, outStream, matcher);
			}

			if (outStream != null)
				outStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}		
	}

	private static void run2(Node cl, PreparedStatement pstmt, BufferedWriter outStream, Matcher matcher) {
		String id = cl.getAttributes().getNamedItem("rdf:ID").getTextContent();
		String name = XPath.getNode("name", cl) != null ? XPath.getNode("name", cl).getTextContent() : "";
		String organism = XPath.getNode("organism", cl) != null ? XPath.getNode("organism", cl).getTextContent() : "";
		String tissue = XPath.getNode("tissue", cl) != null ? XPath.getNode("tissue", cl).getTextContent() : "";

		List<Mention> matches = matcher.match(organism);
		organism = match(organism,matcher);

		String regexp = toRegexp(name);
		
		try{
			if (outStream != null)
				outStream.write("CellLine:CLKB:" + id + "\t" + GenerateMatchers.escapeRegexp(name) + "\t" + organism + "\t" + tissue + "\n");
			if (pstmt != null){

				DictionaryEntry de = new DictionaryEntry(id);
				de.addPattern(regexp);
				Set<String> variants = de.convertRegexpToVariants();
				
				for (Mention m : matches){
					for (String variant : variants){
					SQL.set(pstmt,1,"CellLine:CLKB:" + id);
					SQL.set(pstmt,2,variant);
					SQL.set(pstmt,3,m.getMostProbableID() + "|" + tissue);
					pstmt.addBatch();
					}
				}

				pstmt.executeBatch();
			}
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static String toRegexp(String name) {
		return GenerateMatchers.escapeRegexp(name);
	}
}
