package cc.mallet.fst;

import cc.mallet.types.Sequence;

import java.io.Serializable;

public abstract class MaxLatticeFactory implements Serializable {
	
	public MaxLattice newMaxLattice (Transducer trans, Sequence inputSequence)
	{
		return newMaxLattice (trans, inputSequence, null);
	}
	
	// You may pass null for output
	public abstract MaxLattice newMaxLattice (Transducer trans, Sequence inputSequence, Sequence outputSequence);

}
