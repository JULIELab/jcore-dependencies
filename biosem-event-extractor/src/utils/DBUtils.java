/*
 * @DATE: Aug 30, 2010
 */
package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * 
 * @author Bui Quoc Chinh
 */
public class DBUtils {

	private static final Logger log = LoggerFactory.getLogger(DBUtils.class);

	public Connection con;
	public ResultSet rs;
	public Statement stmt;
	boolean open = false;
	private String defaultName;
	private String currentName;
	private String protocol;

	private String properties;

	public DBUtils() {
		this("data/Data", "file");
	}

	public DBUtils(String defaultName, String protocol) {
		this(defaultName, protocol, null);
	}

	public DBUtils(String defaultName, String protocol, String properties) {
		this.defaultName = this.currentName = defaultName;
		this.protocol = protocol;
		this.properties = properties;
	}

	public void openDB() {
		log.debug("Opening database with protocol {}, name {} and properties {}.",
				new Object[] { protocol, defaultName, properties });
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			String propertyString = properties != null ? ";" + properties : "";
			con = DriverManager.getConnection("jdbc:hsqldb:" + protocol + ":" + defaultName + propertyString, "sa", "");
			stmt = con.createStatement();
			open = true;
		} catch (NullPointerException e) {
			// log.error("Couldn't establish DB connection with protocol: "
			// + protocol + ", name: " + defaultName + " and properties: "
			// + properties + ". Exception was: ", e);
			throw e;
		} catch (Exception e) {
			// log.error("Couldn't establish DB connection with protocol: "
			// + protocol + ", name: " + defaultName + " and properties: "
			// + properties + ". Exception was: ", e);
			throw new RuntimeException(e);
		}
	}

	public void openDB(String dbName) {
		this.currentName = dbName;
		log.debug("Opening database with protocol {}, name {} and properties {}.",
				new Object[] { protocol, currentName, properties });
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			String propertyString = properties != null ? ";" + properties : "";
			con = DriverManager.getConnection("jdbc:hsqldb:" + protocol + ":" + currentName + propertyString, "sa", "");
			stmt = con.createStatement();
			open = true;
		} catch (Exception e) {
			// System.out.println(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Unused.
	 * 
	 * @param name
	 * @return
	 */
	// @Deprecated
	// public Connection createConnection(String name) {
	// try {
	// Class.forName("org.hsqldb.jdbcDriver");
	// open = true;
	// return DriverManager.getConnection(name, "sa", "");
	// } catch (Exception e) {
	// System.out.println(e);
	// }
	// return null;
	// }

	/**
	 * Unused
	 * 
	 * @param sql
	 */
	// @Deprecated
	// public void createDB(String sql) {
	// try {
	// checkStmt();
	// stmt.execute(sql);
	// } catch (Exception e) {
	// System.out.println(e);
	// }
	// }

	/**
	 * Unused
	 */
	// @Deprecated
	// private void checkStmt() {
	// if (stmt == null) {
	// if (!open) {
	// openDB();
	// } else {
	// try {
	// con.createStatement();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }

	/**
	 * Unused
	 * 
	 * @param sql
	 * @return
	 */
	// @Deprecated
	// public ResultSet execQuery(String sql) {
	// try {
	// checkStmt();
	// return stmt.executeQuery(sql);
	// } catch (Exception e) {
	// System.out.println(e);
	// }
	// return null;
	// }

	public Connection getConnection() {
		if (con != null) {
			return con;
		} else {
			log.debug(
					"Opening connection due to request of a connection; this possibly creates an instable state of the database.");
			openDB();
			return con;
		}
	}

	public void dropTable(String tablename) {
		try {
			stmt.executeUpdate("DROP TABLE " + tablename + " IF EXISTS");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void execUpdate(String sql) {
		try {
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Check if a table already exists
	 * 
	 * @param tableName
	 * @return: true if exists
	 */
	public boolean checkTable(String tableName) {

		if (con == null) {
			con = getConnection();
		}
		try {
			DatabaseMetaData meta = con.getMetaData();
			rs = meta.getTables(null, null, null, new String[] { "TABLE" });
			while (rs.next()) {
				String Name = rs.getString("TABLE_NAME");
				if (Name.contentEquals(tableName) || Name.contentEquals(tableName.toUpperCase())) {
					rs.close();
					return true;
				}
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("Error in chechTable" + e);
		}
		return false;
	}

	public void closeDB() {
		try {
			if (con != null && !con.isClosed()) {
				log.debug("Issuing shutdown of database {} (name: {}, protocol: {}, properties: {})",
						new Object[] { this, currentName, protocol, properties });
				stmt.execute("SHUTDOWN");
				con.close();
				// con = null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void shutdownDB() {
		try {
			if (con != null && !con.isClosed()) {
				log.debug("Issuing shutdown of database {} (name: {}, protocol: {}, properties: {})",
						new Object[] { this, currentName, protocol, properties });
				stmt.execute("SHUTDOWN COMPACT");
				con.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
