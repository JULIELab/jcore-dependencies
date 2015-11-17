package corpora;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import relations.SenSimplifier;
import utils.DBUtils;

/**
 *
 * @author Chinh
 * @Date: Aug 30, 2010
 */
public class DataSaver {

    Statement stmt;
    ResultSet rs;
    PreparedStatement ps_pro, ps_event, ps_trigger, ps_modify,ps_equiv, ps_abstract;
    Connection con;
    SenSimplifier simp ;
    public DataSaver(DBUtils db) {
        //init database
        try {
            simp = new SenSimplifier(db);
            con = db.getConnection();
            stmt = con.createStatement();
            // preparing tables - DROPPING TABLES
            stmt.executeUpdate("DROP TABLE PROTEIN IF EXISTS");
            stmt.executeUpdate("DROP TABLE TRIGGERS IF EXISTS");
            stmt.executeUpdate("DROP TABLE EVENT IF EXISTS");
            stmt.executeUpdate("DROP TABLE MODIFY IF EXISTS");
            stmt.executeUpdate("DROP TABLE EQUIV IF EXISTS");
            stmt.executeUpdate("DROP TABLE ABSTRACT IF EXISTS");
            
            // CREATING TABLES
//            stmt.executeUpdate("create cached table PROTEIN(PMID VARCHAR(80), TID VARCHAR(15), POS1 INT, POS2 INT, TXT VARCHAR(120))");
            stmt.executeUpdate("create cached table PROTEIN(PMID VARCHAR(80), TID VARCHAR(15), POS1 INT, POS2 INT, TXT CLOB)");
            stmt.executeUpdate("create cached table TRIGGERS(PMID VARCHAR(80), TID VARCHAR(15), T_TYPE VARCHAR(30), POS1 INT, POS2 INT, TXT VARCHAR(80))");
            stmt.executeUpdate("create cached table EVENT(PMID VARCHAR(80), EID VARCHAR(15), T_TYPE VARCHAR(30), TRIG_ID VARCHAR(5), THEME1 VARCHAR(5),THEME2 VARCHAR(5) ,CAUSE VARCHAR(5))");
            stmt.executeUpdate("create cached table MODIFY(PMID VARCHAR(80), MID VARCHAR(15), T_TYPE VARCHAR(30), THEME VARCHAR(5))");
            stmt.executeUpdate("create cached table EQUIV(PMID VARCHAR(80), TID1 VARCHAR(15),  TID2 VARCHAR(50))");
//            stmt.executeUpdate("create cached table ABSTRACT(PMID VARCHAR(80), TEXT VARCHAR(25000))");
            stmt.executeUpdate("create cached table ABSTRACT(PMID VARCHAR(80), TEXT CLOB)");
            
            // DROPPING INDEXES
            stmt.executeUpdate("DROP index PRO_idx  IF EXISTS");
            stmt.executeUpdate("DROP index TRIG_idx  IF EXISTS");
            stmt.executeUpdate("DROP index EVENT_idx  IF EXISTS");
            stmt.executeUpdate("DROP index MODIFY_idx  IF EXISTS");
            stmt.executeUpdate("DROP index ABSTRACT_idx  IF EXISTS");
            stmt.executeUpdate("DROP index EQUIV_idx  IF EXISTS");
            
            // CREATING INDEXES
            stmt.executeUpdate("create index PRO_idx on Protein (pmid,tid)");
            stmt.executeUpdate("create index TRIG_idx on Triggers (pmid,tid)");
            stmt.executeUpdate("create index EVENT_idx on Event (pmid,eid)");
            stmt.executeUpdate("create index MODIFY_idx on Modify (pmid,mid)");
            stmt.executeUpdate("create index EQUIV_idx on EQUIV (pmid,tid1)");
            stmt.executeUpdate("create index ABSTRACT_idx on ABSTRACT (pmid)");
            
            // prepare statements
            ps_pro = con.prepareStatement("Insert into PROTEIN(PMID, TID, POS1, POS2,TXT) VALUES(?,?,?,?,?)");
            ps_trigger = con.prepareStatement("Insert into TRIGGERS(PMID, TID, T_TYPE, POS1, POS2,TXT) VALUES(?,?,?,?,?,?)");
            ps_event = con.prepareStatement("Insert into EVENT(PMID,EID,T_TYPE,TRIG_ID,THEME1,THEME2,CAUSE) VALUES(?,?,?,?,?,?,?)");
            ps_modify = con.prepareStatement("Insert into MODIFY(PMID, MID, T_TYPE, THEME) VALUES(?,?,?,?)");
            ps_equiv = con.prepareStatement("Insert into EQUIV(PMID, TID1, TID2) VALUES(?,?,?)");
            ps_abstract = con.prepareStatement("Insert into ABSTRACT(PMID, TEXT) VALUES(?,?)");

        } catch (Exception e) {
           throw new RuntimeException(e);
        }
    }

    /*
     * Store protein data into Protein table
     */
    public void saveProtein(String pmid, String tid, int p1, int p2, String value) {
        try {
            ps_pro.setString(1, pmid);
            ps_pro.setString(2, tid);
            ps_pro.setInt(3, p1);
            ps_pro.setInt(4, p2);
            ps_pro.setString(5, value);
            ps_pro.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
     * Store protein data into Protein table
     */
    public void saveAbstract(String pmid, String txt) {
        try {
            ps_abstract.setString(1, pmid);
            ps_abstract.setString(2, txt);
            ps_abstract.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Store trigger into Trigger table
     */
    public void saveTrigger(String pmid, String tid, String type,int p1, int p2, String value) {
        try {
            ps_trigger.setString(1, pmid);
            ps_trigger.setString(2, tid);
            ps_trigger.setString(3, type);
            ps_trigger.setInt(4, p1);
            ps_trigger.setInt(5, p2);
            ps_trigger.setString(6, value);
            ps_trigger.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Store event into event table
     */
    public void saveEvent(String pmid, String eid, String type1,String trig_id, String theme1,String theme2, String cause) {
        try {
            ps_event.setString(1, pmid);
            ps_event.setString(2, eid);
            ps_event.setString(3, type1);
            ps_event.setString(4, trig_id);
            ps_event.setString(5, theme1);
            ps_event.setString(6, theme2);
            ps_event.setString(7, cause);
            ps_event.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Store modified values
     */
    public void saveModify(String pmid, String mid, String type, String theme) {
        try {
            ps_modify.setString(1, pmid);
            ps_modify.setString(2, mid);
            ps_modify.setString(3, type);
            ps_modify.setString(4, theme);
            ps_modify.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Store modified values
     */
    public void saveEquiv(String pmid, String tid, String tid2) {
        try {
            ps_equiv.setString(1, pmid);
            ps_equiv.setString(2, tid);
            ps_equiv.setString(3, tid2);
            ps_equiv.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
