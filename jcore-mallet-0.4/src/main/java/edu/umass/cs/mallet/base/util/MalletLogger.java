/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.base.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MalletLogger extends Logger
{

	// Initialize the java.util.logging.config properties to the MALLET default config file
	// in edu.umass.cs.mallet.base.util.resources.logging.properties

	//Create an arry that allows us to reference the java logging levels by a simple integer.
	static public Level[] LoggingLevels = {Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO,
	                                       Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST,
	                                       Level.ALL};

	static {
		if (System.getProperty("java.util.logging.config.file") == null
				&& System.getProperty("java.util.logging.config.class") == null) {
			System.setProperty("java.util.logging.config.class",
												 "edu.umass.cs.mallet.base.util.Logger.DefaultConfigurator");
			try {
				InputStream s = MalletLogger.class.getResourceAsStream ("resources/logging.properties");
				if (s == null)
					throw new IOException ();
				LogManager.getLogManager().readConfiguration(s);
				Logger.global.config ("Set java.util.logging properties from "+
															MalletLogger.class.getPackage().getName() + "/resources/logging.properties");
			} catch (IOException e) {
				System.err.println ("Couldn't open "+MalletLogger.class.getName()+" resources/logging.properties file.\n"
														+" Perhaps the 'resources' directories weren't copied into the 'class' directory.\n"
														+" Continuing.");
			}
		}
	}

	protected MalletLogger (String name, String resourceBundleName)
	{
		super (name, resourceBundleName);
	}

	public static Logger getLogger (String name)
	{
		return Logger.getLogger (name);
	}

	/** Convenience method for finding the root logger.
	*/
	public Logger getRootLogger()
	{
		Logger rootLogger = this;
		while (rootLogger.getParent() != null) {
			rootLogger = rootLogger.getParent();
		}
		return rootLogger;
	}


}
