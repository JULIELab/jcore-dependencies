package uk.ac.man.documentparser.dataholders;

import martin.common.Misc;
import martin.common.SQL;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.HashSet;

public class Document implements Serializable {
	public enum Type {RESEARCH, REVIEW, OTHER}
	public enum Text_raw_type {XML, OCR, PDF2TEXT, TEXT} 

	private static final long serialVersionUID = 6268131204084207996L;
	
	private String ID;

	private String title, abs, body, rawContent;
	
	private Text_raw_type raw_type;
	private String year;
	private boolean ignoreCoordinates = false;
	
	private Author[] authors;
	private Journal journal;
	private ExternalID externalID;
	private Type type;
	
	private String volume;
	private String issue;
	private String pages;
	
	private String xml;
	
	public Document(String id, String title, String abs, String body, String raw, Text_raw_type raw_type, String year, 
			Journal journal, Type type, Author[] authors, String volume, String issue, String pages, String xml, ExternalID externalID){
		this.ID = id;
		this.title = title;
		this.abs = abs;
		this.body = body;
		this.rawContent = raw;
		this.raw_type = raw_type;
		this.year = year;
		this.journal = journal;
		this.type = type;
		this.authors = authors;
		this.volume = volume;
		this.issue = issue;
		this.pages = pages;
		this.xml = xml;
		this.externalID = externalID;
	}
	
	
	public String getID() {
		return ID;
	}

	public boolean isValid(@SuppressWarnings("unused") int start, @SuppressWarnings("unused") int end){
		return true;
	}

	public String toString(){
		return toString(false);
	}

	public String toString(boolean simplify){
		StringBuffer res = new StringBuffer();

		if (title != null)
			res.append(title + "\n");

		if (abs != null)
			res.append(abs + "\n");

		if (body != null)
			res.append(body + "\n");

		if (rawContent != null && rawContent.length() > 0)
			res.append(rawContent);

		String s = res.toString();
		s = s.replaceAll("\\\\documentclass.*?\\\\end\\{document\\}", "");

		if (simplify){
			while (s.indexOf("  ") != -1){
				s = s.replace("  ", " ");
			}

			try {
				return new String(s.toString().getBytes("ascii"),"ascii");
			} catch (UnsupportedEncodingException e) {
				System.err.println(e.toString());
				e.printStackTrace();
				System.exit(-1);
			}
		}

		return s.toString();
	}

	public void print(){
		System.out.println(this.toString());
		if (authors != null){
			System.out.println("Authors:");
			for (Author a : authors)
				System.out.println(a.toString());
		}
	}
	
	public static PreparedStatement prepareInsertStatements(Connection conn, boolean clear){
		return prepareInsertStatements(conn, "articles", clear);
	}
	
	/**
	 * @param conn the SQL connection used for the insertion
	 * @return 0: INSERT INTO articles_raw (xml, raw_text, date_inserted)... ; 1: "INSERT INTO articles (id_art, text_title, text_abstract, text_body, text_raw, text_raw_type, year, id_issn, article_type, authors, volume, issue, pages, id_ext, source)
	 */
	public static PreparedStatement prepareInsertStatements(Connection conn, String table, boolean clear){
		try{
			if (clear){
				conn.createStatement().execute("DROP TABLE IF EXISTS " + table);
				
				conn.createStatement().execute(
				"CREATE TABLE  " + table + " ("+
						  "`id_art` int(10) unsigned NOT NULL auto_increment,"+
						  "`xml` mediumtext,"+
						  "`id_ext` varchar(255) NOT NULL,"+
						  "`source` enum('medline','pmc','elsevier','text','other') NOT NULL,"+
						  "`date_inserted` datetime NOT NULL,"+
						  "`text_title` varchar(4096) default NULL,"+
						  "`text_abstract` mediumtext,"+
						  "`text_body` mediumtext,"+
						  "`text_raw` mediumtext,"+
						  "`text_raw_type` enum('xml','ocr','pdf2text','text') default NULL,"+
						  "`article_type` enum('research','review','other') default NULL,"+
						  "`authors` mediumtext,"+
						  "`year` varchar(255) default NULL,"+
						  "`id_issn` varchar(255) default NULL,"+
						  "`volume` varchar(255) default NULL,"+
						  "`issue` varchar(255) default NULL,"+
						  "`pages` varchar(255) default NULL,"+
						  "PRIMARY KEY  (`id_art`),"+
						  "KEY `index_issn` (`id_issn`),"+
						  "KEY `index_type` (`article_type`),"+
						  "KEY `index_id_ext` USING BTREE (`id_ext`),"+
						  "KEY `index_src` (`source`),"+
						  "KEY `yearIdx` (`year`),"+
						  "KEY `type` (`article_type`)"+
						") ENGINE=MyISAM AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;");				
			}
			
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO " + table + " " +
					"(xml, id_ext, source, date_inserted, text_title, text_abstract, text_body, text_raw, " +
					"text_raw_type, article_type, authors, year, id_issn, volume, issue, pages) " +
					"VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			return pstmt;
			
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		
		return null;
	}
	
	public void saveToDB(PreparedStatement pstmt) {
		try{
			SQL.set(pstmt, 1, xml);
			SQL.set(pstmt, 2, externalID != null ? externalID.getID() : null);
			SQL.set(pstmt, 3, externalID != null && externalID.getSource() != null? externalID.getSource().toString().toLowerCase() : null);

			SQL.set(pstmt, 4, title);
			SQL.set(pstmt, 5, abs);
			SQL.set(pstmt, 6, body);
			SQL.set(pstmt, 7, rawContent);
			SQL.set(pstmt, 8, raw_type != null ? raw_type.toString().toLowerCase() : null);
			
			SQL.set(pstmt, 9, type != null ? type.toString().toLowerCase() : null);
			
			if (authors != null)
				SQL.set(pstmt, 10, Misc.implode(authors, "|"));
			else
				SQL.set(pstmt,10,(String)null);
			
			SQL.set(pstmt, 11, year);
			SQL.set(pstmt, 12, journal != null ? journal.getISSN() : null);
			SQL.set(pstmt, 13, volume);
			SQL.set(pstmt, 14, issue);
			SQL.set(pstmt, 15, pages);
			
			pstmt.execute();
			/*ResultSet rs = pstmt.getGeneratedKeys();
			rs.first();
			int id = rs.getInt(1);
			return id;*/

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public String getTitle() {
		return title;
	}

	public String getYear() {
		return year;
	}

	public boolean hasTitle(){
		return title != null && title.length() > 5;
	}

	public void saveToTextFile(File file, boolean simplify){
		try {
			BufferedWriter outStream = new BufferedWriter(new FileWriter(file));

			String str = this.toString(simplify);

			outStream.write(str);

			outStream.close();

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public String toHTML(){
		StringBuffer sb = new StringBuffer();

		if (title != null){
			sb.append("<b>" + title.toString() + "</b><br><br>");
		} else {
			sb.append("[Title missing]");
		}
		if (abs != null){
			sb.append("<b>" + abs.toString() + "</b><br><br>");
		} else {
			sb.append("[Abstract missing]");
		}
		if (body != null){
			sb.append("<b>" + body.toString() + "</b><br><br>");
		} else {
			sb.append("[Body missing]");
		}
		if (rawContent != null){
			sb.append("<b>" + rawContent.toString() + "</b><br><br>");
		} else {
			sb.append("[Raw content missing]");
		}

		return sb.toString();		
	}

	public String getRawContent() {
		return rawContent;
	}

	public HashSet<Integer> getMeshTaxIDs(HashMap<String,Integer> meshToTax){
		/*if (meshTerms == null)
			return null;

		HashSet<Integer> res = new HashSet<Integer>();

		for (int i = 0; i < meshTerms.length; i++)
			if (meshToTax.containsKey(meshTerms[i]))
				res.add(meshToTax.get(meshTerms[i]));

		return res;*/ return null;
	}

	/**
	 * @return the ignoreCoordinates
	 */
	public boolean isIgnoreCoordinates() {
		return ignoreCoordinates;
	}

	/**
	 * @param ignoreCoordinates the ignoreCoordinates to set
	 */
	public void setIgnoreCoordinates(boolean ignoreCoordinates) {
		this.ignoreCoordinates = ignoreCoordinates;
	}


	/**
	 * @return the serialVersionUID
	 */
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}


	/**
	 * @return the abs
	 */
	public String getAbs() {
		return abs;
	}


	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}


	/**
	 * @return the raw_type
	 */
	public Text_raw_type getRaw_type() {
		return raw_type;
	}


	/**
	 * @return the authors
	 */
	public Author[] getAuthors() {
		return authors;
	}


	/**
	 * @return the journal
	 */
	public Journal getJournal() {
		return journal;
	}


	/**
	 * @return the externalIDs
	 */
	public ExternalID getExternalID() {
		return externalID;
	}


	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}


	/**
	 * @return the volume
	 */
	public String getVolume() {
		return volume;
	}


	/**
	 * @return the issue
	 */
	public String getIssue() {
		return issue;
	}


	/**
	 * @return the pages
	 */
	public String getPages() {
		return pages;
	}


	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}


	/**
	 * @param rawContent the rawContent to set
	 */
	public void setRawContent(String rawContent) {
		this.rawContent = rawContent;
	}


	/**
	 * @return the xml
	 */
	public String getXml() {
		return xml;
	}


	/**
	 * @param raw_type the raw_type to set
	 */
	public void setRaw_type(Text_raw_type raw_type) {
		this.raw_type = raw_type;
	}


	public String getDescription() {
		if (authors == null || authors.length == 0)
			return ID;
		
		String res = "";
		
		if (authors.length == 1)
			res = authors[0].getSurname();
		else if (authors.length == 2)
			res = authors[0].getSurname() + " and " + authors[1].getSurname();
		else
			res = authors[0].getSurname() + " et al.";
		
		if (year != null)
			res += " (" + year + ")";
		
		return res;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public void setAbs(String abs) {
		this.abs = abs;
	}
}
