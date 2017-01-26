/**
 * JulieXMLBuffer.java
 *
 * Copyright (c) 2010, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: chew
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 13.12.2010
 **/

package de.julielab.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import com.ximpleware.ParseException;
import com.ximpleware.extended.IByteBuffer;
import com.ximpleware.extended.ParseExceptionHuge;

/**
 * Copied from the original XMLBuffer in VTD XML version 2.10. Added the
 * "getFragment(long, long)" method which just does what seemingly "getBytes"
 * was meant for but not yet implemented.
 * 
 * @author chew
 */
public class JulieXMLBuffer implements IByteBuffer {

	byte[][] bufferArray;
	long length;

	public JulieXMLBuffer() {
		// ArrayList al = new ArrayList();
	}

	public JulieXMLBuffer(byte[] ba) {
		if (ba == null)
			throw new IllegalArgumentException("ba can't be null ");
		if (ba.length > 1 << 30)
			throw new IllegalArgumentException(
					"ba should be shorter than 1G bytes ");
		bufferArray = new byte[1][];
		bufferArray[0] = ba;
		length = ba.length;
	}

	/**
	 * 
	 * @param fileName
	 * @throws java.io.IOException
	 * @throws ParseException
	 * 
	 */
	public void readFile(String fileName) throws java.io.IOException,
			ParseExceptionHuge {
		// get file size
		File f = new File(fileName);
		long l = f.length();
		// System.out.println("length ==>"+l);
		length = l;
		if (l >= (1L << 38)) {
			throw new ParseExceptionHuge("document too big > 256 Gbyte");
		}
		// calculate # of buffers needed and each buffer size
		int pageNumber = (int) (l >> 30) + (((l & 0x3fffffffL) == 0) ? 0 : 1);

		bufferArray = new byte[pageNumber][];

		// fill the buffers with doc content
		FileInputStream fis = new FileInputStream(f);
		int byteArrayLen = 0;

		for (int i = 0; i < pageNumber; i++) {
			if (l > (1 << 30)) {
				bufferArray[i] = new byte[1 << 30];
				byteArrayLen = 1 << 30;
			} else {
				bufferArray[i] = new byte[(int) l];
				byteArrayLen = (int) l;
			}
			int offset = 0;
			int numRead = 0;
			int numOfBytes = 1048576;// I choose this value randomly,
			// any other (not too big) value also can be here.
			if (byteArrayLen - offset < numOfBytes) {
				numOfBytes = byteArrayLen - offset;
			}
			while (offset < byteArrayLen
					&& (numRead = fis.read(bufferArray[i], offset, numOfBytes)) >= 0) {
				offset += numRead;
				if (byteArrayLen - offset < numOfBytes) {
					numOfBytes = byteArrayLen - offset;
				}
			}
			// fis.read(bufferArray[i]);
			l = l - (1 << 30);
		}
	}

	/**
     * 
     */
	public final byte byteAt(long index) {
		return bufferArray[(int) (index >> 30)][(int) (index & 0x3fffffff)];
	}

	/**
	 * Return a byte array filled with content from underlying byte storage.
	 * 
	 * @return byte[]
	 * @param offset
	 *            int bytes offset (not UTF char unit)
	 * @param len
	 *            int
	 */
	public byte[] getBytes(int offset, int len) {
		return (byte[]) null;
	}

	/**
	 * Total size in terms of # of bytes.
	 * 
	 * @return int
	 */
	public long length() {
		return length;
	}

	// get the whole XML
	public byte[] getBytes() {
		return null;
	}

	public void writeToFileOutputStream(java.io.FileOutputStream ost, long os,
			long len) throws java.io.IOException {
		// page size is 1<<30
		// then find the remainder
		// ost's page #
		int pageN = (int) (os >> 30);
		// ost's remainder
		int pos = (int) (os & ((1 << 30) - 1));
		// only write to outputStream once
		if (pos + len <= 1 << 30) {
			ost.write(bufferArray[pageN], pos, (int) len);
			return;
		}
		// write the head
		ost.write(bufferArray[pageN], pos, (1 << 30) - pos);
		pageN++;
		len -= (1 << 30) - pos;

		// write the mid sections
		while (len > (1 << 30)) {
			ost.write(bufferArray[pageN], 0, (1 << 30));
			pageN++;
			len -= (1 << 30);
		}

		// write the tail
		ost.write(bufferArray[pageN], 0, (int) len);
		return;
	}

	public byte[] getFragment(long os, long len) throws java.io.IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		// page size is 1<<30
		// then find the remainder
		// ost's page #
		int pageN = (int) (os >> 30);
		// ost's remainder
		int pos = (int) (os & ((1 << 30) - 1));
		// only write to outputStream once
		if (pos + len <= 1 << 30) {
			bos.write(bufferArray[pageN], pos, (int) len);
			return bos.toByteArray();
		}
		// write the head
		bos.write(bufferArray[pageN], pos, (1 << 30) - pos);
		pageN++;
		len -= (1 << 30) - pos;

		// write the mid sections
		while (len > (1 << 30)) {
			bos.write(bufferArray[pageN], 0, (1 << 30));
			pageN++;
			len -= (1 << 30);
		}

		// write the tail
		bos.write(bufferArray[pageN], 0, (int) len);

		return bos.toByteArray();
	}

	@Override
	public void close() {
		
	}

}
