package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

public class NodePairSaveSource extends Pipe
{
	public NodePairSaveSource()
	{
	}

	public Instance pipe (Instance carrier) 
	{
		NodePair pair = (NodePair)carrier.getData();
		Citation s1 = (Citation)pair.getObject1();
		Citation s2 = (Citation)pair.getObject2();

		carrier.setSource(new String( "Citation1:"+s1.getOrigString()+"\nCitation2:"+s2.getOrigString() ) );
		return carrier;
	}


}
