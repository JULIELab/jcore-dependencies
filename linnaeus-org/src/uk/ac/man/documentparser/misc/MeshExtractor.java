package uk.ac.man.documentparser.misc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

import martin.common.ArgParser;
import martin.common.Loggers;
import martin.common.SQL;

public class MeshExtractor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);
		Logger logger = Loggers.getDefaultLogger(ap);
//		Connection conn = SQL.connectMySQL(ap, logger, "articles");
		
		if (ap.containsKey("help") || args.length==0){
			System.out.println("--in <select id,xml from table> --out <table> [--report <x>] <articles db>");
		}

		String inQuery = ap.getRequired("in");
		String outTable = ap.getRequired("out");

		PreparedStatement pstmt = createTable(SQL.connectMySQL(ap, logger, "articles"),outTable);

		int report = ap.getInt("report",-1);

		run(SQL.connectMySQL(ap, logger, "articles"),inQuery,pstmt,report,logger);

	}

	private static void run(Connection conn, String inQuery,
			PreparedStatement pstmt, int report, Logger logger) {

		try{

			Statement stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(Integer.MIN_VALUE);
			ResultSet rs = stmt.executeQuery(inQuery);

			int c = 0; 

			while (rs.next()){
				String id = rs.getString(1);
				String xml = rs.getString(2);


//				System.out.println(xml);



				
				int s = xml.indexOf("<MeshHeading>");
				
				while (s != -1){
					int e = xml.indexOf("</MeshHeading>",s);
					
					int s1 = xml.indexOf("<Descrip",s);
					int e1 = xml.indexOf(">",s1);
					int e2 = xml.indexOf("<",e1);
					
					char major = xml.charAt(s1+30);
					
					
					String descript =xml.substring(e1+1,e2); 
					

//					System.out.println(descript + "\t" + major);
					
					int s10 = xml.indexOf("<Quali",s1);
					
					if (s10 == -1 || s10 >= e){
						SQL.set(pstmt, 1, id);
						SQL.set(pstmt, 2, descript);
						SQL.set(pstmt, 3, (String)null);
						SQL.set(pstmt, 4, major == 'Y');
						pstmt.addBatch();
					}					
					
					while (s10 != -1 && s10 < e){
						char major2 = xml.charAt(s10+29);

						int e10 = xml.indexOf(">",s10);
						int s11 = xml.indexOf("<",e10);
						
						String qual = xml.substring(e10+1,s11);
						
						
//						System.out.println("\t" + descript + "\t" + qual + "\t" + major2);

						SQL.set(pstmt, 1, id);
						SQL.set(pstmt, 2, descript);
						SQL.set(pstmt, 3, qual);
						SQL.set(pstmt, 4, major2 == 'Y');
						pstmt.addBatch();
						
						
						s10 = xml.indexOf("<Quali",s10+1);
						
					}
					
					
					
					
					
					s = xml.indexOf("<MeshHeading>",s+1);
				}




				c++;


				if (report != -1 && c % report == 0)
					logger.info("%t: Processed " + c + " documents.\n");
				
				if (c % 100 == 0)
					pstmt.executeBatch();
			}
			
			pstmt.executeBatch();





		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}


	}

	private static PreparedStatement createTable(Connection conn,
			String outTable) {

		String q = "CREATE TABLE `" + outTable + "` (" + 
		"`document` VARCHAR(127) NOT NULL," + 
		"`descriptor` VARCHAR(127) NOT NULL," + 
		"`qualifier` VARCHAR(127)," + 
		"`major` BOOLEAN NOT NULL," + 
		"INDEX `document`(`document`)," + 
		"INDEX `descriptor`(`descriptor`)," + 
		"INDEX `qualifier`(`qualifier`)," + 
		"INDEX `major`(`major`)" + 
		") " + 
		"ENGINE = MyISAM;";

		System.out.println(q);

		try{

			conn.createStatement().execute("DROP TABLE IF EXISTS `" + outTable + "`");
			conn.createStatement().execute(q);

			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO " + outTable + " (document,descriptor,qualifier,major) VALUES (?,?,?,?)");
			return pstmt;

		} catch (Exception e){
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(-1);

		}	
		return null;
	}
}
