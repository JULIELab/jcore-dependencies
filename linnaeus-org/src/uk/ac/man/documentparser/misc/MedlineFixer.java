package uk.ac.man.documentparser.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

import martin.common.ArgParser;
import martin.common.Loggers;
import martin.common.MyConnection;
import martin.common.SQL;
import martin.common.StreamIterator;

public class MedlineFixer {
	public static void main(String[] args){
		ArgParser ap = ArgParser.getParser(args);
		Logger logger = Loggers.getDefaultLogger(ap);
		int report = ap.getInt("report", -1);

		if (ap.containsKey("getIDs")){
			MyConnection conn = martin.common.SQL.connectMySQL2(ap, logger, "articles");
			getIDs(conn, ap.getFile("getIDs"), logger, report);
		}

		if (ap.containsKey("fix")){
			MyConnection conn = martin.common.SQL.connectMySQL2(ap, logger, "articles");
			MyConnection updateConn = new MyConnection("martin", "martin.", "130.88.210.179", 3306, "Articles", logger);
			fix(conn, updateConn, ap.getFile("fix"), ap.getFile("log"), logger, report);
		}

		if (ap.containsKey("deleteRows")){
			MyConnection conn = martin.common.SQL.connectMySQL2(ap, logger, "farzin");
			File ids = ap.getFile("deleteRows");
			String[] tables = ap.gets("tables");

			deleteRows(conn, ids, tables, report, logger);
		}
	}

	private static void deleteRows(MyConnection selectConn, File idFile, String[] tables, int report, Logger logger) {
		try{
			Connection selectConnInstance = selectConn.getConn();

			PreparedStatement[] pstmts = new PreparedStatement[tables.length];
			
			for (int i = 0; i < tables.length; i++)
				pstmts[i] = selectConnInstance.prepareStatement("DELETE from " + tables[i] + " where doc_id = ?");

			StreamIterator lines = new StreamIterator(idFile);
			
			int c = 0; 
			
			for (String l : lines){
				String id = l.split("\t")[0];
				for (PreparedStatement p : pstmts){
					SQL.set(p, 1, id);
					p.execute();
				}			
				
				if (report != -1 && logger != null && ++c % report == 0)
					logger.info("%t: Processed " + c + " documents.\n");
			}				
			
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void fix(MyConnection selectConn, MyConnection updateConn, File in, File log, Logger logger,
			int report) {
		try{

			BufferedWriter outStream = new BufferedWriter(new FileWriter(log));

			Connection selectConnInstance = selectConn.getConn();
			Statement stmt = selectConnInstance.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(Integer.MIN_VALUE);

			PreparedStatement selectPstmt = selectConnInstance.prepareStatement("SELECT xml from articles_medline_2011 where id_ext = ?");

			Connection updateConnInstance = updateConn.getConn();
			PreparedStatement updatePstmt = updateConnInstance.prepareStatement("UPDATE articles_medline_2011 SET text_abstract = ? WHERE id_ext = ?");

			StreamIterator lines = new StreamIterator(in);

			int c = 0;
			for (String line : lines){
				String id = line.split("\t")[0];
				SQL.set(selectPstmt, 1, id);
				ResultSet rs = selectPstmt.executeQuery();

				if (!rs.next())
					throw new IllegalStateException(id);

				String xml = rs.getString(1);

				rs.close();

				StringBuffer sb = new StringBuffer();

				int x = xml.indexOf("<Abstract>");
				if (x != -1){
					int s = xml.indexOf("<AbstractText", x);

					x = xml.indexOf("</Abstract>", x);

					while (s != -1 && s < x){
						int e = xml.indexOf("</AbstractText>", s);

						int s2 = xml.indexOf("Label=\"", s) + 7;
						int e2 = xml.indexOf("\"", s2);

						String label = null;

						if (s2 != 6 && e2 != -1)
							label = xml.substring(s2, e2);

						s = xml.indexOf(">", s) + 1;

						String text = xml.substring(s, e);

						outStream.write(id + "\t" + label + "\t" + text.length() + "\n");

						if (label != null)
							sb.append(label + ": " + text + "\n");
						else
							sb.append(text + "\n");

						s = xml.indexOf("<AbstractText", s+1);
					}
				}

				SQL.set(updatePstmt, 1, sb.toString());
				SQL.set(updatePstmt, 2, id);
				updatePstmt.execute();

				//				System.out.println(sb.toString());

				if (report != -1 && logger != null && ++c % report == 0)
					logger.info("%t: Processed " + c + " documents.\n");
			}

			outStream.close();


		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private static void getIDs(MyConnection myConn, File file, Logger logger, int report) {
		try{
			Connection conn = myConn.getConn();
			Statement stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(Integer.MIN_VALUE);

			ResultSet rs = stmt.executeQuery("SELECT id_ext, xml from articles_medline_2011");

			BufferedWriter outStream = new BufferedWriter(new FileWriter(file));

			int c = 0;

			while (rs.next()){
				String xml = rs.getString(2);

				int num = 0;

				int x = xml.indexOf("<Abstract>");
				if (x != -1){
					int y = xml.indexOf("<AbstractText", x);

					x = xml.indexOf("</Abstract>", x);

					while (y != -1 && y < x){
						y = xml.indexOf("<AbstractText", y+1);
						num++;
					}
				}

				if (num > 1){
					String id = rs.getString(1);
					outStream.write(id + "\t" + num + "\n");
				}

				if (report != -1 && logger != null && ++c % report == 0)
					logger.info("%t: Processed " + c + " documents.\n");
			}

			outStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
		}
	}
}