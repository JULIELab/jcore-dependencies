package uk.ac.man.entitytagger.matching.matchers;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

public class PrecomputedMatcher extends Matcher {

	private Map<String,List<Mention>> map;
	private Connection conn;
	private String table;

	public PrecomputedMatcher(File file){
		this(file,null,null); 
	}

	public PrecomputedMatcher(Connection conn, String table){
		//PreparedStatement pstmt = conn.pre
		this.conn = conn;
		this.table = table;
	}

	public PrecomputedMatcher(File file, Set<String> validDocuments, String restrictPostfix){
		map = Mention.loadFromFileToHash(file, validDocuments, restrictPostfix, null);
	}

	public List<Mention> matchByID(String id){
		if (map != null){
			if (map.containsKey(id))
				return map.get(id);
			else
				return new ArrayList<Mention>(0);
		}
		if (conn != null){
			List<Mention> mentions = new LinkedList<Mention>();
			try {
				ResultSet rs = conn.createStatement().executeQuery("SELECT entity,start,end,text,comment FROM " + table + " WHERE document='" + id + "'");
				while (rs.next()){
					String[] ids = rs.getString(1).split("\\|");
					int start = rs.getInt(2);
					int end = rs.getInt(3);
					String term = rs.getString(4);
					String comment = rs.getString(5);
					Mention m = new Mention(ids,start,end,term);
					m.setComment(comment);
					m.setDocid(id);
					mentions.add(m);				
				}
				rs.close();
			} catch (Exception e){
				System.err.println(e);
				e.printStackTrace();
				System.exit(-1);
			}

			return mentions;
		}
		return null;
	}

	@Override
	public List<Mention> match(String text, Document doc) {
		if (doc == null || doc.getID() == null || doc.getID().length() == 0)
			throw new IllegalStateException("A PrecomputedMatcher must be called with a document to resolve the ID");

		List<Mention> matches = matchByID(doc.getID());

		return matches;
	}

	@Override
	public int size() {
		return map.size();
	}

	public Map<String,List<Mention>> getStoredData(){
		return map;
	}
}
