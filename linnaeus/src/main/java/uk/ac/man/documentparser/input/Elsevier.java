package uk.ac.man.documentparser.input;

import martin.common.Misc;
import martin.common.xml.MyNodeList;
import martin.common.xml.XPath;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import uk.ac.man.documentparser.dataholders.Author;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.dataholders.Document.Type;
import uk.ac.man.documentparser.dataholders.ExternalID;
import uk.ac.man.documentparser.dataholders.ExternalID.Source;
import uk.ac.man.documentparser.dataholders.Journal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Elsevier implements DocumentIterator {
	private boolean next=true;
	private String xml;
//	private String[] dtdLocations;

	public Elsevier(StringBuffer xml, String[] dtdLocations){
			this.xml=xml.toString();
	}

	public Elsevier(File file, String[] dtdLocations){
		System.out.println(file.getAbsolutePath());
			this.xml = Misc.loadFile(file);
	}

	public void skip() {
		if (!next)
			throw new NoSuchElementException();
		next=false;
	}

	public boolean hasNext() {
		return next;
	}

	private String get(Node root, String name){
		return get(root, new String[]{name});
	}
	private String get(Node root, String[] nodes){
		for (String name : nodes){
			Node n =  martin.common.xml.XPath.getNode(name, root);
			if (n != null)
				return n.getTextContent();
		}
		return null;
	}
	
	public Document next() {
		if (!next)
			throw new NoSuchElementException();

		xml = xml.replaceAll("\t*", "");
		xml = xml.replaceAll("\n", " ");
		while (xml.contains("  "))
			xml = xml.replaceAll("  ", " ");
		
		org.w3c.dom.Document root=null;
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			root = db.parse(new InputSource(new StringReader(xml)));
		} catch (Exception e){
			throw new RuntimeException(e);
		}
		

		String title = get(root, new String[]{"doc:document/ja:article/ja:head/ce:title", "doc:document/cja:converted-article/cja:head/ce:title","doc:document/ja:simple-article/ja:simple-head/ce:title"});
		
		String abs = get(root, new String[]{"doc:document/ja:article/ja:head/ce:abstract", "doc:document/cja:converted-article/cja:head/ce:abstract", "doc:document/ja:simple-article/ja:simple-head/ce:abstract"});
		if (abs != null && abs.startsWith(" Abstract "))
			abs = abs.substring(11);
		while (abs != null && abs.contains("  "))
			abs = abs.replaceAll("  ", " ");
		
		String bdy = getBody(root);
		while (bdy != null && bdy.contains("  "))
			bdy = bdy.replaceAll("  ", " ");

		String raw = get(root, new String[]{"doc:document/dp:document-properties/dp:raw-text"});
		while (raw != null && raw.contains("  "))
			raw = raw.replaceAll("  ", " ");
		
		String doi = get(root, new String[]{"doc:document/rdf:RDF/rdf:Description/prism:doi","doc:document/cja:converted-article/cja:item-info/ce:doi"});
		String year = get(root, "doc:document/rdf:RDF/rdf:Description/prism:coverDate");
		String pages = get(root, "doc:document/rdf:RDF/rdf:Description/prism:pageRange");
		
		String volume = get(root, "doc:document/rdf:RDF/rdf:Description/prism:volume");
		String issue = get(root, "doc:document/rdf:RDF/rdf:Description/prism:number");
		
		String ISSN = get(root, "doc:document/rdf:RDF/rdf:Description/prism:issn");
		String journalName = get(root, "doc:document/rdf:RDF/rdf:Description/prism:publicationName");

		
		Author[] authors = getAuthors(root);
		
		
		
		Type type = abs != null ? Type.RESEARCH : Type.OTHER;

		ExternalID eid = new ExternalID(doi,Source.ELSEVIER);
		Journal journal = new Journal(ISSN,journalName,null);

		if (doi == null)
			return null;
		
		Document d = new Document(doi,title,abs,bdy,raw,null,year,journal,type,authors,volume,issue,pages,xml,eid);
		next=false;

		return d;
	}

	private StringBuffer getSection(Node root){
		StringBuffer sb = new StringBuffer();

		MyNodeList sections = XPath.getNodeList("ce:section", root);
		for (Node s : sections){
			String title = XPath.getNode("ce:section-title", s) != null ? XPath.getNode("ce:section-title", s).getTextContent() : null;
			sb.append(title + "\n");

			sb.append(getSection(s));
		}
		
		MyNodeList paras = XPath.getNodeList("ce:para", root);
		for (Node p : paras)
			sb.append(p.getTextContent() + "\n");
		
		return sb;
	}
	
	private String getBody(org.w3c.dom.Document root) {
		StringBuffer sb = new StringBuffer();
		
		Node bdyNode = XPath.getNode("doc:document/ja:article/ja:body/ce:sections", root);
		if (bdyNode == null)
			bdyNode = XPath.getNode("doc:document/cja:converted-article/cja:body/ce:sections", root);
		if (bdyNode == null)
			bdyNode = XPath.getNode("doc:document/ja:simple-article/ja:body/ce:sections", root);
		
		if (bdyNode == null)
			return null;
		
		return getSection(bdyNode).toString();
	}

	private Author[] getAuthors(org.w3c.dom.Document root) {
		MyNodeList authorList = XPath.getNodeList("doc:document/ja:article/ja:head/ce:author-group/ce:author", root);
		if (authorList == null || authorList.getLength() == 0)
			authorList = XPath.getNodeList("doc:document/cja:converted-article/cja:head/ce:author-group/ce:author", root);
		
		if (authorList == null || authorList.getLength() == 0){
			return null;
		}
		
		Author[] authors = new Author[authorList.getLength()];
		int i = 0; 
		
		for (Node n : authorList){
			String gn = XPath.getNode("ce:given-name", n) != null ? XPath.getNode("ce:given-name", n).getTextContent() : null;
			String sn = XPath.getNode("ce:surname", n).getTextContent();
			String add = XPath.getNode("ce:e-address", n) != null ? XPath.getNode("ce:e-address",n).getTextContent() : null; 
			authors[i++] = new Author(sn,gn,add);			
		}
		
		return authors;
	}

	public void remove() {
		throw new IllegalStateException("Not implemented");
	}

	public Iterator<Document> iterator() {
		return this;
	}
}
