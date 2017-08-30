package dragon.nlp.tool;

import dragon.nlp.*;
import dragon.util.*;
import gate.*;
import gate.creole.*;
import gate.corpora.*;
import java.io.*;
import java.util.*;

/**
 * <p>Annie adopted from gate </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class Annie implements NER{
    private SerialAnalyserController annieController;
    private Corpus corpus;
    private Set annotTypesRequired;

    public Annie() throws Exception{
        initAnnie(EnvVariable.getDragonHome()+ "/nlpdata/gate");
    }

    public Annie(String gateHome) throws Exception {
        if(!FileUtil.exist(gateHome) && FileUtil.exist(EnvVariable.getDragonHome()+"/"+gateHome))
                gateHome=EnvVariable.getDragonHome()+"/"+gateHome;
        initAnnie(gateHome);
    }

    public void initAnnie(String gateHome) throws Exception{
        // initialise the GATE library
        Gate.setGateHome(new File(gateHome));
        Gate.setPluginsHome(new File(gateHome));
        Gate.setUserConfigFile(new File(Gate.getGateHome(), "gate.xml"));
        Gate.init();
        Gate.getCreoleRegister().registerDirectories(new File(Gate.getGateHome(), "ANNIE").toURL());

        System.out.println("Initializing ANNIE...");
        // create a serial analyser controller to run ANNIE with
        annieController = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController",
            Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE_" + Gate.genSym());

        // load each PR as defined in ANNIEConstants
        // we don't need the last processing resource: Orthomatcher
        for (int i = 0; i < ANNIEConstants.PR_NAMES.length-1; i++) {
            FeatureMap params = Factory.newFeatureMap(); // use default parameters
            ProcessingResource pr = (ProcessingResource) Factory.createResource(ANNIEConstants.PR_NAMES[i], params);
            annieController.add(pr);
        }
        System.out.println("...ANNIE loaded");

        corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
        annotTypesRequired = new HashSet();
        annotTypesRequired.add("Person");
        annotTypesRequired.add("Location");
        annotTypesRequired.add("Organization");
    }

    public void setAnnotationTypes(String[] arrType){
        int i;

        if(arrType==null || arrType.length==0) return;

        annotTypesRequired = new HashSet();
        for(i=0;i<arrType.length;i++)
            annotTypesRequired.add(arrType[i]);
    }

    public void close(){
        annieController.cleanup();
    }

    public ArrayList extractEntities(String content){
        DocumentImpl doc;
        AnnotationSet defaultAnnotSet;
        Annotation curAnnotation;
        SortedArray entityList;
        Iterator it;
        Token curToken;

        try{
            content=content.replaceAll("\r\n"," ");
            content=content.replace('\r', ' ');
            content=content.replace('\n', ' ');

            entityList=new SortedArray();
            doc = new DocumentImpl();
            doc.setStringContent(content);
            doc.init();
            corpus.clear();
            corpus.add(doc);
            annieController.setCorpus(corpus);
            annieController.execute();

            defaultAnnotSet = doc.getAnnotations();
            defaultAnnotSet= defaultAnnotSet.get(annotTypesRequired);
            if(defaultAnnotSet==null) return null;

            it = defaultAnnotSet.iterator();
            while(it.hasNext()){
                curAnnotation=(Annotation)it.next();
                curToken=new Token(content.substring(curAnnotation.getStartNode().getOffset().intValue(), curAnnotation.getEndNode().getOffset().intValue()));
                if(!entityList.add(curToken)){
                    curToken=(Token)entityList.get(entityList.insertedPos());
                    curToken.addFrequency(1);
                }
            }
            doc.cleanup();
            return entityList;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList extractFromDoc(dragon.nlp.Document doc){
        Sentence sent;
        Paragraph pg;
        ArrayList curTermList, termList;
        try {
            termList = new ArrayList(60);
            pg = doc.getFirstParagraph();
            while (pg != null) {
                sent = pg.getFirstSentence();
                while (sent != null) {
                    curTermList = extractFromSentence(sent);
                    if (curTermList != null) {
                        termList.addAll(curTermList);
                        curTermList.clear();
                    }
                    sent = sent.next;
                }
                pg = pg.next;
            }
            return termList;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList extractFromSentence(Sentence sent) {
        ArrayList termList;
        SortedArray annotationList;
        AnnotationSet defaultAnnotSet;
        Annotation annot;
        Iterator it;
        Term curTerm;
        Word curWord, startWord, endWord;
        StringBuffer content;
        DocumentImpl doc;
        int i, start, end;

        try {
            content = new StringBuffer();
            curWord = sent.getFirstWord();
            while (curWord != null) {
                if (content.length() > 0)
                    content.append(' ');
                curWord.setOffset(content.length());
                content.append(curWord.getContent());
                curWord = curWord.next;
            }
            if(content.toString().trim().length()<=20)
                return null;

            doc = new DocumentImpl();
            doc.setStringContent(content.toString());
            doc.init();
            corpus.clear();
            corpus.add(doc);
            annieController.setCorpus(corpus);
            annieController.execute();
            defaultAnnotSet = doc.getAnnotations();
            defaultAnnotSet= defaultAnnotSet.get(annotTypesRequired);
            if(defaultAnnotSet==null)
                return null;

            annotationList=new SortedArray(new AnnotationComparator());
            it = defaultAnnotSet.iterator();
            if(it==null)
                return null;
            while (it.hasNext())
                annotationList.add(it.next());

            termList=new ArrayList(annotationList.size());
            curWord=sent.getFirstWord();
            for(i=0;i<annotationList.size();i++){
                annot=(Annotation)annotationList.get(i);
                start=annot.getStartNode().getOffset().intValue();
                end=annot.getEndNode().getOffset().intValue();
                while(curWord.getOffset()<start)
                    curWord=curWord.next;
                if(curWord.getOffset()==start){
                    startWord = curWord;
                    endWord=curWord;
                    curWord=curWord.next;
                    while (curWord != null && curWord.getOffset() < end){
                        endWord=curWord;
                        curWord = curWord.next;
                    }
                    curTerm=new Term(startWord,endWord);
                    curTerm.setTUI(annot.getType());
                    termList.add(curTerm);
                    if(startWord.getAssociatedConcept()!=null)
                        startWord.setAssociatedConcept(curTerm);
                    curWord=startWord;
                }
            }
            return termList;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String annotate(String original) {
        DocumentImpl doc;
        AnnotationSet defaultAnnotSet;
        try{
            doc = new DocumentImpl();
            doc.setStringContent(original);
            doc.init();
            corpus.clear();
            corpus.add(doc);
            annieController.setCorpus(corpus);
            annieController.execute();

            defaultAnnotSet = doc.getAnnotations();
            defaultAnnotSet= defaultAnnotSet.get(annotTypesRequired);
            return "<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n<doc>"+doc.toXml(defaultAnnotSet, false)+"</doc>";
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private class AnnotationComparator implements Comparator {
        public int compare(Object firstObj, Object secondObj) {
            long start1, start2;
            long end1, end2;

            start1 = ( (Annotation) firstObj).getStartNode().getOffset().longValue();
            start2 = ( (Annotation) secondObj).getStartNode().getOffset().longValue();
            if (start1 < start2)
                return -1;
            else if (start1 == start2) {
                end1 = ( (Annotation) firstObj).getEndNode().getOffset().longValue();
                end2 = ( (Annotation) secondObj).getEndNode().getOffset().longValue();
                if (end1 > end2)
                    return -1;
                else if (end1 < end2)
                    return 1;
                else
                    return 0;
            }
            else
                return 1;
        }
    }
}
