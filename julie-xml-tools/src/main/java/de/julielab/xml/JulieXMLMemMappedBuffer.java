/**
 * JulieXMLMemMappedBuffer.java
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
 * Creation date: 10.12.2012
 **/

/**
 * 
 */
package de.julielab.xml;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;

import com.ximpleware.extended.IByteBuffer;
import com.ximpleware.extended.ParseExceptionHuge;

/**
 * Copied from
 * <code>XMLMemMappedBuffer<code> of VTD XML 2.11 and added <code>writeToPipe</code>
 * for having a method to get fragments of an XML document without writing these
 * fragments directly to a file.
 * 
 * @author faessler
 * 
 */
public class JulieXMLMemMappedBuffer implements IByteBuffer {

	MappedByteBuffer input[];
	FileChannel fc;
	RandomAccessFile raf;
	String fn;
	long length;

	public JulieXMLMemMappedBuffer() {

	}

	public long length() {
		return length;
	}

	public byte byteAt(long index) {
		return input[(int) (index >> 30)].get((int) (index & 0x3fffffff));
	}

	public void readFile(String fileName) throws java.io.IOException,
			ParseExceptionHuge {
		File f = new File(fileName);
		fn = fileName;
		long l = f.length();
		length = l;
		if (l >= (1L << 38)) {
			throw new ParseExceptionHuge("document too big > 256 Gbyte");
		}
		raf = new RandomAccessFile(fileName, "r");
		fc = raf.getChannel();
		int pageNumber = (int) (l >> 30) + (((l & 0x3fffffffL) == 0) ? 0 : 1);

		input = new MappedByteBuffer[pageNumber];
		long l2 = 0;
		for (int i = 0; i < pageNumber; i++) {
			if (i < (pageNumber - 1)) {
				// bufferArray[i] = new byte[1<<30];
				input[i] = fc.map(FileChannel.MapMode.READ_ONLY, l2, 1 << 30);
				l2 = l2 + (1 << 30);
			} else {
				// bufferArray[i] = new byte[(int)l];
				input[i] = fc.map(FileChannel.MapMode.READ_ONLY, l2, l
						- ((long) i << 30));
			}
			// input[i] = new RandomAccessFile(fileName, "r").getChannel()
			// .map(FileChannel.MapMode.READ_ONLY, 0,(1<<32)-1);
		}
		// if (fc!=null)
		// fc.close();
		// if (raf!=null)
		// raf.close();
	}

	/**
	 * NOt implemented yet
	 */
	public byte[] getBytes() {
		return null;
	}

	/**
	 * not implemented yet
	 */
	public byte[] getBytes(int offset, int len) {
		return (byte[]) null;
	}

	/**
	 * write the segment (denoted by its offset and length) into a file output
	 * file stream
	 */
	public void writeToFileOutputStream(java.io.FileOutputStream ost, long os,
			long len) throws java.io.IOException {

		FileChannel ostChannel = ost.getChannel();

		fc.transferTo(os, len, ostChannel);

	}

	/**
	 * write the segment (denoted by its offset and length) into an output file
	 * stream
	 */
	public void writeToPipe(Pipe pipe, long os, long len)
			throws java.io.IOException {

		SinkChannel sinkChannel = pipe.sink();

		fc.transferTo(os, len, sinkChannel);

	}

	@Override
	public void close() {
		
	}
}
