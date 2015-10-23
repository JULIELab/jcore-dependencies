/** 
 * UnkownFormatException.java
 * 
 * Copyright (c) 2007, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: faessler
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 30.08.2007 
 * 
 * The exception class related to an unkown format passed to a Converter object.
 **/

/**
 * 
 */
package de.julielab.segmentationEvaluator;

/**
 * @author faessler
 *
 */
public class UnknownFormatException extends Exception {
  
  public String toString() {
    return "Converter.TYPE_IOB or Converter.TYPE_IO expected";
  }
  
}
