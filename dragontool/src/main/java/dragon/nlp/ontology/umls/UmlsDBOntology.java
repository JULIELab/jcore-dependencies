package dragon.nlp.ontology.umls;

import dragon.nlp.ontology.*;
import dragon.nlp.tool.*;
import dragon.util.DBUtil;
import java.sql.*;

/**
 * <p>UMLS Ontology has functions of reading ontology from database</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class UmlsDBOntology extends UmlsExactOntology implements Ontology{
    private Connection con;
    private UmlsSemanticNet snNet;

    public UmlsDBOntology(Connection con,Lemmatiser lemmatiser) {
        super(lemmatiser);
        this.con=con;

        UmlsSTYList styList=new UmlsSTYList(con);
        UmlsRelationNet relationNet=new UmlsRelationNet(con,styList);
        snNet=new UmlsSemanticNet(this,styList,relationNet);
    }

    public Connection getConnection(){
        return con;
    }

    public void closeConnection(){
        try{
            if (con != null){
                con.close();
                con=null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    public SemanticNet getSemanticNet(){
        return snNet;
    }

    public String[] getSemanticType(String[] cuis){
        StringBuffer sSQL;

        if(cuis==null) return null;
        sSQL=new StringBuffer("select distinct tui from mrsty where cui in (");
        for(int i=0;i<cuis.length;i++){
            sSQL.append('\'');
            sSQL.append(cuis[i]);
            sSQL.append('\'');
            if(i<cuis.length-1)
                sSQL.append(',');
            else
                sSQL.append(')');
        }
        return readSemanticTypes(sSQL.toString());
    }

    public String[] getSemanticType(String cui){
        String sSQL;

        sSQL="select distinct tui from mrsty where cui='"+ cui +"'";
        return readSemanticTypes(sSQL);
    }

    private String[] readSemanticTypes(String sql){
        ResultSet rs;
        int count,i;
        String[] arrTUI;

        try{
            rs = DBUtil.getResultSet(con,sql);
            count=DBUtil.getRecordCount(rs);
            if(count==0)
            {
                arrTUI=null;
            }
            else
            {
                arrTUI=new String[count];
                i=0;
                while (i<count) {
                    arrTUI[i]=rs.getString("tui");
                    rs.next();
                    i++;
                }
            }
            rs.close();
            rs.getStatement().close();
            return arrTUI;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    public String[] getCUI(String term){
        String sSQL;
        ResultSet rs;
        int count,i;
        String[] arrCUI;

        sSQL = "select distinct cui from mrxns_eng where NSTR='"+term+"'";
        try{
            rs = DBUtil.getResultSet(con,sSQL);
            count=DBUtil.getRecordCount(rs);
            if(count==0)
            {
                arrCUI=null;
            }
            else
            {
                arrCUI=new String[count];
                i=0;
                while (i<count) {
                    arrCUI[i]=rs.getString("cui");
                    rs.next();
                    i++;
                }
            }
            rs.close();
            rs.getStatement().close();
            return arrCUI;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isTerm(String term){
        String sSQL;
        ResultSet rs;
        int count;

        sSQL = "select cui from mrxns_eng where NSTR='"+term+"' fetch first 1 rows only";
        try{
            rs = DBUtil.getResultSet(con,sSQL);
            count=DBUtil.getRecordCount(rs);
            rs.close();
            rs.getStatement().close();
            return (count>0);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}