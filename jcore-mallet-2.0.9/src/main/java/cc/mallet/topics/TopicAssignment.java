package cc.mallet.topics;

import cc.mallet.types.Instance;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.Labeling;

import java.io.Serializable;

/** This class combines a sequence of observed features
 *   with a sequence of hidden "labels".
 */

public class TopicAssignment implements Serializable {
	public Instance instance;
	public LabelSequence topicSequence;
	public Labeling topicDistribution;
                
	public TopicAssignment (Instance instance, LabelSequence topicSequence) {
		this.instance = instance;
		this.topicSequence = topicSequence;
	}
}
