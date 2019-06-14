/** 
 * Converter.java
 * 
 * Copyright (c) 2006, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: faessler
 * 
 * Current version: 1.0.4
 * Since version:   1.0
 *
 * Creation date: Aug 23, 2007 
 * 
 * An object in charge of converting text in IO or IOB format to the corresponding object sequence (of IO- or IOBToken). 
 **/

package de.julielab.segmentationEvaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;


public class Converter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Converter.class);
	
	public static final int TYPE_IO = 1;

	public static final int TYPE_IOB = 2;

	private int mode;

	public Converter(int mode) throws UnknownFormatException {

		if (mode != TYPE_IO && mode != TYPE_IOB) {
			throw new UnknownFormatException();
		}
		this.mode = mode;
	}

	/**
	 * converts a line into an IOToken. The following format is assumed:
	 * token<whitespaces>label
	 * 
	 * whitespaces can be any WS characters of arbitrary number
	 * 
	 * @param line
	 * @return
	 */
	private IOToken lineToIOToken(String line) {
		String[] parts = null;
		IOToken ioToken =  null;
		// replace blanc lines by outside tags
		if (line.equals("")) {
			line = "O\tO";
		}

		// split at arbitrary number of white spaces
		parts = line.split("\\s+");
		
		if (parts.length == 2) {
				//&& !(parts[0].equals("O") && parts[1].equals("O"))) {
			if (mode == TYPE_IO) {
				ioToken =  new IOToken(parts[0], parts[1]);

			} else {
				ioToken =  new IOBToken(parts[0], parts[1]);
			}
		} else {
			LOGGER.error("input format incorrect: two many columns in current line: " + line);
			System.exit(-1);
		}
		return ioToken;
	}
	
	/**
	 * @param src
	 *            File in IO or IOB Format to be converted to IOTokens.
	 * @return An array of IOTokens, representing the tokens of the input file.
	 * @throws FileNotFoundException
	 */
	public IOToken[] textToIOTokens(File src) throws FileNotFoundException {

		if (!src.exists()) {
			throw new FileNotFoundException();
		}

		BufferedReader br = null;
		ArrayList<IOToken> retList = new ArrayList<IOToken>();
		IOToken[] ret = new IOToken[0];
		String line = null;

		try {
			br = new BufferedReader(new FileReader(src));

			while ((line = br.readLine()) != null) {
				retList.add(lineToIOToken(line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		ret = retList.toArray(ret);

		return ret;
	}

	/**
	 * converts an arraylist which consists of elements of lines in io(b) format
	 * 
	 * @param lines
	 *            in IO or IOB Format to be converted to IOTokens.
	 * @return An array of IOTokens, representing the tokens of the input file.
	 * @throws FileNotFoundException
	 */
	public IOToken[] textToIOTokens(ArrayList<String> lines) {

		ArrayList<IOToken> retList = new ArrayList<IOToken>();
		IOToken[] ret = new IOToken[0];
		for (String line : lines) {
			retList.add(lineToIOToken(line));
		}
		ret = retList.toArray(ret);
		return ret;
	}

}
