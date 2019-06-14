package martin.common;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SQL {

	/**
	 * Creates a connection to a MySQL database, using arguments held by the
	 * ArgParser.
	 * 
	 * @param ap
	 *            Argument parser, holding properties "dbHost", "dbUsername",
	 *            "dbPassword" and "dbSchema".
	 * @param logger
	 *            Logger which is used to provide information back to the user.
	 * @param postfix
	 *            if postfix != null, arguments will instead be read from
	 *            dbHost-postfix, dbUsername-postfix, etc. instead.
	 * @return a connection to the database.
	 */
	public static Connection connectMySQL(ArgParser ap, Logger logger,
			String postfix) {
		if (postfix == null)
			postfix = "";
		else
			postfix = "-" + postfix;

		String host = ap.get("dbHost" + postfix);
		String user = ap.get("dbUsername" + postfix);
		String pass = ap.get("dbPassword" + postfix);
		String schema = ap.get("dbSchema" + postfix);

		int port = ap.getInt("dbPort" + postfix, 3306);

		if (host == null)
			throw new IllegalStateException(
					"SQL connection failed: host was not specified (--dbHost"
					+ postfix + " <host>)");
		if (user == null)
			throw new IllegalStateException(
					"SQL connection failed: username was not specified (--dbUsername"
					+ postfix + " <username>)");
		if (pass == null)
			throw new IllegalStateException(
					"SQL connection failed: password was not specified (--dbPassword"
					+ postfix + " <password>)");
		if (schema == null)
			throw new IllegalStateException(
					"SQL connection failed: database schema was not specified (--dbSchema"
					+ postfix + " <schema>)");

		try {
			if (logger != null)
				logger.info("%t: Connecting to MySQL database " + schema
						+ " at " + user + "@" + host + ":" + port + "...");

			Class.forName("com.mysql.jdbc.Driver").newInstance();

			Connection conn = DriverManager.getConnection("jdbc:mysql://"
					+ host + ":" + port + "/" + schema + "?autoReconnect=true&user=" + user
					+ "&password=" + pass  + "&netTimeoutForStreamingResults=3600");

			if (logger != null)
				logger.info(" done, connected.\n");

			return conn;
		}

		catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return null;
	}
	
	/**
	 * Creates a connection to a MySQL database, using arguments held by the
	 * ArgParser.
	 * 
	 * @param ap
	 *            Argument parser, holding properties "dbHost", "dbUsername",
	 *            "dbPassword" and "dbSchema".
	 * @param logger
	 *            Logger which is used to provide information back to the user.
	 * @param postfix
	 *            if postfix != null, arguments will instead be read from
	 *            dbHost-postfix, dbUsername-postfix, etc. instead.
	 * @return A "MyConnection" object, which can be used to get a connection (but also allows reconnection).
	 */
	public static MyConnection connectMySQL2(ArgParser ap, Logger logger, String postfix) {
		if (postfix == null)
			postfix = "";
		else
			postfix = "-" + postfix;

		String host = ap.get("dbHost" + postfix);
		String user = ap.get("dbUsername" + postfix);
		String pass = ap.get("dbPassword" + postfix);
		String schema = ap.get("dbSchema" + postfix);
		int port = ap.getInt("dbPort" + postfix, 3306);
		
		if (host == null)
			throw new IllegalStateException(
					"SQL connection failed: host was not specified (--dbHost"
					+ postfix + " <host>)");
		if (user == null)
			throw new IllegalStateException(
					"SQL connection failed: username was not specified (--dbUsername"
					+ postfix + " <username>)");
		if (pass == null)
			throw new IllegalStateException(
					"SQL connection failed: password was not specified (--dbPassword"
					+ postfix + " <password>)");
		if (schema == null)
			throw new IllegalStateException(
					"SQL connection failed: database schema was not specified (--dbSchema"
					+ postfix + " <schema>)");

		return new MyConnection(user, pass, host, port, schema, logger);
	}

	public static void main(String[] args) throws SQLException {
		if (args.length == 0) {
			System.err
			.println("Usage: sql.jar <DB settings> [--db <database setting tag>] [--uploadTable <file> <table name> [--separator <sep>] [--no-clear]] [--downloadTable <table name> <file> [--separator <sep>]]");
			System.exit(-1);
		}

		ArgParser ap = new ArgParser(args);
		Logger logger = Loggers.getDefaultLogger(ap);

		MyConnection myconn = SQL.connectMySQL2(ap, logger, ap.get("db"));
		Connection conn = myconn.getConn();
		
		int report = ap.getInt("report",-1);

		if (ap.containsKey("uploadTable")) {
			File f = new File(ap.gets("uploadTable")[0]);
			String table = ap.gets("uploadTable")[1];
			String sep = ap.get("separator", "\t");
			uploadTable(f, table, sep, conn, !ap.containsKey("no-clear"),report,logger);
		}
		
		try {
			conn.close();
		} catch (SQLException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void uploadTable(File f, String table, String sep,
			Connection conn, boolean clear, int report, Logger logger) {

		logger.info("%t: Guessing column types...\n");

		Tuple<String[], int[]> types = guessTypes(f, sep);

		logger.info("%t: Done. Creating table...\n");

		StreamIterator lines = new StreamIterator(f);
		String header = lines.next();
		assert (header != null && header.startsWith("#"));
		String[] columns = header.substring(1).split(sep,-1);

		List<String> indices = getIndices(columns);
		
		PreparedStatement ps = createTable(conn, table, columns, types.getA(), clear);

		logger.info("%t: Done. Inserting rows...\n");

		int c = 0;

		try {
			for (String l : lines) {
				
				if (!l.startsWith("#")) {
					String[] fs = l.split(sep,-1);
					
					for (int i = 0; i < fs.length; i++) {
						switch (types.getB()[i]) {
						case 0:
							set(ps, i+1, Integer.parseInt(fs[i]));
							break;
						case 1:
							set(ps, i+1, Float.parseFloat(fs[i]));
							break;
						case 2:
							set(ps, i+1, fs[i]);
							break;
						case 3:
							set(ps, i+1, fs[i]);
							break;
						case 4:
							set(ps, i+1, fs[i]);
							break;
						case 5:
							set(ps, i+1, fs[i]);
							break;
						}
					}

					ps.addBatch();

					if (++c % 100 == 0)
						ps.executeBatch();
				}

				if (report != -1 && c % report == 0)
					logger.info("%t: Uploaded " + c + " rows.\n");
			}

			if (c % 100 != 0)
				ps.executeBatch();

			ps.close();
			
			if (indices.size() > 0){
				logger.info("%t: Indexing...\n");
				index(conn, table, indices);
				logger.info("%t: Done.\n");
			}
			
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void index(Connection conn, String table,
			List<String> indices) {
		
		int c = 0;
		
		String q = "ALTER TABLE `" + table + "`";
		for (int i = 0; i < indices.size() - 1; i++)
			q += " ADD INDEX `index_" + c++ + "`(`" + indices.get(i) + "`),";
		
		q += " ADD INDEX `index_" + c++ + "`(`" + indices.get(indices.size()-1) + "`);";
		
		System.out.println(q);
		
		try{
			conn.createStatement().execute(q);
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static List<String> getIndices(String[] columns) {
		List<String> res = new ArrayList<String>();
		
		for (int i = 0;i < columns.length; i++)
			if (columns[i].startsWith("_")){
				columns[i] = columns[i].substring(1);
				res.add(columns[i]);
			}

		return res;
	}

	private static PreparedStatement createTable(Connection conn, String table,
			String[] columnNames, String[] types, boolean clear) {

		assert (columnNames.length == types.length);

		String q1 = "DROP TABLE IF EXISTS `" + table + "`;";
		String q2 = "CREATE TABLE `" + table + "` (";

		for (int i = 0; i < columnNames.length - 1; i++) {
			q2 += "`" + columnNames[i] + "` " + types[i] + ", ";
		}

		q2 += "`" + columnNames[columnNames.length - 1] + "` "
		+ types[columnNames.length - 1];

		q2 += ") ENGINE=MyISAM DEFAULT CHARSET=latin1;";

		PreparedStatement ps = null;

		try {
			if (clear){
				conn.createStatement().execute(q1);
				System.out.println(q2);
				conn.createStatement().execute(q2);
			}

			String q3 = "INSERT INTO " + table + " ("
			+ Misc.implode(columnNames, ",") + ") VALUES (";
			for (int i = 0; i < columnNames.length - 1; i++)
				q3 += "?,";
			q3 += "?);";

			ps = conn.prepareStatement(q3);

		} catch (SQLException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

		return ps;
	}

	private static Tuple<String[], int[]> guessTypes(File file, String sep) {
		/*
		 * types: 0: Integer 1: float 2: varchar(255) 3: varchar(1000) 4: varchar(4096) 5: text
		 */

		StreamIterator lines = new StreamIterator(file, true);
		int[] types = null;
		for (String l : lines) {
			String[] fs = l.split(sep,-1);
			if (types == null)
				types = new int[fs.length];
			else if (types.length != fs.length)
				throw new IllegalStateException(
						"Variable number of fields: line '" + l
						+ "' should have " + types.length + " fields.");

			for (int i = 0; i < fs.length; i++) {
				String f = fs[i];

				int type = types[i];

				if (type == 0) {
					try {
						@SuppressWarnings("unused")
						int x = Integer.parseInt(f);
					} catch (Exception e) {
						type++;
					}
				}
				if (type == 1) {
					try {
						@SuppressWarnings("unused")
						float x = Float.parseFloat(f);
					} catch (Exception e) {
						type++;
					}
				}
				if (type == 2 && f.length() > 255)
					type++;
				if (type == 3 && f.length() > 1000)
					type++;
				if (type == 4 && f.length() > 4096)
					type++;

				types[i] = type;
			}
		}

		assert (types != null);

		String[] typesS = new String[types.length];

		for (int i = 0; i < types.length; i++) {
			switch (types[i]) {
			case 0:
				typesS[i] = "INT(32)";
				break;
			case 1:
				typesS[i] = "FLOAT";
				break;
			case 2:
				typesS[i] = "VARCHAR(255)";
				break;
			case 3:
				typesS[i] = "VARCHAR(1000)";
				break;
			case 4:
				typesS[i] = "VARCHAR(4096)";
				break;
			case 5:
				typesS[i] = "TEXT";
				break;
			}
		}

		return new Tuple<String[], int[]>(typesS, types);
	}

	public static void set(PreparedStatement pstmt, int field, Boolean value) {
		try{
			if (value != null)
				pstmt.setBoolean(field, value);
			else
				pstmt.setNull(field, java.sql.Types.NULL);
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void set(PreparedStatement pstmt, int field, Float value) {
		try{
			if (value != null)
				pstmt.setFloat(field, value);
			else
				pstmt.setNull(field, java.sql.Types.NULL);
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void set(PreparedStatement pstmt, int field, Double value) {
		try{
			if (value != null)
				pstmt.setDouble(field, value);
			else
				pstmt.setNull(field, java.sql.Types.NULL);
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void set(PreparedStatement pstmt, int field, Integer value) {
		try{
			if (value != null)
				pstmt.setInt(field, value);
			else
				pstmt.setNull(field, java.sql.Types.NULL);
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void set(PreparedStatement pstmt, int field, String value) {
		try{
			if (value != null)
				pstmt.setString(field, value);
			else
				pstmt.setNull(field, java.sql.Types.NULL);
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
}