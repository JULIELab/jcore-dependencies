package cc.mallet.fst;

import cc.mallet.fst.Transducer.State;
import cc.mallet.types.Sequence;

import java.util.List;

/** The interface to classes implementing the Viterbi algorithm, 
 * finding the best sequence of states for a given input sequence. */
public interface MaxLattice {
	public double getDelta (int inputPosition, int stateIndex);
	public Sequence<Object> bestOutputSequence ();
	public List<Sequence<Object>> bestOutputSequences (int n);
	public Sequence<State> bestStateSequence ();
	public List<Sequence<State>> bestStateSequences (int n);
	public Transducer getTransducer ();
	public double elementwiseAccuracy (Sequence referenceOutput);
}
