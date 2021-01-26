/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.projects.seg_plus_coref.ie; // Generated package name

import edu.umass.cs.mallet.base.fst.Transducer;
import edu.umass.cs.mallet.base.fst.TransducerEvaluator;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.util.ArrayUtils;
import edu.umass.cs.mallet.base.util.MalletLogger;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created: May 12, 2004
 * 
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: FieldF1Evaluator.java,v 1.2 2005/01/04 03:31:43 casutton Exp $
 */
public class FieldF1Evaluator extends TransducerEvaluator {

  private static final Logger logger = MalletLogger.getLogger(FieldF1Evaluator.class.getName());

  String[] segmentTags;

  public FieldF1Evaluator (String[] segmentTags)
  {
    this.segmentTags = segmentTags;
  }

  // Pair that delineates boundaries of one segment in the sequence.
  private static class Segment {
    int start;
    int end;
    int tag;

    Segment (int t, int s, int e)
    {
      tag = t;
      start = s;
      end = e;
    }

    public int hashCode ()
    {
      return start ^ end;
    }

    public boolean equals (Object o)
    {
      Segment seg = (Segment) o;
      return (start == seg.start) && (end == seg.end) && (tag == seg.tag);
    }

  }

  public void test (Transducer transducer, InstanceList data, String description, PrintStream viterbiOutputStream)
  {
    int[] ntrue = new int[segmentTags.length];
    int[] npred = new int[segmentTags.length];
    int[] ncorr = new int[segmentTags.length];

    LabelAlphabet dict = (LabelAlphabet) transducer.getInputPipe().getTargetAlphabet();

    for (int i = 0; i < data.size(); i++) {
      Instance instance = data.getInstance(i);
      Sequence input = (Sequence) instance.getData();
      Sequence trueOutput = (Sequence) instance.getTarget();
      assert (input.size() == trueOutput.size());
      Sequence predOutput = transducer.viterbiPath(input).output();
      assert (predOutput.size() == trueOutput.size());

      List trueSegs = new ArrayList();
      List predSegs = new ArrayList();

      addSegs(trueSegs, trueOutput);
      addSegs(predSegs, predOutput);

//      System.out.println("FieldF1Evaluator instance "+instance.getName ());
//      printSegs(dict, trueSegs, "True");
//      printSegs(dict, predSegs, "Pred");

      for (Iterator it = predSegs.iterator(); it.hasNext();) {
        Segment seg = (Segment) it.next();
        npred[seg.tag]++;
        if (trueSegs.contains(seg)) {
          ncorr[seg.tag]++;
        }
      }

      for (Iterator it = trueSegs.iterator(); it.hasNext();) {
        Segment seg = (Segment) it.next();
        ntrue[seg.tag]++;
      }

    }

    DecimalFormat f = new DecimalFormat("0.####");
    logger.info(description + " per-field F1");
    for (int tag = 0; tag < segmentTags.length; tag++) {
      double precision = ((double) ncorr[tag]) / npred[tag];
      double recall = ((double) ncorr[tag]) / ntrue[tag];
      double f1 = (2 * precision * recall) / (precision + recall);
      Label name = dict.lookupLabel(segmentTags [tag]);
      logger.info(" segments " + name + "  true = " + ntrue[tag] + "  pred = " + npred[tag] + "  correct = " + ncorr[tag]);
      logger.info(" precision=" + f.format(precision) + " recall=" + f.format(recall) + " f1=" + f.format(f1));
    }
  }

  private void addSegs (List segs, Sequence output)
  {
    int segtype = -1;
    int startidx = -1;

    for (int j = 0; j < output.size(); j++) {
//      System.out.println("addSegs j="+j);
      Object tag = output.get(j);
      segtype = ArrayUtils.indexOf(segmentTags, tag.toString());

      if (segtype > -1) {
//        System.out.println("...found segment "+tag);
        // A new segment is starting
        startidx = j;
        while (j < output.size() - 1) {
//          System.out.println("...inner addSegs j="+j);
          j++;
          Object nextTag = output.get(j);
          if (!nextTag.equals(tag)) {
            j--;
            segs.add(new Segment(segtype, startidx, j));
            segtype = startidx = -1;
            break;
          }
        }
      }
    }

    // Handle end-of-sequence
    if (startidx > -1) {
      segs.add (new Segment (segtype, startidx, output.size () - 1));
    }

  }

  private void printSegs (LabelAlphabet dict, List segs, String desc)
  {
    System.out.println(desc + " segments:");
    for (Iterator it = segs.iterator(); it.hasNext();) {
      Segment seg = (Segment) it.next();
      Label lbl = dict.lookupLabel(segmentTags [seg.tag]);
      System.out.println(lbl + " [ " + seg.start + " " + seg.end + " ]");
    }
  }

}
