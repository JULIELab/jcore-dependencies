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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

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
			throw new IllegalArgumentException("ba should be shorter than 1G bytes ");
		bufferArray = new byte[1][];
		bufferArray[0] = ba;
		length = ba.length;
	}

	/**
	 * JULIE Lab adopted version of the method. Does support GZIP files. GZIP
	 * files must end with ".gz" or ".gzip" in order to be recognized.
	 * 
	 * @param fileName
	 * @throws java.io.IOException
	 * @throws ParseException
	 * 
	 */
	public void readFile(String fileName) throws java.io.IOException, ParseExceptionHuge {
		File f = new File(fileName);
		// we don't get the file length from the File object because it might be
		// gzipped
		length = 0;
		List<byte[]> buffers = new ArrayList<>();

		// fill the buffers with doc content
		InputStream fis = new FileInputStream(f);
		if (fileName.endsWith(".gz") || fileName.endsWith(".gzip"))
			fis = new GZIPInputStream(fis);
		try {
			int byteArrayLen = 0;

			int numOfBytesLastPage = 0;
			int currentPage = 0;
			// Since we also support gzipped file, we don't know the
			// uncompressed file length at the beginning and thus we don't know
			// the number of pages we need. We will just continue allocate full
			// pages until the whole file is read - or 256GB are reached - and
			// then trim the last page to its actual size.
			boolean stop = false;
			while (!stop) {
				numOfBytesLastPage = 0;
				buffers.add(new byte[1 << 30]);
				byteArrayLen = 1 << 30;

				int offset = 0;
				int numRead = 0;
				int numOfBytes = 1048576;// I choose this value randomly,
				// any other (not too big) value also can be here.
				if (byteArrayLen - offset < numOfBytes) {
					numOfBytes = byteArrayLen - offset;
				}
				// stop will only be set to false if we still have file contents
				// to read
				stop = true;
				while (offset < byteArrayLen
						&& (numRead = fis.read(buffers.get(currentPage), offset, numOfBytes)) >= 0) {
					stop = false;
					offset += numRead;
					if (byteArrayLen - offset < numOfBytes) {
						numOfBytes = byteArrayLen - offset;
					}
					numOfBytesLastPage += numRead;
				}
				length += numOfBytesLastPage;
				if (length >= (1L << 38)) {
					throw new ParseExceptionHuge("document too big > 256 Gbyte");
				}
				++currentPage;
			}
			// if the last page wasn't full - which will nearly never be
			// the case - we trim it to its actual size
			if (numOfBytesLastPage < (1 << 30)) {
				int lastPageIndex = buffers.size() - 1;
				byte[] lastPage = new byte[numOfBytesLastPage];
				System.arraycopy(buffers.get(lastPageIndex), 0, lastPage, 0, numOfBytesLastPage);
				buffers.set(lastPageIndex, lastPage);
			}
			bufferArray = buffers.toArray(new byte[buffers.size()][]);
		} finally {
			fis.close();
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

	public void writeToFileOutputStream(java.io.FileOutputStream ost, long os, long len) throws java.io.IOException {
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

	/**
	 * Convenience JULIE Lab method. It's basically a copy of
	 * {@link #writeToFileOutputStream(java.io.FileOutputStream, long, long)}
	 * but allows any kind of output stream as first parameter.
	 * 
	 * @param ost
	 *            Some OutputStream to write the specified XML buffer portion
	 *            to.
	 * @param os
	 *            The offset, in bytes, to write to ost.
	 * @param len
	 *            The number of bytes to write, beginning at offset.
	 * @throws java.io.IOException
	 *             If writing to the OutputStream fails.
	 */
	public void writeToOutputStream(OutputStream ost, long os, long len) throws java.io.IOException {
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

	/**
	 * Convenience JULIE Lab method. It uses
	 * {@link #writeToOutputStream(OutputStream, long, long)} with a
	 * ByteArrayOutputStream to return the specified buffer portion as a byte
	 * array.
	 * 
	 * @param os Offset of the XML buffer to begin extraction, in bytes.
	 * @param len Number of bytes to extract.
	 * @return The bytes extracted from the XML buffer.
	 * @throws java.io.IOException If writing to the internal ByteArrayOutputStream fails.
	 */
	public byte[] getFragment(long os, long len) throws java.io.IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		writeToOutputStream(bos, os, len);
		return bos.toByteArray();
	}

	@Override
	public void close() {

	}

}
