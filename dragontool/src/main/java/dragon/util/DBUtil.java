package dragon.util;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.sql.*;
/**
 * <p>Basic database utility operations</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DBUtil {
    public DBUtil() {
    }

    public static void executeQuery(Connection con,String sql){
        Statement st;

        try{
            st=con.createStatement();
            st.executeUpdate(sql);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void executeBatchQuery(Connection con,String scriptFile){
        Statement st;
        BufferedReader br;
        String line;

        try{
            br=FileUtil.getTextReader(scriptFile);
            st = con.createStatement();
            while((line=br.readLine())!=null){
                System.out.println(line);
                st.executeUpdate(line);
            }
            System.out.println("Done!");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    public static void printResultSet(Connection con, String sql, PrintWriter out){
        ResultSet rs;

        rs=getResultSet(con,sql);
        printResultSet(rs,out);
        try{
            rs.close();
            rs.getStatement().close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void printResultSet(ResultSet rs, PrintWriter out){
        printResultSet(rs,out,0);
    }

    public static void printResultSet(ResultSet rs, PrintWriter out, int top){
        int i, fieldNum,count;
        ResultSetMetaData rsMeta;

        try{
            count=0;
            rsMeta=rs.getMetaData();
            fieldNum=rsMeta.getColumnCount();
            while(rs.next() && (count<top || top<=0))
            {
                for(i=0;i<fieldNum;i++)
                    out.write(rs.getString(i+1)+"\t");
                out.write("\n");
                out.flush();
                count++;
            }
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Connection getAccessCon(String file)
    {
         try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            // set this to a MS Access DB you have on your machine
            String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
            database+=file+";DriverID=22;READONLY=true}";
            // now we can get the connection from the DriverManager
            return DriverManager.getConnection( database ,"","");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Connection getMSSQLCon(String server, String db, String uid, String pwd)
    {
         try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            // set this to a MS SQL Server
            String database = "jdbc:odbc:Driver={SQL Server};Server="+server;
            database+=";Database="+db+";UID="+uid+";PWD="+pwd+";";
            // now we can get the connection from the DriverManager
            return DriverManager.getConnection( database ,"","");
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static Connection getMSSQL2000Con(String server, String db, String uid, String pwd)
    {
         try {
            Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
            // set this to a MS SQL Server
            String database = "jdbc:microsoft:sqlserver://"+server+":1433;";
            database+="databasename="+db+";user="+uid+";password="+pwd+";";
            // now we can get the connection from the DriverManager
            return DriverManager.getConnection(database);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Connection getMSSQL2005Con(String server, String db, String uid, String pwd)
    {
         try {

            //Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // set this to a MS SQL Server
            String database = "jdbc:sqlserver://"+server+":8081;";
            database+="databasename="+db+";user="+uid+";password="+pwd+";";
            // now we can get the connection from the DriverManager
            return DriverManager.getConnection(database);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


	public static Connection getDB2Connection(String server, String db, String uid, String pwd) {
		String sConnect;

        sConnect ="jdbc:db2://"+server+":50000/"+db;
		try  {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            Connection con=java.sql.DriverManager.getConnection(sConnect,uid,pwd);
            return con;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	  }

    public static void closeConnection(Connection con){
        try{
			con.close();
		}
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static ResultSet getResultSet (Connection con, String sql){
        try{
            Statement st=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs=st.executeQuery(sql);
            return rs;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static int getRecordCount(ResultSet rs)
    {
        if(rs==null) return 0;

        try {
            if (rs.getType() == ResultSet.TYPE_FORWARD_ONLY)
                return 0;
            else {
                int pos = rs.getRow();
                rs.last();
                int count = rs.getRow();
                if(pos==0) rs.first();
                else rs.absolute(pos);

                return count;
            }
        }
        catch (SQLException e) {
            return 0;
        }
    }
}