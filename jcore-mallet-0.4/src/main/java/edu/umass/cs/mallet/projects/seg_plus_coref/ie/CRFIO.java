/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */


package edu.umass.cs.mallet.projects.seg_plus_coref.ie;

import edu.umass.cs.mallet.base.fst.CRF;

import java.io.*;

public class CRFIO {

	public static void writeCRF(String filename, CRF crf) {
		File f = new File(filename);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
			oos.writeObject(crf);
			oos.close();
		}
		catch (IOException e) {
			System.err.println("Exception writing CRF file: " + e);
		}

	}

	public static CRF readCRF(String filename) {
		CRF crf = null;
		File f = new File(filename);
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			crf = (CRF) ois.readObject();
			ois.close();
		}
		catch (IOException e) {
			System.err.println("Exception reading crf file: " + e);
			crf = null;
		}
		catch (ClassNotFoundException cnfe) {
			System.err.println("Cound not find class reading in object: " + cnfe);
			crf = null;
		}
		return crf;
	}
}
