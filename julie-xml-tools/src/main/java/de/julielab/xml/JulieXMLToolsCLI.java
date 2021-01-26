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

import java.io.*;
import java.util.*;

/**
 * Simple CLI to offer the tools not only as a library.
 * 
 * @author faessler
 * 
 */
public class JulieXMLToolsCLI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out
					.println("Usage: "
							+ JulieXMLToolsCLI.class.getName()
							+ " <XML file> <XPath expression> [xml | text] [output directory] [XPath to record ID for output file names]");
			System.out
					.println("The option 'xml' will result whole XML fragments to be returned in contrast to only text contents.");
			System.out
					.println("If you specify an output directory, each result record of the first XPath expression will be written to a file.");
			System.out
					.println("If you specify a record ID XPath, the output file names will correspond to the value of this field. NOTE: This XPath must be given RELATIVE to the first XPath! I.e. without leading / and from the point where the first XPath ended.");
			System.exit(1);
		}
		String fileName = args[0];
		String xpath = args[1];
		String option = args.length > 2 ? args[2].toLowerCase() : null;
		String outputDir = args.length > 3 ? args[3] : null;
		String recordIdXPath = args.length > 4 ? args[4] : null;

		List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
		Map<String, String> field = new HashMap<String, String>();
		field.put(JulieXMLConstants.NAME, "value");
		field.put(JulieXMLConstants.XPATH, ".");
		if (option != null && option.equals("xml")) {
			field.put(JulieXMLConstants.RETURN_XML_FRAGMENT, "true");
		}
		fields.add(field);
		if (recordIdXPath != null) {
			field = new HashMap<String, String>();
			field.put(JulieXMLConstants.NAME, "id");
			field.put(JulieXMLConstants.XPATH, recordIdXPath);
			fields.add(field);
		}

		Iterator<Map<String, Object>> rowIterator = JulieXMLTools.constructRowIterator(fileName, 1024, xpath, fields, false);

		PrintStream out = null;
		if (outputDir == null) {
			try {
				out = new PrintStream(System.out, true, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		// If no ID XPath is given or there is no ID value.
		int idCounter = 0;
		while (rowIterator.hasNext()) {
			Map<String, Object> row = rowIterator.next();
			String fragment = (String) row.get("value");
			String id = (String) (recordIdXPath != null ? row.get("id") : String.valueOf(idCounter++));
			if (outputDir == null) {
				out.println(fragment);
			} else {
				String outputFileName = outputDir + File.separator + id;
				if (option.equals("xml"))
					outputFileName = outputFileName + ".xml";
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName))) {
					bw.write(fragment);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
