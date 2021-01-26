package uk.ac.man.documentparser.input;

import martin.common.xml.EntityResolver;
import martin.common.xml.XPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import uk.ac.man.documentparser.dataholders.Author;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.dataholders.Section;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Iterator;

public class BMC implements DocumentIterator{
	private org.w3c.dom.Document doc;
	private int numArticles;
	private int nextArticle=0;

	public BMC(StringBuffer data, String dtdLocation[]){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			if (dtdLocation != null)
				db.setEntityResolver(new EntityResolver(dtdLocation));

			doc = db.parse(new InputSource(new StringReader(data.toString())));

			numArticles = doc.getElementsByTagName("art").getLength();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public BMC(java.io.File file, String dtdLocation[]){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			if (dtdLocation != null)
				db.setEntityResolver(new EntityResolver(dtdLocation));

			System.out.println(file.getAbsolutePath()); //TODO: remove
			
			doc = db.parse(file);

			numArticles = doc.getElementsByTagName("art").getLength();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public BMC(String filename, String[] dtdLocation){
		this(new java.io.File(filename), dtdLocation);
	}

	public boolean hasNext() {
		return nextArticle < numArticles;
	}

	private Section[] getSection(NodeList mainElement){
		if (mainElement == null)
			return null;

		Section[] mainSections = new Section[mainElement.getLength()];

		for (int i = 0; i < mainElement.getLength(); i++){
			Node n = mainElement.item(i);
			String title = XPath.getNode("st/p", n).getTextContent();
			Section[] subSections = getSection(XPath.getNodeList("sec", n));
			NodeList contentParagraphs = XPath.getNodeList("p", n);

			String contents = new String();

			for (int j = 0; j < contentParagraphs.getLength(); j++)
				contents += contentParagraphs.item(j).getTextContent();

			mainSections[i] = new Section(title, contents, subSections);
		}

		return mainSections;
	}

	public uk.ac.man.documentparser.dataholders.Document next() {
		Element root = doc.getDocumentElement();

		String id = XPath.getNode("ui", root).getTextContent();
		
		Node titleNode = XPath.getNode("fm/bibl/title/p", root);
		String title = titleNode != null ? titleNode.getTextContent() : null;
		
		NodeList absNodes =XPath.getNodeList("fm/abs/sec", root); 
		Section[] abs = absNodes != null ? getSection(absNodes) : null;
		
		NodeList bdyNodes =XPath.getNodeList("bdy/sec", root); 
		Section[] bdy = bdyNodes != null ? getSection(bdyNodes) : null;
		
		String year = null;//XPath.getNode("fm/bibl/pubdate", root).getTextContent();
		
		Author[] authors = null;//getAuthors(root);
		
		nextArticle++;

		return new Document(id, title, Section.toString(abs), Section.toString(bdy), null, null, year, null, null, authors, null, null, null, null, null);
	}

	private Author[] getAuthors(Element root) {
		NodeList authorList = XPath.getNodeList("art/fm/bibl/aug/au", root);
		Author[] authors = new Author[authorList.getLength()];
		
		for (int i = 0; i < authors.length; i++){
			Node snn = XPath.getNode("snm", authorList.item(i));
			Node fnn = XPath.getNode("fnm", authorList.item(i));
			Node emailn = XPath.getNode("email", authorList.item(i));
			
			String sn = snn != null ? snn.getTextContent() : null;
			String fn = fnn != null ? fnn.getTextContent() : null;
			String email = emailn != null ? emailn.getTextContent() : null;
			
			authors[i] = new Author(sn, fn, email);			
		}
		
		return authors;
	}

	public void remove() {
		throw new IllegalStateException("remove() is not supported");
	}

	public void skip() {
		nextArticle++;
	}

	public Iterator<Document> iterator() {
		return this;
	}
}
