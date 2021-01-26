/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
http://www.cs.umass.edu/~mccallum/mallet
This software is provided under the terms of the Common Public License,
version 1.0, as published by http://www.opensource.org.  For further
information, see the file `LICENSE' included with this distribution. */


/**
 @author Ben Wellner
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import edu.umass.cs.mallet.base.pipe.iterator.AbstractPipeInputIterator;
import edu.umass.cs.mallet.base.types.Instance;

import java.util.*;
import java.util.logging.Logger;


public class NodePairIterator extends AbstractPipeInputIterator
{

	  private static Logger logger = Logger.getLogger(NodePairIterator.class.getName());
	
	  List nodes;
    List pairArray;
    int currentIndex;
    int pairCount;
  	java.util.Random r;
	
    public NodePairIterator (List nodes) {
			this (nodes, 1.0);
		}
	
    public NodePairIterator (List nodes, double negativeProb) {
			this.r = new java.util.Random( 1 );
			this.nodes = nodes;
			this.pairArray = new ArrayList();
			for (int i=0; i < nodes.size(); i++) {
				for (int j=i-1; j >= 0; j--) {
					//Node n = (Node)nodes.get(j);
					Citation n = (Citation)nodes.get(j);
					String n_label = (String)n.getLabel();
					String i_label = (String)((Citation)nodes.get(i)).getLabel();
					if (n_label.equals(i_label)){
						pairArray.add(new NodePair(nodes.get(j), nodes.get(i), true));
//					System.out.println(i + "/" + nodes.size() + " :" + j);
					}
					else {
						float f = r.nextFloat();
						if (f <= negativeProb)
							pairArray.add(new NodePair(nodes.get(j), nodes.get(i), false));
					}
				}
			}
			currentIndex = 0;
			pairCount = pairArray.size();
			
			logger.fine("Number of pairs: "  + pairCount);
    }
	
    /**
     * NodePairIterator Constructor (2 arg overload).  Instead of all pairs
     * of nodes (N choose 2), this constructor only makes the pairs passed
     * in a list.  Each element of the list is assumed to be an array of 2
     * Integers.
     * @param nodes
     * @param pairs
     */
    public NodePairIterator (List nodes, List pairs) {
        this.nodes = nodes;
        this.pairArray = new ArrayList();

        Map idToNode = new HashMap();
        Iterator iter = nodes.iterator();
        while (iter.hasNext()) {
            Node n = (Node)iter.next();
            idToNode.put(new Integer(n.index), n);
            //System.out.println(n.index);
        }

        iter = pairs.iterator();
        while (iter.hasNext()) {
            Object[] pair = (Object[])iter.next();
            assert(pair.length == 2);
            Integer index1 = (Integer) pair[0];
            Integer index2 = (Integer) pair[1];
            logger.fine(index1 + ", " + index2);
            Node n1 = (Node)idToNode.get(index1);
            Node n2 = (Node)idToNode.get(index2);
            String n1_label = (String)n1.getLabel();
            String n2_label = (String)n2.getLabel();
            assert(index1.intValue() == n1.getIndex());
            assert(index2.intValue() == n2.getIndex());
            if (n1_label.equals(n2_label)) {
                pairArray.add(new NodePair(n1, n2, true));
            } else {
                pairArray.add(new NodePair(n1, n2, false));
            }
        }
        currentIndex = 0;
        pairCount = pairArray.size();

        logger.fine("..."  + pairCount);
    }

    public boolean hasNext () {
        return (currentIndex < pairCount);
    }

    public Instance nextInstance () {
        if (currentIndex < pairCount) {
            String label;
            NodePair np = (NodePair)pairArray.get(currentIndex);
            currentIndex++;
            if (np.getIdRel())
                label = "yes";
            else
                label = "no";
            return new Instance(np, label, null, null);
        } else {return null;}
    }
    public Object next () {
        return (Object)nextInstance();
    }
    public void remove () { throw new UnsupportedOperationException(); }

}
