package uk.ac.man.documentparser.input;

import martin.common.Misc;
import martin.common.xml.EntityResolver;
import martin.common.xml.MyNodeList;
import martin.common.xml.XPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import uk.ac.man.documentparser.dataholders.*;
import uk.ac.man.documentparser.dataholders.Document.Type;
import uk.ac.man.documentparser.dataholders.ExternalID.Source;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public class PMC2 implements DocumentIterator{
	private org.w3c.dom.Document doc = null;

	private String xml = null;

	private String id;

	public PMC2(File xmlLocation, String[] dtdLocations){
		String[] fs = xmlLocation.getName().split("-");
		this.id = "PMC" + fs[fs.length-1].split("\\.")[0];
		this.xml = Misc.loadFile(xmlLocation);
		
		if (!xmlLocation.getAbsolutePath().endsWith(".xml") && !xmlLocation.getAbsolutePath().endsWith(".nxml"))
			throw new IllegalStateException("PMC XML files have to end with .xml or .nxml");

//		try {
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//
//			if (dtdLocations != null)
//				db.setEntityResolver(new EntityResolver(dtdLocations));
//
//			doc = db.parse(xmlLocation);
//		} catch (Exception e){
//			System.err.println(e);
//			doc = null;
//			e.printStackTrace();
//			System.exit(-1);
//		}
		
		load(new StringBuffer(xml), dtdLocations);
	}

	public PMC2(StringBuffer data, String dtdLocation[]){
		load(data,dtdLocation);
	}

	private void load(StringBuffer data, String[] dtdLocation) {
		xml = data.toString();

		xml = filter(xml);
		
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			if (dtdLocation != null)
				db.setEntityResolver(new EntityResolver(dtdLocation));

			doc = db.parse(new InputSource(new StringReader(xml)));
		} catch (Exception e){
			System.err.println(xml);
			System.err.println(e);
			e.printStackTrace();
			doc = null;
//			System.exit(-1);
		}
	}

	private String filter(String str) {
		str = Pattern.compile("<table-wrap .*?</table-wrap>", Pattern.DOTALL).matcher(str).replaceAll("");
		str = Pattern.compile("<tex-math .*?</tex-math>", Pattern.DOTALL).matcher(str).replaceAll("");
		
		return str;
	}

	public Iterator<Document> iterator() {
		return this;
	}

	public boolean hasNext() {
		if (doc == null)
			return false;

		return true;
	}

	private Section[] getSection(NodeList mainElement){
		if (mainElement == null)
			return null;

		Section[] mainSections = new Section[mainElement.getLength()];

		for (int i = 0; i < mainElement.getLength(); i++){
			Node n = mainElement.item(i);

			Node titleNode = XPath.getNode("title", n);
			String title = titleNode != null ? titleNode.getTextContent() : "";

			Section[] subSections = getSection(XPath.getNodeList("sec", n));
			NodeList contentParagraphs = XPath.getNodeList("p", n);

			StringBuffer contents = new StringBuffer();

			for (int j = 0; j < contentParagraphs.getLength(); j++)
				contents.append(contentParagraphs.item(j).getTextContent() + "\n");

			mainSections[i] = new Section(title, contents.toString(), subSections);
		}

		return mainSections;
	}

	private String loadFile(File f){
		StringBuffer res = new StringBuffer();
		try{
			BufferedReader inStream = new BufferedReader(new FileReader(f));

			String line = inStream.readLine();
			while (line != null){

				if (line.length() == 0)
					res.append("\n\n");
				else
					res.append(line);

				if (res.length() > 0){
					if (res.charAt(res.length()-1) == '-')
						res = res.deleteCharAt(res.length()-1);
					else
						res.append(" ");
				}

				line = inStream.readLine();
			}

			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return res.toString();
	}

	public uk.ac.man.documentparser.dataholders.Document next() {
		if (doc == null)
			throw new NoSuchElementException();

		Element root = doc.getDocumentElement();

//		String id = null;
//		MyNodeList idList = XPath.getNodeList("front/article-meta/article-id", root);
//		for (Node n : idList)
//			if (n.getAttributes().getNamedItem("pub-id-type").getTextContent().equals("pmc"))
//				id = "PMC" + n.getTextContent();
		
		Node titleNode = XPath.getNode("front/article-meta/title-group/article-title", root);
		String title = titleNode != null ? titleNode.getTextContent() : null;

		Section[] absSections = getSection(XPath.getNodeList("front/article-meta/abstract", root));
		Section[] bdy = getSection(XPath.getNodeList("body", root));

		//		removeSections(bdy, "materials");
		//		removeSections(bdy, "methods");
		//		absSections = null;
		//		bdy = null;

		Node yearNode = XPath.getNode("front/article-meta/pub-date/year", root);
		String year = yearNode != null ? yearNode.getTextContent() : null;

		MyNodeList authorList = XPath.getNodeList("front/article-meta/contrib-group/contrib", root);
		Author[] authors = new Author[authorList.getLength()];
		for (int i = 0; i < authors.length; i++){
			Node snn = XPath.getNode("name/surname", authorList.item(i));
			Node fnn = XPath.getNode("name/given-names", authorList.item(i));
			Node emailn = XPath.getNode("email", authorList.item(i));

			String sn = snn != null ? snn.getTextContent() : null;
			String fn = fnn != null ? fnn.getTextContent() : null;
			String email = emailn != null ? emailn.getTextContent() : null;

			authors[i] = new Author(sn, fn, email);			
		}

		String ISSN = null, jTitle = null, jTitleAbbrev = null;
		MyNodeList ISSNlist = XPath.getNodeList("front/journal-meta/issn", root);
		for (Node n : ISSNlist)
			ISSN = n.getTextContent();
		MyNodeList jIDs = XPath.getNodeList("front/journal/journal-id", root);
		for (Node n : jIDs)
			if (n.getAttributes().getNamedItem("journal-id-type").getTextContent().equals("nlm-ta"))
				jTitleAbbrev = n.getTextContent();
		Node jTitleNode = XPath.getNode("front/journal-meta/journal-title", root);
		jTitle = jTitleNode != null ? jTitleNode.getTextContent() : null;

		Journal journal = new Journal(ISSN, jTitle, jTitleAbbrev);

		String volume = XPath.getNode("front/article-meta/volume", root) != null ? XPath.getNode("front/article-meta/volume", root).getTextContent() : null;
		String issue = XPath.getNode("front/article-meta/issue", root) != null ? XPath.getNode("front/article-meta/issue", root).getTextContent() : null;
		String type = root.getAttributes().getNamedItem("article-type") != null ? root.getAttributes().getNamedItem("article-type").getTextContent() : null;

		String fpage = XPath.getNode("front/article-meta/fpage", root) != null ? XPath.getNode("front/article-meta/fpage", root).getTextContent() : null; 
		String lpage = XPath.getNode("front/article-meta/lpage", root) != null ? XPath.getNode("front/article-meta/lpage", root).getTextContent() : null;
		String pages = null;
		if (fpage != null && lpage != null)
			if (fpage.equals(lpage))
				pages = fpage;
			else
				pages = fpage + "-" + lpage;

		Type typee=null;
		if (type != null){
			if (type.equals("research-article"))
				typee = Type.RESEARCH;
			else if (type.equals("review-article"))
				typee = Type.REVIEW;
			else
				typee = Type.OTHER;
		}



		ExternalID externalID = new ExternalID(id, Source.PMC);

		Document d = new Document("a", title, Section.toString(absSections), Section.toString(bdy), null, null, year, journal, typee, authors, volume, issue, pages, xml, externalID);

		doc = null;
		
		return d;
	}

	private void removeSections(Section[] sections, String keyword) {
		for (int i = 0; i < sections.length; i++){
			if (sections[i] != null){
				Section s = sections[i];

				if (s.getTitle() != null && s.getTitle().toLowerCase().contains(keyword.toLowerCase()))
					sections[i] = null;
				else
					removeSections(s.getSubSections(), keyword);
			}
		}

	}

	public void remove() {
		throw new IllegalStateException("remove() is not supported");
	}

	public void skip() {
		throw new IllegalStateException();
	}
}
