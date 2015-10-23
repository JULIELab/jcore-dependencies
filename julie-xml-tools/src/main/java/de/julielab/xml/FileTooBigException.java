/**
 * FileTooBigException.java
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
 * Creation date: 07.12.2012
 **/

/**
 * 
 */
package de.julielab.xml;

import java.io.IOException;

/**
 * @author faessler
 *
 */
public class FileTooBigException extends IOException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6848219329348733045L;

	/**
	 * 
	 */
	public FileTooBigException() {
		super();
	}
	
	public FileTooBigException(String msg) {
		super(msg);
	}
}

