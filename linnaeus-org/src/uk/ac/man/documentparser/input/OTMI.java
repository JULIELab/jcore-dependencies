package uk.ac.man.documentparser.input;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import martin.common.xml.XPath;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.dataholders.Document.Text_raw_type;

public class OTMI implements DocumentIterator {

	private Node root;

	public OTMI(File file){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			root = XPath.getNode("atom:entry", db.parse(file));
		} catch (Exception e){
			
			System.err.println("Failed parsing " + file.getAbsolutePath() + ", error: " + e);
			root = null;
			/*
			
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);*/
		}
	}
	
	public void skip() {
		if (root == null)
			throw new NoSuchElementException();
		root = null;
	}

	public boolean hasNext() {
		return root != null;
	}

	public Iterator<Document> iterator() {
		return this;
	}

	private ArrayList<String> getSnippets(NodeList sections){
		ArrayList<String> snippets = new ArrayList<String>();
		
		for (int i = 0; i < sections.getLength(); i++){
			Node n = sections.item(i);
			
			NodeList sections2 = XPath.getNodeList("otmi:section", n);
			snippets.addAll(getSnippets(sections2));
			
			NodeList ss = XPath.getNodeList("otmi:snippets/otmi:snippet", n);

			for (int j = 0; j < ss.getLength(); j++){
				Node s = ss.item(j);
				snippets.add(s.getTextContent());
			}
		}
		
		return snippets;
	}
	
	private HashMap<String,Integer> getWordCounts(NodeList sections){
		HashMap<String,Integer> res = new HashMap<String,Integer>();
		
		for (int i = 0; i < sections.getLength(); i++){
			Node n = sections.item(i);
			
			NodeList sections2 = XPath.getNodeList("otmi:section", n);
			
			HashMap<String,Integer> r = getWordCounts(sections2);
			
			for (String k : r.keySet()){
				if (res.containsKey(k)){
					res.put(k, res.get(k)+r.get(k));
				} else {
					res.put(k,r.get(k));
				}
			}
			
			NodeList vectors = XPath.getNodeList("otmi:vectors/otmi:vector",n);
			
			for (int j = 0; j < vectors.getLength(); j++){
				Node v = vectors.item(j);
				String k = v.getTextContent();
				int c = Integer.parseInt(v.getAttributes().getNamedItem("count").getTextContent());
				
				if (res.containsKey(k))
					res.put(k,res.get(k)+c);
				else
					res.put(k,c);
			}
		}
		
		return res;
	}

	public Document next() {
		if (!hasNext())
			throw new NoSuchElementException();
	
		String title = XPath.getNode("atom:title", root).getTextContent();
		String id = XPath.getNode("atom:id", root).getTextContent();
		
		String yearstr = XPath.getNode("atom:published", root).getTextContent();
		String year = yearstr.length() >= 4 ? yearstr.substring(0,4) : null;
		
		NodeList sections = XPath.getNodeList("otmi:data/otmi:section", root);
		
		ArrayList<String> snippets = getSnippets(sections);
		HashMap<String,Integer> wordCounts = getWordCounts(sections);
		
		StringBuffer sb = new StringBuffer();
		
		for (String sn : snippets)
			sb.append(sn + "\n");
		
		sb.append("\n");
		
		for (String k : wordCounts.keySet()){
			int c = wordCounts.get(k);
			for (int j = 0; j < c; j++)
				sb.append(k + " ");
			sb.append("\n");
		}

		Document d = new Document(id, title, null, null, sb.toString(), Text_raw_type.XML, year, null, null, null, null, null, null, null, null);
		d.setIgnoreCoordinates(true);
		
		root = null; //invalidate future next() requests (and allow hasNext() to return false)
		
		return d;
	}

	public void remove() {
		throw new IllegalStateException("not implemented");		
	}
}
