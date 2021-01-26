/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

/** 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.projects.seg_plus_coref.clustering;

import edu.umass.cs.mallet.base.pipe.iterator.AbstractPipeInputIterator;
import edu.umass.cs.mallet.base.types.Instance;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.regex.Pattern;


/** Iterate over groups of lines of text, separated by lines that
		match a regular expression.  For example, the WSJ BaseNP data
		consists of sentences with one word per line, each sentence
		separated by a blank line.  If the "boundary" line is to be
		included in the group, it is placed at the end of the group. */

public class LineGroupIterator2 extends AbstractPipeInputIterator
{
	LineNumberReader reader;
	Pattern lineBoundaryRegex;
	boolean skipBoundary;
	//boolean putBoundaryLineAtEnd; // Not yet implemented
	String nextLineGroup;
	int groupIndex = 0;

	Object referenceNo;
	Object clusterNo;
	Object clusterNo_true = null;

	String refNoMeta = "reference_no=";
	String clusterNoMeta = "cluster_no=";
	String clusterNoMeta_true = "true_id=";

	public LineGroupIterator2 (Reader input, Pattern lineBoundaryRegex, boolean skipBoundary)
	{
		this.reader = new LineNumberReader (input);
		this.lineBoundaryRegex = lineBoundaryRegex;
		this.skipBoundary = skipBoundary;
		this.nextLineGroup = getNextLineGroup();
	}

	// added by Fuchun Peng
	public String getLineGroup ()
	{
		return nextLineGroup;
	}
	// added by Fuchun Peng
	public void nextLineGroup()
	{
		nextLineGroup = getNextLineGroup();
	}

	public String getNextLineGroup ()
	{
		StringBuffer sb = new StringBuffer ();
		String line;

		Pattern metaRegex = Pattern.compile("<meta .*></meta>");
		while (true) {
			try {
				line = reader.readLine();
			} catch (IOException e) {
				throw new IllegalStateException ();
			}

			//System.out.println ("LineGroupIterator2: got line: "+line);
			if (line == null) {
				break;
			}
			else if(metaRegex.matcher (line).matches()){
				int indexRefNo_start = line.indexOf(refNoMeta) + refNoMeta.length() + 1;
				int indexRefNo_end = line.indexOf("\"", indexRefNo_start ) ;
			
				int indexClusterNo_start = line.indexOf(clusterNoMeta) + clusterNoMeta.length() + 1;
				int indexClusterNo_end = line.indexOf("\"", indexClusterNo_start) ;

				int indexClusterNo_true_start = line.indexOf(clusterNoMeta_true) + clusterNoMeta_true.length() + 1;
				int indexClusterNo_true_end = -1;
				if(line.indexOf(clusterNoMeta_true) >= 0) 
					indexClusterNo_true_end = line.indexOf("\"", indexClusterNo_true_start) ;

//				System.out.println(line);
//				System.out.println(indexRefNo_start + "/" + indexRefNo_end);
//				System.out.println(indexClusterNo_start + "/" + indexClusterNo_end);

				referenceNo = line.substring(indexRefNo_start, indexRefNo_end);
				clusterNo = line.substring(indexClusterNo_start, indexClusterNo_end);

				if(indexClusterNo_true_end > indexClusterNo_true_start)
					clusterNo_true = line.substring(indexClusterNo_true_start, indexClusterNo_true_end);

//				System.out.println(refNoMeta + referenceNo);
//				System.out.println(clusterNoMeta + clusterNo);
//				System.out.println(clusterNoMeta_true + clusterNo_true);

			} 
			else if (lineBoundaryRegex.matcher (line).matches()) {

				if (!skipBoundary) {
					sb.append(line);
					sb.append('\n');
				}
				if (sb.length() > 0)
					break;
			} else {

				sb.append(line);
				sb.append('\n');
			}
		}

//		System.out.println("\n" + sb);

		if (sb.length() == 0)
			return null;
		else
			return sb.toString();
	}
	
	// The PipeInputIterator interface

	public Instance nextInstance ()
	{
		assert (nextLineGroup != null);
//		Instance carrier = new Instance (nextLineGroup, null, "linegroup"+groupIndex++, null);
		Instance carrier = null;
		if(clusterNo_true != null)
			carrier = new Instance (nextLineGroup, null, referenceNo + ":" + clusterNo + ":" + clusterNo_true, null);
		else	
			carrier = new Instance (nextLineGroup, null, referenceNo + ":" + clusterNo, null);
				
	
		nextLineGroup = getNextLineGroup ();


		return carrier;
	}

	public boolean hasNext ()	{	return nextLineGroup != null;	}
	
}
