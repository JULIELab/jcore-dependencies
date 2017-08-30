package dragon.nlp.ontology.mesh;

import dragon.matrix.*;
import dragon.nlp.*;
import dragon.nlp.compare.*;
import dragon.nlp.ontology.*;
import dragon.util.*;
import java.util.*;

/**
 * <p>Similarity measures of comparing two MeSH nodes </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Drexel University</p>
 * @author Xiaodan Zhang
 * @version 1.0
 */

public class MeshSimilarity implements SimilarityMetric{
    //declared similarity measure by author's name
    public static final int MIN_LEN = 0;
    public static final int WU_PALMER = 1;
    public static final int LI = 2;
    public static final int LEACOCK = 3;
    public static final int JING = 4;
    public static final int LIN = 5;
    public static final int JIANG = 6;
    public static final int RESINK = 7;
    public static final int MAO = 8;
    public static final int FEATURE=9;
    public static final int KNAPPE=10;

    private MeshNodeList meshNodeList;
    private SortedArray maxTaxoLenList;
    private int similarityMode;

    public MeshSimilarity(String hierFile, int similarityMode){
        meshNodeList = new MeshNodeList(hierFile);
        if(similarityMode==LEACOCK)
            maxTaxoLenList=genMaxTaxoLenList();
        else
            maxTaxoLenList=null;
        this.similarityMode =similarityMode;
    }

    public String getParent(String path){
        if (path.indexOf(".") < 0)
            return null;
        return path.substring(0, path.lastIndexOf("."));
    }

    public String[] getAncestors(String path){
        int i;
        String[] ancestors;
        ArrayList parents;

        if(path.indexOf(".")<0){
            ancestors = new String[1];
            ancestors[0] = path;
            return ancestors;
        }

        parents = new ArrayList(5);
        while(path.indexOf(".")>0){
            path=path.substring(0, path.lastIndexOf("."));
            parents.add(path);
        }
        ancestors = new String[parents.size()];
        for(i=0;i<ancestors.length;i++){
            ancestors[i]=(String)parents.get(i);
        }
        return ancestors;
    }

    public String[] getSharedAncestors(String path1, String path2){
        int i,j;
        String[] ancestors1,ancestors2,sharedAncestors;
        ArrayList sharedParents;

        if(path1.equals(path2))
            return getAncestors(path1);
        ancestors1=getAncestors(path1);
        ancestors2=getAncestors(path2);
        sharedParents= new ArrayList(5);
        for(i=0;i<ancestors1.length;i++){
            for(j=0;j<ancestors2.length;j++){
                if(ancestors1[i].equals(ancestors2[j]))
                    sharedParents.add(ancestors1[i]);
            }
        }
        if(sharedParents.size()==0) return null;
        sharedAncestors = new String[sharedParents.size()];
        for(i=0;i<sharedAncestors.length;i++)
            sharedAncestors[i]=(String)sharedParents.get(i);
        return sharedAncestors;
    }

    public String[] getUnionAncestors(String path1, String path2){
        int i;
        SortedArray unionParents;
        String[] ancestors1,ancestors2,unionAncestors;

        if(path1.equals(path2))
            return getAncestors(path1);
        ancestors1=getAncestors(path1);
        ancestors2=getAncestors(path2);
        unionParents= new SortedArray(new AlphabetaComparator());
        for(i=0;i<ancestors1.length;i++)
            unionParents.add(ancestors1[i]);
        for(i=0;i<ancestors2.length;i++)
            unionParents.add(ancestors2[i]);

        unionAncestors= new String[unionParents.size()];
        for(i=0;i<unionAncestors.length;i++)
            unionAncestors[i]=(String)unionParents.get(i);
        return unionAncestors;
    }

    public String getRoot(String path){
        if (path.indexOf(".") < 0)
            return path;
        return path.substring(0, path.indexOf("."));
    }

    public boolean isRoot(String path){
        if (path.indexOf(".") < 0)
            return true;
        return false;
    }

    public String getCommonParent(String path1,String path2){
        String temp;
        if(!getRoot(path1).equals(getRoot(path2)))
            return null;

        if(getDepth(path2)>getDepth(path1)){
            temp = path1;
            path1=path2;
            path2=temp;
        }
        while (path1 != null) {
            while (path2 != null) {
                if (path1.indexOf(path2) >= 0)
                    return path2;
                else if (path2.indexOf(path1) >= 0)
                    return path1;
                path2 = getParent(path2);
            }
            path1 = getParent(path1);
        }
        return null;
    }

    public int getDescendantNum(String path){
        MeshNode cur;

        cur=meshNodeList.lookup(path);
        if(cur==null)
            return 0;
        else
            return cur.getDescendantNum();
    }

    public ArrayList getDescendant(String path){
        ArrayList nodeList;
        MeshNode cur,child;
        int pos;

        pos=meshNodeList.binarySearch(new MeshNode(path));
        nodeList = new ArrayList();
        cur = (MeshNode) meshNodeList.get(pos);
        while(pos<=meshNodeList.size()-2){
            pos++;
            child=(MeshNode) meshNodeList.get(pos);
            if(!child.getPath().startsWith(cur.getPath()))
                break;
            nodeList.add(child);
        }
        return nodeList;
    }

    public int getMaxPathLen(String path){
        ArrayList nodeList;
        int i,max,length;

        nodeList = getDescendant(path);
        if (nodeList.size() == 0)
            return getDepth(path);

        max = 0;
        for (i = 0; i < nodeList.size(); i++) {
            length = getDepth( ( (MeshNode) nodeList.get(i)).getPath());
            if (max < length)
                max = length;
        }
        return max;
    }

    public int getMinLen(String path1, String path2){
        String coParent;
        int parLen;
        coParent=getCommonParent(path1,path2);
        if(coParent==null)
            return -1;
        parLen=getDepth(coParent);
        return getDepth(path1)+getDepth(path2)-2*parLen;
    }

    public int getMinLen(String path1, String path2, String coParent){
        int parLen;
        if(coParent==null)
            return -1;
        parLen=getDepth(coParent);
        return getDepth(path1)+getDepth(path2)-2*parLen;
    }

    public int getDepth(String path){
        int count;
        if(path.indexOf(".")<0)
            return 1;
        count=0;
        while(getParent(path)!=null){
            path=getParent(path);
            count++;
        }
        count++;
        return count;
    }

    private double getWuPalmerSimilarity(String path1, String path2){
        String coParent;
        int parLen;
        coParent = getCommonParent(path1, path2);
        if (coParent == null)
            return -1;
        parLen = getDepth(coParent);
        return (2*parLen)/(double)(getDepth(path1)-parLen + getDepth(path2)-parLen + 2 * parLen);
    }

   private double getLiSimilarity(String path1, String path2,double minPathScaler, double parentDepthScaler) {
        String coParent;
        int parentDepth,path1Depth,path2Depth;
        double dist,minLen;
        coParent = getCommonParent(path1, path2);
        if (coParent == null)
            return -1;
        parentDepth = getDepth(coParent);
        path1Depth = getDepth(path1);
        path2Depth = getDepth(path2);
        minLen = path1Depth+path2Depth-2*parentDepth;
        dist = Math.exp(-minPathScaler*minLen);
        dist = dist*(Math.exp(parentDepthScaler*parentDepth)-Math.exp(-parentDepthScaler*parentDepth));
        dist = dist/(Math.exp(parentDepthScaler*parentDepth)+Math.exp(-parentDepthScaler*parentDepth));
        return dist;
    }

    private double getFeatureSimilarity(String path1, String path2){
        double join, union;
        String[] sharedAncestors;
        sharedAncestors=getSharedAncestors(path1,path2);
        if(sharedAncestors==null) return 0;
        join = sharedAncestors.length;
        union = getUnionAncestors(path1,path2).length;
        return (join+1)/(union+1);
    }

    private double getKnappeSimilarity(String path1, String path2){
        double gen,speci,join;
        String[] ancestors1,ancestors2,sharedAncestors;

        if(path1.equals(path2)) return 1;
        ancestors1=getAncestors(path1);
        ancestors2=getAncestors(path2);
        sharedAncestors=getSharedAncestors(path1,path2);
        if(sharedAncestors ==null)return 0;
        join = sharedAncestors.length;
        gen=ancestors1.length;
        speci=ancestors2.length;
        return 0.5*(join/gen)+0.5*(join/speci);
    }

    private double getLeacockSimilarity(String path1, String path2){
        int minLen,pos;
        double value;
        String coParent,root;
        Token token;
        coParent = getCommonParent(path1,path2);
        if (coParent == null)
            return -1;
        minLen = getMinLen(path1,path2,coParent);
        root =getRoot(coParent);
        pos=maxTaxoLenList.binarySearch(root);
        token =(Token) maxTaxoLenList.get(pos);
        value = (double)(minLen+1)/2/token.getWeight();
        return -Math.log(value);
    }

    private double getJingSimilarity(String path1, String path2){
        double dist,coParentDepth;
        String coParent;

        coParent = getCommonParent(path1,path2);
        if(coParent==null) return -1;
        coParentDepth=getDepth(coParent);
        dist=1/coParentDepth;
        dist=dist*0.5;
        dist=dist*((getDepth(path1)-coParentDepth)/getMaxPathLen(path1)+(getDepth(path2)-coParentDepth)/getMaxPathLen(path2));
        return 1-dist;
    }

    private double getLinSimilarity(String path1, String path2){
        String coParent;
        if(path1.equals(path2))
            return 1;
        coParent = getCommonParent(path1,path2);
        if(coParent==null) return -1;
        return 2*meshNodeList.lookup(coParent).getWeight()/(meshNodeList.lookup(path1).getWeight()+meshNodeList.lookup(path2).getWeight());
    }

    private double getJiangSimilarity(String path1, String path2){
        String coParent;

        coParent = getCommonParent(path1, path2);
        if (coParent == null)
            return -1;
        return -meshNodeList.lookup(path1).getWeight() - meshNodeList.lookup(path2).getWeight() +
            2 * meshNodeList.lookup(coParent).getWeight();
    }

    private double getResinkSimilarity(String path1, String path2){
        double dist;
        String coParent;

        if(path1.equals(path2)){
            dist = -meshNodeList.lookup(path1).getWeight();
            if (dist == 0)
                dist = 0.11;
                return dist;
        }
        coParent = getCommonParent(path1,path2);
        if(coParent==null) return -1;
        dist= -meshNodeList.lookup(coParent).getWeight();
        if (dist == 0)
                dist = 0.11;
        return dist;
    }

    private double getMaoSimilarity(String path1, String path2){
        int c1,c2;
        double len;

        len=getMinLen(path1,path2);
        if (len == 0)
            return 1;
        else if (len == -1)
            return -1;
        c1 = getDescendantNum(path1);
        c2 = getDescendantNum(path2);
        return 0.9/len/Math.log(2+c1+c2);
    }

   public MeshNodeList getMeshNodeList(){
       return meshNodeList;
   }

   private SortedArray genMaxTaxoLenList() {
       int i, depth;
       double max;
       SortedArray maxTaxoLenList;
       Token token;
       MeshNode meshNode;
       String curRoot, oldRoot, maxPath;

       maxTaxoLenList = new SortedArray(107, new AlphabetaComparator());
       oldRoot = "A01";
       maxPath = "A01";
       max = 0;
       for (i = 0; i < meshNodeList.size(); i++) {
           meshNode = (MeshNode) meshNodeList.get(i);
           curRoot = getRoot(meshNode.getPath());
           if (!curRoot.equals(oldRoot)) {
               token = new Token(getRoot(maxPath));
               token.setWeight(max);
               maxTaxoLenList.add(token);
               max = 0;
               oldRoot = curRoot;
           }
           depth = getDepth(meshNode.getPath());
           if (max < depth) {
               max = depth;
               maxPath = meshNode.getPath();
           }
           oldRoot = curRoot;
       }
       maxTaxoLenList.add(new Token(getRoot(maxPath)));
       ((Token)maxTaxoLenList.get(maxTaxoLenList.size()-1)).setWeight(max);
       for(i=0;i<maxTaxoLenList.size();i++){
           token =((Token)maxTaxoLenList.get(i));
       }
       return maxTaxoLenList;
   }

    public void trainWeightByInformationContent(ArrayList trainings) {
        MeshNode node;
        String cur;
        double ic;
        int i;

        for(i=0;i<meshNodeList.size();i++){
            node = (MeshNode) meshNodeList.get(i);
            node.setFrequency(0);
        }

        for (i = 0; i < trainings.size(); i++) {
            node=(MeshNode)trainings.get(i);
            cur=node.getPath();
            meshNodeList.lookup(cur).addFrequency(node.getFrequency());
            while ( getParent(cur) != null){
                cur = getParent(cur);
                meshNodeList.lookup(cur).addFrequency(node.getFrequency());
            }
        }

        for(i=0;i<meshNodeList.size();i++){
            node = (MeshNode) meshNodeList.get(i);
            if(node.getFrequency()==0)
                node.setWeight(0);
            else{
                ic = (double) node.getFrequency() / meshNodeList.lookup(getRoot(node.getPath())).getFrequency();
                node.setWeight(Math.log(ic));
            }
        }
    }

    public void genSimilarityMatrix(String matrixFile, double threshold) {
        DoubleSuperSparseMatrix matrix;
        MeshNode meshNodeI,meshNodeJ;
        int i, j,start,end;
        double dist;

        start=0;
        end=0;
        matrix= new DoubleSuperSparseMatrix(matrixFile, false, false);
        for(i=0;i<meshNodeList.size();i++){
            meshNodeI = (MeshNode) meshNodeList.get(i);
            if(meshNodeI.getPath().indexOf(".")<0){
                start = i;
                end = i+meshNodeI.getDescendantNum();
                System.out.println(new Date()+" "+start+" "+end);
            }
            for(j=start;j<end;j++){
                meshNodeJ = (MeshNode)meshNodeList.get(j);
                dist = getSimilarity(meshNodeI.getPath(),meshNodeJ.getPath(),similarityMode);
                if(dist>threshold){
                    matrix.add(i,j,dist);
                    matrix.add(j,i,dist);
                }
            }
        }
        matrix.finalizeData();
    }

    private double getSimilarity(String path1, String path2, int mode){
        if (mode == MIN_LEN) {
            return getMinLen(path1,path2);
        }
        else if (mode == WU_PALMER) {
            return getWuPalmerSimilarity(path1,path2);
        }
        else if (mode == LI) {
            return getLiSimilarity(path1,path2,0.6,0.2);
        }
        else if (mode == LEACOCK) {
            return getLeacockSimilarity(path1,path2);
        }
        else if (mode == JING) {
            return getJingSimilarity(path1,path2);
        }
        else if (mode == LIN) {
            return getLinSimilarity(path1,path2);
        }
        else if (mode == JIANG) {
            return getJiangSimilarity(path1,path2);
        }
        else if (mode == RESINK) {
            return getResinkSimilarity(path1,path2);
        }
        else if (mode == MAO) {
            return getMaoSimilarity(path1,path2);
        }
        else if(mode==FEATURE){
            return getFeatureSimilarity(path1,path2);
        }
        else if(mode==KNAPPE){
            return getKnappeSimilarity(path1,path2);
        }
        return -1;
    }

    public double getSimilarity(String path1, String path2){
        return getSimilarity(path1,path2,similarityMode);
    }

    public double getSimilarity(Term a, Term b){
        return getSimilarity(a.getCUI(),b.getCUI(),similarityMode);
    }
}