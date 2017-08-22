package uk.ac.man.entitytagger.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import martin.common.Function;
import martin.common.Pair;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;

/**
 * Class representing a tagged document. Contains the original document, and the tags found in that document by a matcher.
 * @author Martin
 */
public class TaggedDocument {
	public enum Format{XMLTags,HTML, Alibaba}

	private Document original;
	private TaggedSection[] abs, body;
	private List<Mention> rawMatches;
	private String rawContent;

	public TaggedDocument(Document original, TaggedSection[] abs, TaggedSection[] body, List<Mention> rawMatches, String rawContent){
		this.original = original;
		this.abs = abs;
		this.body = body;
		this.rawMatches = rawMatches;
		this.rawContent = rawContent;
	}

	public StringBuffer toHTML(boolean link, Function<Pair<String>> alternativeTagFunction){
		if (rawMatches == null)
			return new StringBuffer(original.toString());
		
		StringBuffer sb = new StringBuffer();

		if (original != null && original.getTitle() != null)
			sb.append("<b>" + original.getTitle().replace("<", "_").replace(">", "_") + "</b><br>");

		if (abs != null)
			for (int i = 0; i < abs.length; i++)
				if (abs[i] != null)
					sb.append(abs[i].toHTML());

		if (body != null)
			for (int i = 0; i < body.length; i++)
				if (body[i] != null)
					sb.append(body[i].toHTML());

		if (rawContent != null)
			sb.append(TaggedDocument.toHTML(rawContent.replace("<", "_").replace(">", "_"), rawMatches, Format.HTML, link, alternativeTagFunction));

		return sb;
	}

	private static Pair<String> getMatchTags(Mention m, Format format, boolean link){
		String[] ids = m.getIds();
		Double[] probs = m.getProbabilities();

		String starttag = "";
		String endtag = null;

		if (m.isAmbigous())
			link = false;
		
		if (link == false && format == Format.HTML){
			if (ids[0].equals("red")){
				starttag = "<font style=\"background-color: #FF7777\">";
				endtag = "</font>";
			} else if (ids[0].equals("green")){
				starttag = "<font style=\"background-color: #77FF77\">";
				endtag = "</font>";
			} else if (ids[0].equals("orange")){
				starttag = "<font style=\"background-color: #FF9933\">";
				endtag = "</font>";
			} else if (ids[0].equals("purple")){
				starttag = "<font style=\"background-color: #DB70FF\">";
				endtag = "</font>";
			} else if (ids[0].equals("0")){
				starttag = "<font style=\"background-color: #FF7777\">";
				endtag = "</font>";
			} else {
				starttag = "<font style=\"background-color: #7777FF\">";
				endtag = "</font>";
			}
		} else if (ids.length <= 1 && format == Format.HTML){
			if (ids.length == 0 || ids[0].equals("0") || ids[0].startsWith("CellLine:CLKB:")){
				starttag = "<font style=\"background-color: #FF7777\">";
				endtag = "</font>";
			} else if (ids[0].equals("red")){
				starttag = "<font style=\"background-color: #FF7777\">";
				endtag = "</font>";
			} else if (ids[0].startsWith("species:ncbi:"))
				starttag = "<font style=\"background-color: #88FF88\"><a href=\"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=" + ids[0].substring(13) + "&mode=info\">";
			else if (ids[0].startsWith("genus:ncbi:"))
				starttag = "<font style=\"background-color: #88FF88\"><a href=\"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=" + ids[0].substring(11) + "&mode=info\">";
			else if (ids[0].startsWith("gene:ncbi:"))
				starttag = "<font style=\"background-color: #88FF88\"><a href=\"http://www.ncbi.nlm.nih.gov/gene/" + ids[0].substring(10) + "\">";
			else if (ids[0].startsWith("protein:uniprot:"))
				starttag = "<font style=\"background-color: #88FF88\"><a href=\"http://www.uniprot.org/uniprot/" + ids[0].substring(16) + "\">";
			else 
				throw new IllegalStateException("Could not recognize entity type '" + ids[0] + "'");

			if (endtag == null)
				endtag = "</a></font>";
		} else if (ids.length > 1 && format == Format.HTML){
			starttag = "<u>";

			if (ids[0].startsWith("species:ncbi:"))
				endtag = "</u>[<a href=\"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=" + ids[0].substring(13) + "&mode=info\">" + ids[0] + "</a>";
			else if (ids[0].startsWith("genus:ncbi:"))
				endtag = "</u>[<a href=\"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=" + ids[0].substring(11) + "&mode=info\">" + ids[0] + "</a>";
			else if (ids[0].startsWith("gene:ncbi:"))
				endtag = "</u>[<a href=\"http://www.ncbi.nlm.nih.gov/gene/" + ids[0].substring(10) + "\">" + ids[0] + "</a>";
			else if (ids[0].startsWith("protein:uniprot:"))
				endtag = "</u><a href=\"http://www.uniprot.org/uniprot/" + ids[0].substring(16) + "\">" + ids[0] + "</a>";
			else
				throw new IllegalStateException("Could not recognize entity type");

			for (int j = 1; j < ids.length; j++)
				if (ids[j].startsWith("species:ncbi:"))
					endtag += ", " + "<a href=\"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=" + ids[j].substring(13) + "&mode=info\">" + ids[j] + "</a>";
				else if (ids[j].startsWith("genus:ncbi:"))
					endtag += ", " + "<a href=\"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=" + ids[j].substring(11) + "&mode=info\">" + ids[j] + "</a>";
				else if (ids[j].startsWith("gene:ncbi:"))
					endtag += "</u>[<a href=\"http://www.ncbi.nlm.nih.gov/gene/" + ids[j].substring(10) + "\">" + ids[j] + "</a>";
				else if (ids[j].startsWith("protein:uniprot:"))
					endtag += "</u><a href=\"http://www.uniprot.org/uniprot/" + ids[j].substring(16) + "\">" + ids[j] + "</a>";
				else
					throw new IllegalStateException("Could not recognize entity type");

			endtag += "]";
		} else if (format == Format.XMLTags){
			starttag = "<e data=\"";

			for (int i = 0; i < ids.length; i++){
				if (i == 0){
					starttag += ids[i];
				} else {
					starttag += "|" + ids[i];
				}
				if (probs[i] != null)
					starttag += "?" + probs[i];
			}

			starttag += "\">";

			endtag = "</e>";
		} else if (format == Format.Alibaba){
			int max = 0;
			if (probs != null)
				for (int i = 0; i < probs.length; i++)
					if ((probs[i] != null && probs[max] == null) || (probs[i] != null && probs[max] != null && probs[i] > probs[max]))
						max = i;

			if (ids[max].startsWith("species:ncbi:")){
				String id = ids[max].substring(13);
				return new Pair<String>("<z:species ids=\"" + id + "\">","</z:species>");
			} else {
				throw new IllegalStateException("Could not convert " + ids[max] + " to alibaba");
			}
		} else {
			throw new IllegalStateException("Should not have reached this stage");
		}

		return new Pair<String>(starttag,endtag);
	}

	/**
	 * Converts a document text, with given NER mentions, to e.g. HTML or XML format (adding tags around the recognized mentions).
	 * When calling the method, an alternative user-specific function can be provided of the following signature: Pair<String> Function.function(Object[] args), 
	 * where the returned pair is the pair of tags enclosing the mention (e.g. <u> and </u>) and the args is an array of [the mention, format, link]
	 * @param text the original text of the document
	 * @param matches the recognized NER mentions
	 * @param format the wanted format
	 * @param link whether to construct NER linkouts or not
	 * @param alternativeTagFunction potentially an alternative tagging function (may be null)
	 * @return the text of the document, with formatting tags added around the specified mentions 
	 */
	public static StringBuffer toHTML(String text, List<Mention> matches, Format format, boolean link, Function<Pair<String>> alternativeTagFunction){
		StringBuffer sb = new StringBuffer(text);

		Mention[] matchesArr = matches.toArray(new Mention[0]);
		Arrays.sort(matchesArr);

		int added = 0;

		int prevend = -1;

		for (int i = 0; i < matchesArr.length; i++){
			Mention m = matchesArr[i];
			
			if (m.getStart() > prevend){
				Pair<String> tags = alternativeTagFunction == null ? getMatchTags(m, format, link) : alternativeTagFunction.function(new Object[]{m, format, link});

				String starttag = tags.getX();
				String endtag = tags.getY();

				if (m.getEnd() + added > sb.length())
					throw new IllegalStateException("m.getEnd(): " + m.getEnd() + ", added: " + added + ", sb.length(): " + sb.length() + ", sb: " + sb.toString());

				sb = sb.insert(m.getEnd() + added, endtag);

				if (m.getStart() + added > sb.length())
					throw new IllegalStateException("m.getStart(): " + m.getStart() + ", added: " + added + ", sb.length(): " + sb.length() + ", sb: " + sb.toString());

				sb = sb.insert(m.getStart() + added, starttag);

				added += starttag.length() + endtag.length();

				prevend = m.getEnd();
			}
			
		}		

		return sb;
	}

	/**
	 * @return the original
	 */
	public Document getOriginal() {
		return original;
	}

	/**
	 * @return the abs
	 */
	public TaggedSection[] getAbs() {
		return abs;
	}

	/**
	 * @return the body
	 */
	public TaggedSection[] getBody() {
		return body;
	}

	/**
	 * @return the rawMatches
	 */
	public List<Mention> getRawMatches() {
		return rawMatches;
	}

	public ArrayList<Mention> getAllMatches() {
		ArrayList<Mention> matches = new ArrayList<Mention>();

		if (rawMatches != null)
			matches.addAll(rawMatches);

		if (abs != null)
			for (int i = 0; i < abs.length; i++)
				if (abs[i] != null)
					matches.addAll(abs[i].getAllMatches());

		if (body != null)
			for (int i = 0; i < body.length; i++)
				if (body[i] != null)
					matches.addAll(body[i].getAllMatches());

		return matches;
	}

	public HashSet<String> getAllMatchedSpecies() {
		HashSet<String> res = new HashSet<String>();

		ArrayList<Mention> matches = getAllMatches();

		for (Mention m : matches)
			for (String id : m.getIds())
				res.add(id);

		return res;
	}

	public String getContent() {
		return rawContent;
	}
}
