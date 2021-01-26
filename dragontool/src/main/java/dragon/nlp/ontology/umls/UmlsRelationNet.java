package dragon.nlp.ontology.umls;

import dragon.matrix.DoubleFlatSparseMatrix;
import dragon.util.DBUtil;
import dragon.util.FileUtil;
import dragon.util.SortedArray;

import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.ResultSet;

/**
 * <p>UMLS relation net for relation operations </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class UmlsRelationNet {
    protected DoubleFlatSparseMatrix matrix;
    protected UmlsSTYList list;

    public UmlsRelationNet(String relationFile,UmlsSTYList list) {
        this.list=list;
        loadRelations(relationFile);
    }

    public UmlsRelationNet(Connection con,UmlsSTYList list) {
        this.list=list;
        loadRelations(con);
    }

    private boolean loadRelations(String filename){
        BufferedReader br;
        String line;
        String[] arrField;
        int first, second;
        double  relation;

        try{
            br=FileUtil.getTextReader(filename);
            matrix=new DoubleFlatSparseMatrix();
            while((line=br.readLine())!=null){
                arrField=line.split("\t");
                first=list.lookup(arrField[0]).getIndex();
                second=list.lookup(arrField[2]).getIndex();
                relation=list.lookup(arrField[1]).getIndex()+1;
                matrix.add(first,second,relation);
            }
            br.close();
            matrix.finalizeData(false);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadRelations(Connection con){
        ResultSet rs;
        int first, second;
        double  relation;

        try{
            rs=DBUtil.getResultSet(con,"select * from SRSTRE1");
            matrix=new DoubleFlatSparseMatrix();
            while(rs.next()){
                first=list.lookup(rs.getString(1)).getIndex();
                second=list.lookup(rs.getString(3)).getIndex();
                relation=list.lookup(rs.getString(2)).getIndex()+1;
                matrix.add(first,second,relation);
            }
            rs.close();
            rs.getStatement().close();
            matrix.finalizeData(false);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String[] getRelations(String[] arrFirstST, String[] arrSecondST){
       SortedArray typeList;
       String[] arrTypes;
       String relation;
       int i,j;
       int first;

        if(arrFirstST==null || arrSecondST==null) return null;

        typeList=new SortedArray(3);
        for(i=0;i<arrFirstST.length;i++)
        {
            first=list.lookup(arrFirstST[i]).getIndex();
            for(j=0;j<arrSecondST.length;j++){
                relation = getRelations(first,list.lookup(arrSecondST[j]).getIndex());
                if (relation != null) {
                   typeList.add(relation);
                }
            }
        }
        if(typeList.size()>0){
            arrTypes=new String[typeList.size()];
            for(i=0;i<typeList.size();i++)
                arrTypes[i]=(String)typeList.get(i);
            return arrTypes;
        }
        else
            return null;
    }

    public String getRelations(String firstST,String secondST){
        if(firstST==null || secondST==null)
            return null;

        return getRelations(list.lookup(firstST).getIndex(),list.lookup(secondST).getIndex());
    }

    public String getRelations(int firstST, int secondST){
        int index;

        index=(int)matrix.get(firstST,secondST)-1;
        if(index<=0) //we assume the relation is undirected.
            index=(int)matrix.get(secondST,firstST)-1;

        if(index>0)
            return ((UmlsSTY)list.get(index)).toString();
        else
            return null;
    }

    public boolean isSemanticRelated(String[] arrFirstST, String[] arrSecondST){
        int i,j;
        int first;

        if(arrFirstST==null || arrSecondST==null) return false;
        for(i=0;i<arrFirstST.length;i++){
            first=list.lookup(arrFirstST[i]).getIndex();
            for (j = 0; j < arrSecondST.length; j++)
                if (isSemanticRelated(first, list.lookup(arrSecondST[j]).getIndex()))
                    return true;
        }
        return false;
    }

    public boolean isSemanticRelated(String firstST,String secondST){
        if(firstST==null || secondST==null)
            return false;
        return isSemanticRelated(list.lookup(firstST).getIndex(),list.lookup(secondST).getIndex());
    }

    public boolean isSemanticRelated(int firstST, int secondST){
        return (matrix.get(firstST,secondST)>0 || matrix.get(secondST,firstST)>0);
    }
}