/**
 * XMLTools.java
 *
 * Copyright (c) 2012, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 07.05.2012
 **/

/**
 * 
 */
package de.julielab.xml;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

import java.io.*;
import java.util.*;

/**
 * Simple program that deletes a single XPath from an XML document.
 * 
 */
public class ElementDeleter {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out
					.println("Usage: "
							+ ElementDeleter.class.getName()
							+ " <XML file> <XPath expression>");
			System.out.println("Removes all occurrences of an XPath in an XML file.");
			System.out.println("WARNING: Operates on the XML in place! Make a BACKUP before running this program!");
			System.exit(1);
		}
		String fileName = args[0];
		String xpath = args[1];

		VTDGen vg = new VTDGen();
		vg.parseFile(fileName, false);
		final VTDNav nav = vg.getNav();

		AutoPilot ap = new AutoPilot(nav);
		ap.selectXPath(xpath);

		XMLModifier mod = new XMLModifier(nav);
		while (ap.evalXPath() != -1) {
			mod.remove();
		}
		mod.output(fileName);

	}
}
