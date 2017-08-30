package dragon.onlinedb.pubmed;

import dragon.onlinedb.BasicArticle;

/**
 * <p>Data structure for PubMed article</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class PubMedArticle extends BasicArticle  {
    private String[] arrMeSH;

    public PubMedArticle(){
        arrMeSH=null;
    }

    public PubMedArticle(String rawText) {
        arrMeSH=null;
        parseDef(rawText);
    }

    private boolean parseDef(String content){
        int start, end;
        String mesh;
        //get PMID
        start = content.indexOf("pmid") + 4;
        end = content.indexOf(",", start);
        key = content.substring(start, end).trim();

        //get Title
        start = end;
        start = content.indexOf("title {");
        if(start>=0){
            start=content.indexOf('\"',start)+1;
            end = content.indexOf("\"\n", start);
            title = content.substring(start, end).replace('\n',' ');
        }

        //get Abstract
        start = end;
        start = content.indexOf("abstract \"");
        if(start>=0){
            start=start+10;
            end = content.indexOf("\",\n    ", start);
            abt = content.substring(start, end);
        }

        //get MeSH Terms
        start = end;
        start = content.indexOf("mesh {");
        if(start>=0){
            start=start+6;
            end = content.indexOf("\n    },", start);
            mesh = content.substring(start, end);
            start=mesh.indexOf("term \"");
            while(start>=0)
            {
                start=start+6;
                end=mesh.indexOf("\"",start);
                if(meta==null)
                    meta=mesh.substring(start,end);
                else
                    meta=meta+","+mesh.substring(start,end);
                start=mesh.indexOf("term \"",end);
            }
            if(meta!=null)
                arrMeSH=meta.split(",");
        }
        return true;
    }

    public int getMeSHNum(){
        if(arrMeSH==null)
            return 0;
        else
            return arrMeSH.length;
    }

    public String getMainHeading(int index)
    {
        return arrMeSH[index].toString() ;
    }
}