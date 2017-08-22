package martin.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class MyConnection {
	private String user;
	private String pass;
	private String host;
	private int port;
	private String schema;
	private Logger logger;
	private Connection conn;
	private final int RECONNECT_SLEEP_TIME = 10000;

	public MyConnection(String user, String pass, String host, int port, String schema, Logger logger) {
		this.user = user;
		this.pass = pass;
		this.host = host;
		this.port = port;
		this.schema = schema;
		this.logger = logger;
	}
	
	public Connection getConn() {
		try {
			if (conn == null || conn.isClosed()){
				boolean retry = true;
				boolean sleep = false;
				
				while (retry){
					try{
						if (sleep)
							Thread.sleep(RECONNECT_SLEEP_TIME);
						
						if (logger != null)
							logger.info("%t: Connecting to MySQL database " + schema
									+ " at " + user + "@" + host + ":" + port + "... ");

						Class.forName("com.mysql.jdbc.Driver").newInstance();

						conn = DriverManager.getConnection("jdbc:mysql://"
								+ host + ":" + port + "/" + schema + "?autoReconnect=true&user=" + user
								+ "&password=" + pass  + "&netTimeoutForStreamingResults=3600");

						if (logger != null)
							logger.info("%t: Done, connected to SQL server.\n");
						
						retry=false;
						
					} catch (Exception e){
						logger.warning("%t: SQL connection failed (" + e.toString() + "). Trying again in " + RECONNECT_SLEEP_TIME + " ms...");
						sleep=true;
					}
				}			
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return conn;			
	}
	
	public Connection reconnect(){
		try{
			conn.close();			
		} catch (Exception e){
		}
		
		conn = null;
		
		return getConn();
	}
}
