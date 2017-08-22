package uk.ac.man.documentparser.misc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;

import martin.common.ArgParser;
import martin.common.Loggers;
import martin.common.Misc;
import martin.common.SQL;

public class RemoveDuplicates {
	public static void main(String[] args){
		ArgParser ap = new ArgParser(args);
		Logger logger = Loggers.getDefaultLogger(ap);
		Connection conn = SQL.connectMySQL(ap, logger, "articles");
		Set<String> ids = Misc.loadStringSetFromFile(ap.getFile("ids"));
		run (conn,ids, ap.get("table"));
	}

	private static void run(Connection conn, Set<String> ids, String table) {
		try{
			Statement stmt = conn.createStatement();
		for (String id : ids){
			System.out.println(id);
			ResultSet rs = stmt.executeQuery("select id_art from " + table + " where id_ext = '" + id + "'");
			LinkedList<Integer> id_arts = new LinkedList<Integer>();
			while (rs.next())
				id_arts.add(rs.getInt(1));
			while (id_arts.size() > 1)
				stmt.execute("delete from " + table + " where id_art = " + id_arts.removeFirst());
//			System.out.println("\tdelete from " + table + " where id_art = " + id_arts.removeFirst());
		}

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
