//package dragon.nlp.tool;
//
//import dragon.util.*;
//import edu.cmu.cs.linkgrammar.*;
///**
// * <p>Link Grammar class adopted from edu.cmu.cs.linkgrammar </p>
// * <p> </p>
// * <p>Copyright: Copyright (c) 2003</p>
// * <p>Company: IST, Drexel University</p>
// * @author Davis Zhou
// * @version 1.0
// */
//
//public class LinkGrammar {
//    private Dictionary lgd;
//    private ParseOptions lgp;
//    private Sentence lgs;
//
//    public LinkGrammar() {
//        this(EnvVariable.getDragonHome()+ "/nlpdata/linkgrammar", 100,1);
//    }
//
//    public LinkGrammar(int maxLinkage,int maxParseTime){
//        this(EnvVariable.getDragonHome()+ "/nlpdata/linkgrammar",maxLinkage,maxParseTime);
//    }
//
//    public LinkGrammar(String workDir, int maxLinkage,int maxParseTime) {
//        if(!FileUtil.exist(workDir) && FileUtil.exist(EnvVariable.getDragonHome()+"/"+workDir))
//                workDir=EnvVariable.getDragonHome()+"/"+workDir;
//        lgd = new Dictionary(workDir+"/4.0.dict", "4.0.knowledge", "4.0.constituent-knowledge", "4.0.affix");
//        lgp = new ParseOptions();
//        lgp.parseOptionsSetShortLength(10);
//        lgp.parseOptionsSetMaxNullCount(10);
//        lgp.parseOptionsSetLinkageLimit(maxLinkage);
//        lgp.parseOptionsSetMaxParseTime(maxParseTime);
//    }
//
//    public static void main(String[] args) {
//        LinkGrammar lg=new LinkGrammar();
//        String example="";
//
//        for(int i=0;i<args.length;i++) example=example+" "+args[i];
//        if(example.length()==0) 
//        	example="I love you";
//        Sentence lgs=lg.parse(example);
//
//        if(lgs.sentenceNumLinkagesFound()<1)
//        {
//            lgs.sentenceDelete();
//            System.out.println("No linkage was found.");
//        }
//        else
//        {
//            Linkage link=lg.getLinkage(0);
//            System.out.println(link.linkagePrintDiagram());
//            System.out.println(lg.getConstituentTree(link,1));
//            System.out.println(lg.getConstituentTree(link,2));
//            link.linkageDelete();
//            lgs.sentenceDelete();
//        }
//        lg.close();
//    }
//
//    public Sentence parse(String sent){
//        boolean resource_exhausted;
//
//        lgs = new Sentence ( sent, lgd );
//        lgs.parse(lgp);
//        resource_exhausted=(lgp.parseOptionsResourcesExhausted()>0);
//        if(resource_exhausted)
//            lgp.parseOptionsResetResources();
//        return lgs;
//    }
//
//    public Linkage getLinkage(int index)
//    {
//        return new Linkage(index,lgs,lgp);
//    }
//    
//    /**
//     * Print out the constituent tree
//     * @param linkage the linkage returned by the getLinkage method
//     * @param mode 1: nested Lisp format 2: a flat tree is displayed using brackets
//     * @return the constituent tree
//     */
//    public String getConstituentTree(Linkage linkage, int mode){
//        Constituent lgc;
//        String tree;
//        
//        lgc= new Constituent(linkage);
//        tree=lgc.printConstituentTree(linkage, mode);
//        lgc.linkageFreeConstituentTree( );
//        return tree;
//    }
//
//    public void close(){
//        lgd.dictionaryDelete();
//        lgp.parseOptionsDelete();
//    }
//
//
//}