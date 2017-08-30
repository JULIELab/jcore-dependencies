package dragon.nlp.ontology.umls;

import dragon.util.SortedArray;
import dragon.util.FileUtil;
import java.sql.*;
import java.util.*;
import java.io.*;

/**
 * <p>List for storing UMLS semantic semantic types</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class UmlsSTYList extends SortedArray {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args){
        UmlsSTYList list;

        list=new UmlsSTYList("sir/semantictype.list");
        System.out.println(list.lookup("T028").getDescription());
    }

    public UmlsSTYList(String styFile) {
        loadSTYList(styFile);
    }

    public UmlsSTYList(Connection con) {
        loadSTYList(con);
    }


    public UmlsSTY styAt(int index){
        return (UmlsSTY)get(index);
    }

    public UmlsSTY lookup(String tui){
        int pos;

        pos=binarySearch(new UmlsSTY(0,tui,null,null,false));
        if(pos<0)
            return null;
        else
            return (UmlsSTY)get(pos);
    }

    public UmlsSTY lookup(UmlsSTY tui){
        int pos;

        pos=binarySearch(tui);
        if(pos<0)
            return null;
        else
            return (UmlsSTY)get(pos);
    }

    private boolean loadSTYList(String filename){
        BufferedReader br;
        String line;
        String[] arrField;
        int i, total;
        boolean isRelation;
        ArrayList list;
        UmlsSTY cur;

        try{
            br=FileUtil.getTextReader(filename);
            line=br.readLine();
            total=Integer.parseInt(line);
            list=new ArrayList(total);

            for(i=0;i<total;i++){
                line=br.readLine();
                arrField=line.split("\t");
                if(arrField[0].equals("STY"))
                    isRelation=false;
                else
                    isRelation=true;
                cur=new UmlsSTY(i,arrField[1],arrField[2],arrField[3],isRelation);
                list.add(cur);
            }
            br.close();
            Collections.sort(list);
            this.addAll(list);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadSTYList(Connection con){
        String sql;
        Statement st;
        ResultSet rs;
        ArrayList list;
        UmlsSTY cur;
        int count;
        boolean isRelation;

        try{
            sql="select RT, UI, STYRL,STNRTN from SRDEF";
            st=con.createStatement();
            rs=st.executeQuery(sql);
            count=0;
            list=new ArrayList();

            while(rs.next())
            {
                if(rs.getString("RT").equalsIgnoreCase("STY"))
                    isRelation=false;
                else
                    isRelation=true;
                cur=new UmlsSTY(count,rs.getString("UI"),rs.getString("STYRL"),rs.getString("STNRTN"),isRelation);
                list.add(cur);
            }
            rs.close();
            st.close();
            Collections.sort(list);
            this.addAll(list);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}