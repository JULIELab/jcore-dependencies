package uk.ac.man.documentparser.input;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import martin.common.MyConnection;

import uk.ac.man.documentparser.dataholders.Author;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.dataholders.ExternalID;
import uk.ac.man.documentparser.dataholders.Journal;
import uk.ac.man.documentparser.dataholders.Document.Text_raw_type;
import uk.ac.man.documentparser.dataholders.Document.Type;
import uk.ac.man.documentparser.dataholders.ExternalID.Source;

public class DatabaseListIterator implements DocumentIterator {

	private ResultSet rs;

	private boolean hasNext;
	private boolean full;
	private Statement stmt;

	private MyConnection myConn;
	private Connection conn;
	private String table;

	private int id_exts_counter;

	private List<String> id_exts;

	public DatabaseListIterator(MyConnection myConn, String table, List<String> id_exts, boolean full){
		this.myConn = myConn;
		this.table = table;
		this.id_exts = id_exts;
		this.id_exts_counter = 0;
		this.full = full;

		this.hasNext = id_exts.size() > 0;
		if (hasNext)
			getDoc();
	}

	private void getDoc(){
		boolean retry=true;
		boolean sleep=false;

		while (retry){
			try{
				if (sleep)
					Thread.sleep(10000);

				this.conn = myConn.getConn();
				this.stmt = this.conn.createStatement();

				String q = "select * from " + table + " where id_ext = '" + id_exts.get(id_exts_counter) + "'";
				
//				System.out.print(q + "...");

				this.rs = this.stmt.executeQuery(q);

				if (!rs.next()){
					System.err.println("Warning: no results returned for id_ext='" + id_exts.get(id_exts_counter) + "'");
					rs.close();
					id_exts_counter++;
					this.hasNext = id_exts_counter < id_exts.size();
					if (hasNext)
						getDoc();
				} else {
//					System.out.println(" done.");
				}

				retry = false;

			} catch (Exception e){
				if (e.toString().contains("com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException"))
					throw new RuntimeException(e);

				System.err.println(e.toString());
				System.err.println("Retrying...");
				sleep=true;
			}
		}
	}

	public void skip() {
		if (hasNext) {
			id_exts_counter++;
			hasNext = id_exts_counter < id_exts.size();
			if (hasNext)
				getDoc();
		} else {
			throw new NoSuchElementException();
		}
	}

	public boolean hasNext() {
		return hasNext;
	}

	public Document next() {
		assert(hasNext);
		boolean reconnect = false;

		while (true){
			try{
				if (reconnect){
					Thread.sleep(10000);
					getDoc();
				}

				Document d = null;
				if (!full){
					d = new Document(
							rs.getString("id_ext"),
							rs.getString("text_title"),
							rs.getString("text_abstract"),
							rs.getString("text_body"),
							rs.getString("text_raw"),
							null,
							null,
							null,
							null,
							null,
							null,
							null,
							null,
							null,
							null);
				} else {
					d = new Document(
							rs.getString("id_ext"),
							rs.getString("text_title"),
							rs.getString("text_abstract"),
							rs.getString("text_body"),
							rs.getString("text_raw"),
							convTextRawType(rs.getString("text_raw_type")),
							rs.getString("year"),
							new Journal(rs.getString("id_issn"),null,null),
							convType(rs.getString("article_type")),
							convAuthors(rs.getString("authors")),
							rs.getString("volume"),
							rs.getString("issue"),
							rs.getString("pages"),
							rs.getString("xml"),
							new ExternalID(rs.getString("id_ext"), convSource(rs.getString("source"))));
				}

				rs.close();
				
				id_exts_counter++;
				hasNext = id_exts_counter < id_exts.size();
				if (hasNext)
					getDoc();

				return d;
			} catch (Exception e){
				System.err.println("DatabaseIterator.java: An error occured: " + e.toString());
				System.err.println("Sleeping for 10s, reconnecting to DB and then retrying");
				reconnect=true;				
			}
		}
	}

	private Source convSource(String source) {
		if (source == null)
			return null;

		if (source.equals(ExternalID.Source.ELSEVIER.toString().toLowerCase()))
			return ExternalID.Source.ELSEVIER;
		if (source.equals(ExternalID.Source.MEDLINE.toString().toLowerCase()))
			return ExternalID.Source.MEDLINE;
		if (source.equals(ExternalID.Source.OTHER.toString().toLowerCase()))
			return ExternalID.Source.OTHER;
		if (source.equals(ExternalID.Source.PMC.toString().toLowerCase()))
			return ExternalID.Source.PMC;
		if (source.equals(ExternalID.Source.TEXT.toString().toLowerCase()))
			return ExternalID.Source.TEXT;
		return null;
	}

	private Author[] convAuthors(String string) {
		if (string == null)
			return null;
		if (string.length() == 0)
			return new Author[0];

		String[] fs = string.split("\\|");

		Author[] as = new Author[fs.length];
		for (int i = 0; i < fs.length; i++){
			String[] fss = fs[i].split(", ");
			if (fss.length == 2)
				as[i] = new Author(fss[0],fss[1],null);
			else
				as[i] = new Author(fs[i],"",null);
		}
		return as;
	}

	private Type convType(String type) {
		if (type == null)
			return null;

		if (type.equals(Document.Type.OTHER.toString().toLowerCase()))
			return Document.Type.OTHER;
		if (type.equals(Document.Type.RESEARCH.toString().toLowerCase()))
			return Document.Type.RESEARCH;
		if (type.equals(Document.Type.REVIEW.toString().toLowerCase()))
			return Document.Type.REVIEW;
		return null;
	}

	private Text_raw_type convTextRawType(String type) {
		if (type == null)
			return null;

		if (type.equals(Document.Text_raw_type.OCR.toString().toLowerCase()))
			return Text_raw_type.OCR;
		if (type.equals(Document.Text_raw_type.PDF2TEXT.toString().toLowerCase()))
			return Text_raw_type.PDF2TEXT;
		if (type.equals(Document.Text_raw_type.TEXT.toString().toLowerCase()))
			return Text_raw_type.TEXT;
		if (type.equals(Document.Text_raw_type.XML.toString().toLowerCase()))
			return Text_raw_type.XML;
		return null;
	}

	public void remove() {
		throw new IllegalStateException("not implemented");
	}

	public Iterator<Document> iterator() {
		return this;
	}
}
