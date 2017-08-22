package uk.ac.man.documentparser.input;

import java.io.File;
import java.io.StringReader;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import martin.common.xml.*;

import uk.ac.man.documentparser.dataholders.*;
import uk.ac.man.documentparser.dataholders.Document.Type;

public class Medline implements DocumentIterator {

	private NodeList citations;
	private int numArticles;
	private int nextArticle=0;
	private String xml = null;

	public Medline(String filename){
		this(new File(filename));
	}

	public Medline(StringBuffer data){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			org.w3c.dom.Document doc = db.parse(new InputSource(new StringReader(data.toString())));

			citations = XPath.getNodeList("MedlineCitation", doc.getDocumentElement());
			numArticles = citations.getLength();

			xml = data.toString();

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public Medline(File file){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document doc = db.parse(file);

			citations = XPath.getNodeList("MedlineCitation", doc.getDocumentElement());
			numArticles = citations.getLength();

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public boolean hasNext() {
		return nextArticle < numArticles;
	}

	public Iterator<Document> iterator() {
		return this;
	}

	public uk.ac.man.documentparser.dataholders.Document next() {
		Node n = citations.item(nextArticle);

		String ID = XPath.getNode("PMID", n).getTextContent();

		String title = XPath.getNode("Article/ArticleTitle", n).getTextContent();

		Node absNode = XPath.getNode("Article/Abstract/AbstractText", n);
		String abs = absNode != null ? absNode.getTextContent() : null; 

		Node ISSNNode = XPath.getNode("Article/Journal/ISSN", n);
		String ISSN = ISSNNode != null ? ISSNNode.getTextContent() : null;

		Node journalNode = XPath.getNode("Article/Journal/Title", n);
		String journalTitle = journalNode != null ? journalNode.getTextContent() : null;

		Node journalAbbrevNode = XPath.getNode("Article/Journal/ISOAbbreviation", n);
		String journalTitleAbbrev = journalAbbrevNode != null ? journalAbbrevNode.getTextContent() : null;

		if (abs == null){
			absNode = XPath.getNode("OtherAbstract/AbstractText", n);
			abs = absNode != null ? absNode.getTextContent() : null;
		}

		Node yearNode = XPath.getNode("Article/Journal/JournalIssue/PubDate/Year", n);

		String year;
		if (yearNode != null)
			year = yearNode.getTextContent();
		else
			year = XPath.getNode("Article/Journal/JournalIssue/PubDate/MedlineDate", n).getTextContent().split(" ")[0];


		NodeList meshTermList = XPath.getNodeList("MeshHeadingList/MeshHeading", n);
		String[] meshTerms = new String[meshTermList.getLength()];
		for (int i = 0; i < meshTerms.length; i++)
			meshTerms[i] = XPath.getNode("DescriptorName",meshTermList.item(i)).getTextContent();

		NodeList authorList = XPath.getNodeList("Article/AuthorList/Author", n);
		Author[] authors = new Author[authorList.getLength()];
		for (int i = 0; i < authorList.getLength(); i++){
			String fn = XPath.getNode("ForeName", authorList.item(i)) != null ? XPath.getNode("ForeName", authorList.item(i)).getTextContent() : null;
			String sn = XPath.getNode("LastName", authorList.item(i)) != null ? XPath.getNode("LastName", authorList.item(i)).getTextContent() : null;
			authors[i] = new Author(sn, fn, null);
		}

		String volume = XPath.getNode("Article/Journal/JournalIssue/Volume", n) != null ? XPath.getNode("Article/Journal/JournalIssue/Volume", n).getTextContent() : null;
		String issue = XPath.getNode("Article/Journal/JournalIssue/Issue", n) != null ? XPath.getNode("Article/Journal/JournalIssue/Issue", n).getTextContent() : null;

		String pages= XPath.getNode("Article/Pagination/MedlinePgn", n) != null ? XPath.getNode("Article/Pagination/MedlinePgn", n).getTextContent() : null;
		if (pages != null){
			String[] pagess = pages.split("-");
			if (pagess.length == 2){
				if (pagess[1].length() < pagess[0].length())
					pagess[1] = pagess[0].substring(0, pagess[0].length() - pagess[1].length()) + pagess[1];
				pages = pagess[0] + "-" + pagess[1];
			}
		}

		Journal journal = new Journal(ISSN, journalTitle, journalTitleAbbrev);
		ExternalID externalID = new ExternalID(ID, uk.ac.man.documentparser.dataholders.ExternalID.Source.MEDLINE);
		Type type = Type.RESEARCH;

		nextArticle++;

		return new Document(ID, title, abs, null, null, null, year, journal, type, authors, volume, issue, pages, xml, externalID);
	}

	public void remove() {
		throw new IllegalStateException("remove() is not supported");
	}

	public void skip() {
		nextArticle++;
	}
}
