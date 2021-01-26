package dragon.ir.index.sentence;

import dragon.nlp.Sentence;
import dragon.nlp.extract.DualConceptExtractor;

/**
 * <p>Two dimensional sentence indexer </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DualSentenceIndexer extends AbstractSentenceIndexer {
    private BasicSentenceWriteController firstWriter, secondWriter;
    private DualConceptExtractor cptExtractor;

    public DualSentenceIndexer(DualConceptExtractor extractor, String firstIndexFolder, String secondIndexFolder){
        this(extractor,false,firstIndexFolder,false,secondIndexFolder);
    }

    public DualSentenceIndexer(DualConceptExtractor extractor, boolean useConcept, String firstIndexFolder, String secondIndexFolder){
        this(extractor,useConcept,firstIndexFolder,useConcept,secondIndexFolder);
    }

    public DualSentenceIndexer(DualConceptExtractor extractor, boolean firstUseConcept, String firstIndexFolder,
                               boolean secondUseConcept, String secondIndexFolder) {
        super(extractor.getDocumentParser());
        this.cptExtractor = extractor;
        firstWriter = new BasicSentenceWriteController(firstIndexFolder,false, firstUseConcept);
        secondWriter = new BasicSentenceWriteController(secondIndexFolder,false,secondUseConcept);
    }

    public boolean indexedSentence(String sentKey){
        return firstWriter.indexed(sentKey);
    }

    public boolean index(Sentence sent, String sentKey) {
        try {
            if (firstWriter.indexed(sentKey)) {
                return false;
            }
            firstWriter.addRawSentence(sent);
            cptExtractor.initDocExtraction();
            cptExtractor.extractFromSentence(sent);
            firstWriter.write(cptExtractor.getFirstConceptList());
            secondWriter.write(cptExtractor.getSecondConceptList());
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void initialize() {
        if (initialized) {
            return;
        }
        firstWriter.initialize();
        secondWriter.initialize();
        initialized = true;
    }

    public void close() {
        firstWriter.close();
        secondWriter.close();
        super.close();
    }
}