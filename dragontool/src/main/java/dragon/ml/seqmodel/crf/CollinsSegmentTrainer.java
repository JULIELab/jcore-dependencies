package dragon.ml.seqmodel.crf;

import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.feature.FeatureGenerator;
import dragon.ml.seqmodel.model.ModelGraph;

/**
 * <p>Collins segment trainer</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CollinsSegmentTrainer extends CollinsBasicTrainer{
    private int maxSegmentLength;

    public CollinsSegmentTrainer(ModelGraph model, FeatureGenerator featureGenerator, int maxSegmentLength) {
        super(model,featureGenerator);
        this.maxSegmentLength =maxSegmentLength;
    }

    protected Labeler getLabeler(){
        return new ViterbiSegmentLabeler(model, featureGenerator, maxSegmentLength);
    }

    protected int getSegmentEnd(DataSequence dataSeq, int start) {
        return dataSeq.getSegmentEnd(start);
    }
}