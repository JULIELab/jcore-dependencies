/**
 * Utils.java
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
 * Creation date: 16.11.2010
 **/

package de.julielab.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.EOFException;
import com.ximpleware.EncodingException;
import com.ximpleware.EntityException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import com.ximpleware.extended.AutoPilotHuge;
import com.ximpleware.extended.EOFExceptionHuge;
import com.ximpleware.extended.EncodingExceptionHuge;
import com.ximpleware.extended.EntityExceptionHuge;
import com.ximpleware.extended.NavExceptionHuge;
import com.ximpleware.extended.ParseExceptionHuge;
import com.ximpleware.extended.VTDExceptionHuge;
import com.ximpleware.extended.VTDGenHuge;
import com.ximpleware.extended.VTDNavHuge;
import com.ximpleware.extended.XPathEvalExceptionHuge;
import com.ximpleware.extended.XPathParseExceptionHuge;

/**
 * Utility class offering convenience methods.
 * 
 * @author faessler
 */
public class JulieXMLTools {

	static final Logger LOG = LoggerFactory.getLogger(JulieXMLTools.class);

	public static final int ELEMENT_FRAGMENT = 0;

	public static final int CONTENT_FRAGMENT = 1;

	/**
	 * Convenience method for quick construction of a row iterator over an XML document.
	 * 
	 * <p>
	 * The <code>fileName</code> determines the location of the XML file to return data records from. For more detailed
	 * information see {@link #constructRowIterator(VTDNav, String, List, String)}.
	 * 
	 * @param fileName
	 *            XML file to return data rows from.
	 * @param bufferSize
	 *            Size of buffers while reading the file at <code>fileName</code>.
	 * @param forEachXpath
	 *            An XPath expression determining the XML elements to retrieve data records from.
	 * @param fields
	 *            List of attribute-value pairs determining the record fields returned by the iterator.
	 * @return An iterator over all rows extracted from the XMl document pointed to by <code>fileName</code>.
	 */
	public static Iterator<Map<String, Object>> constructRowIterator(String fileName, int bufferSize,
			String forEachXpath, final List<Map<String, String>> fields, boolean largeFileSize) {
		try {
			if (largeFileSize) {
				return constructRowIteratorHuge(fileName, forEachXpath, fields);
			} else {
				InputStream is = null;
				if (fileName.endsWith(".gz") || fileName.endsWith(".gzip")) {
					is = new GZIPInputStream(new FileInputStream(fileName));
				} else if (fileName.endsWith(".zip")) {
					is = new ZipInputStream(new FileInputStream(fileName));
					((ZipInputStream) is).getNextEntry();
				} else {
					is = new FileInputStream(fileName);
				}
				VTDNav vn = getVTDNav(is, bufferSize);
				return constructRowIterator(vn, forEachXpath, fields, fileName);
			}
		} catch (FileNotFoundException e) {
			LOG.error(String.format("File %s could not be found.", fileName));
			e.printStackTrace();
		} catch (FileTooBigException e) {
			try {
				LOG.info("Trying to fall back on VTD XML 'Huge' parser for large XML files...");
				return constructRowIteratorHuge(fileName, forEachXpath, fields);
			} catch (ParseExceptionHuge e1) {
				LOG.error("Error while parsing file " + fileName + ": ", e1.getMessage());
				e1.printStackTrace();
				System.exit(1);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			LOG.error("Error while parsing file " + fileName + ": ", e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (ParseExceptionHuge e) {
			LOG.error("Error while parsing file " + fileName + ": ", e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	/**
	 * @param fileName
	 * @param forEachXpath
	 * @param fields
	 * @return
	 * @throws IOException
	 * @throws ParseExceptionHuge
	 * @throws EncodingExceptionHuge
	 * @throws EOFExceptionHuge
	 * @throws EntityExceptionHuge
	 */
	private static Iterator<Map<String, Object>> constructRowIteratorHuge(String fileName, String forEachXpath,
			final List<Map<String, String>> fields) throws IOException, ParseExceptionHuge {
		if (fileName.endsWith(".gz") || fileName.endsWith(".gzip") || fileName.endsWith(".zip")) {
			LOG.warn("File " + fileName + " seems to be (g)zipped - huge files must be uncompressed!");
		}
		JulieXMLBuffer buffer = new JulieXMLBuffer();
		buffer.readFile(fileName);
		VTDGenHuge vg = new VTDGenHuge();
		vg.setDoc(buffer);
		vg.parse(true);
		VTDNavHuge vn = vg.getNav();
		return constructRowIterator(vn, forEachXpath, fields, fileName);
	}

	/**
	 * Convenience method for quick construction of a row iterator over an XML document.
	 * 
	 * <p>
	 * <code>data</code> contains the XML data to return data records from. For more detailed information see
	 * {@link #constructRowIterator(VTDNav, String, List, String)}.
	 * 
	 * @param data
	 *            Byte array containing an XML document.
	 * @param bufferSize
	 *            Size of buffers while reading the file at <code>fileName</code>.
	 * @param forEachXpath
	 *            An XPath expression determining the XML elements to retrieve data records from.
	 * @param fields
	 *            List of attribute-value pairs determining the record fields returned by the iterator.
	 * @param identifier
	 *            A string identifying the XML document in <code>data</code>, needed for error messages.
	 * @return An iterator over all rows extracted from the XMl document pointed to by <code>fileName</code>.
	 */
	public static Iterator<Map<String, Object>> constructRowIterator(byte[] data, int bufferSize, String forEachXpath,
			List<Map<String, String>> fields, String identifier) {
		try {
			VTDGen vg = new VTDGen();
			vg.setDoc(data);
			vg.parse(true);
			VTDNav vn = vg.getNav();
			return constructRowIterator(vn, forEachXpath, fields, identifier);
		} catch (ParseException e) {
			LOG.error("Error while parsing document " + identifier);
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	/**
	 * This method corresponds to {@link #constructRowIterator(VTDNav, String, List, String)} but employs the
	 * VTD-XML-Huge API to extract data from very large XML documents. Due to API differences, there are a few
	 * shortcomings however:<br>
	 * <ul>
	 * <li>The iterator returned by this method is not able to automatically extract namespace URIs. If namespace-aware
	 * processing is necessary, namespace prefixes and their corresponding URIs must be made manually configurable.
	 * </ul>
	 * 
	 * @param vn
	 *            <code>VTDNavHuge</code> object to the XML object. The object should not have been used before, so the
	 *            current VTD index points to the document root.
	 * @param forEachXpath
	 *            An absolute XPath expression determining the slice(s) of the XML document from which data rows are
	 *            extracted.
	 * @param fields
	 *            The fields to be returned with each data row.
	 * @param identifier
	 *            Used in error messages
	 * @return An iterator over all rows extracted from the XMl document navigated by <code>vn</code>.
	 */
	private static Iterator<Map<String, Object>> constructRowIterator(VTDNavHuge vn, String forEachXpath,
			List<Map<String, String>> fields, String identifier) {
		final AutoPilotHuge ap = new AutoPilotHuge(vn);
		try {

			// starting conditions
			ap.selectXPath(forEachXpath);
			final int startIndex = ap.evalXPath();
			if (startIndex == -1)
				LOG.warn("Couldn't find XPath: " + forEachXpath + " in document " + identifier);

			final Map<String, FieldValueSource> navigators = new HashMap<String, FieldValueSource>();
			for (Map<String, String> field : fields) {
				String xPath = field.get(JulieXMLConstants.XPATH);
				Options options = new Options();
				String fieldName = field.get(JulieXMLConstants.NAME);
				if (xPath != null) {
					AutoPilotHuge pilot = new AutoPilotHuge(vn);
					AutoPilotHuge pilotForEach = new AutoPilotHuge(vn);
					String fieldForEach = field.get(JulieXMLConstants.FOR_EACH);

					// Default: The XPath attribute directly holds the path to
					// the desired value. The ForEachAP is the main navigator,
					// so it must select the XPath if no specific ForEachXPath
					// is given.
					pilotForEach.selectXPath(xPath);
					pilot.selectXPath(xPath);
					if (fieldForEach != null) {
						pilotForEach = new AutoPilotHuge(vn);
						pilotForEach.selectXPath(fieldForEach);
					} else {
						// If there is no ForEachXPath indeed, the ForEachAP
						// always selects the path. The XPathAP is nearly
						// unemployed now: It just needs to point to the spot
						// the ForEachXPath navigated to.
						pilot.selectXPath(".");
					}

					options.returnXMLFragment = Boolean.parseBoolean(field.get(JulieXMLConstants.RETURN_XML_FRAGMENT));
					options.returnArray = Boolean.parseBoolean(field.get(JulieXMLConstants.RETURN_ARRAY));
					// options.returnAttributeValue = xPath
					// .matches(REGEX_XPATH_ATTR);
					options.concatString = field.get(JulieXMLConstants.CONCAT_STRING);
					if (options.concatString == null)
						options.concatString = ",";
					options.performGzip = Boolean.parseBoolean(field.get(JulieXMLConstants.GZIP));
					navigators.put(fieldName, new XPathNavigatorHuge(vn, pilotForEach, pilot, options));
				} else if (Boolean.parseBoolean(field.get(JulieXMLConstants.EXTRACT_FROM_FILENAME))) {
					String[] path = identifier.split("/");
					navigators.put(fieldName, new FileNameValueSource(path[path.length - 1], field));
				} else {
					LOG.warn("Field with name \""
							+ fieldName
							+ "\" does not define a source to get a value from (e.g. XML XPath or file name) and will not have imported any values.");
				}
			}

			return new Iterator<Map<String, Object>>() {

				int index = startIndex;

				public boolean hasNext() {
					return index != -1;
				}

				public Map<String, Object> next() {
					if (!hasNext())
						return null;
					Map<String, Object> row = new HashMap<String, Object>();
					try {
						for (String fieldName : navigators.keySet()) {
							FieldValueSource navi = navigators.get(fieldName);
							Object fieldValue = navi.getFieldValue();

							row.put(fieldName, fieldValue);
						}
						index = ap.evalXPath();
						return row;
					} catch (XPathEvalException e) {
						e.printStackTrace();
					} catch (NavException e) {
						e.printStackTrace();
					} catch (XPathEvalExceptionHuge e) {
						e.printStackTrace();
					} catch (NavExceptionHuge e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}

				public void remove() {
				}

			};
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (VTDExceptionHuge e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * <p>
	 * The <code>VTDNav</code> vn is a VTD navigator over the XML file to return data records from. For each evaluation
	 * of the <code>forEach</code> XPath expression, one data row is created
	 * </p>
	 * <p>
	 * Such a row consist of the fields given by the list <code>fields</code> The list contains <code>Maps</code> of
	 * attribute-value pairs. All fields are required to have a {@link Constants#XPATH} attribute which specifies the
	 * XPath pointing to information in the XML documents to retrieve. Likewise, a {@link Constants#NAME} attribute is
	 * required. This attribute determines the name of the field in the resulting row containing the information
	 * retrieved by the field's <code>Constants.XPATH</code> attribute.
	 * </p>
	 * <p>
	 * <samp> <b>Example:</b>
	 * <p>
	 * A field with the following attribute-value-pairs
	 * </p>
	 * <p>
	 * <em>&lt;field name="pmid" xpath="/MedlineCitationSet/MedlineCitation/PMID" &gt;</em>
	 * </p>
	 * will create one field in each returned data row named "pmid" and its value will by the character data at the
	 * XPath "/MedlineCitationSet/MedlineCitation/PMID". </samp> </p>
	 * 
	 * @param vn
	 *            The {@link VTDNav} object which navigates over the XML document to retrieve records from.
	 * @param forEachXpath
	 *            An XPath expression determining the XML elements for each of which one row should be created.
	 * @param fields
	 *            The fields to be returned with each data row.
	 * @return An iterator over all rows extracted from the XMl document navigated by <code>vn</code>.
	 */
	public static Iterator<Map<String, Object>> constructRowIterator(final VTDNav vn, String forEachXpath,
			final List<Map<String, String>> fields, String identifier) {
		final AutoPilot ap = new AutoPilot(vn);
		try {
			Map<String, String> namespaceMap = buildNamespaceMap(vn.duplicateNav());

			// starting conditions
			declareNamespaces(ap, namespaceMap);
			ap.selectXPath(forEachXpath);
			final int startIndex = ap.evalXPath();
			if (startIndex == -1)
				LOG.info("Couldn't find XPath: " + forEachXpath + " in document " + identifier);

			final Map<String, FieldValueSource> navigators = new HashMap<String, FieldValueSource>();
			for (Map<String, String> field : fields) {
				String xPath = field.get(JulieXMLConstants.XPATH);
				final Options options = new Options();
				String fieldName = field.get(JulieXMLConstants.NAME);
				if (xPath != null) {
					AutoPilot pilot = new AutoPilot(vn);
					AutoPilot pilotForEach = new AutoPilot(vn);
					String fieldForEach = field.get(JulieXMLConstants.FOR_EACH);

					declareNamespaces(pilot, namespaceMap);
					declareNamespaces(pilotForEach, namespaceMap);

					// Default: The XPath attribute directly holds the path to
					// the desired value. The ForEachAP is the main navigator,
					// so it must select the XPath if no specific ForEachXPath
					// is given.
					pilotForEach.selectXPath(xPath);
					pilot.selectXPath(xPath);
					if (fieldForEach != null) {
						pilotForEach = new AutoPilot(vn);
						pilotForEach.selectXPath(fieldForEach);
					} else {
						// If there is no ForEachXPath indeed, the ForEachAP
						// always selects the path. The XPathAP is nearly
						// unemployed now: It just needs to point to the spot
						// the ForEachXPath navigated to.
						pilot.selectXPath(".");
					}

					options.returnXMLFragment = Boolean.parseBoolean(field.get(JulieXMLConstants.RETURN_XML_FRAGMENT));
					options.returnArray = Boolean.parseBoolean(field.get(JulieXMLConstants.RETURN_ARRAY));
					// options.returnAttributeValue = xPath
					// .matches(REGEX_XPATH_ATTR);
					options.resolveEntities = Boolean.parseBoolean(field.get(JulieXMLConstants.RESOLVE_ENTITIES));
					options.concatString = field.get(JulieXMLConstants.CONCAT_STRING);
					if (options.concatString == null)
						options.concatString = ",";
					options.performGzip = Boolean.parseBoolean(field.get(JulieXMLConstants.GZIP));
					navigators.put(fieldName, new XPathNavigator(vn, pilotForEach, pilot, options));
				} else if (Boolean.parseBoolean(field.get(JulieXMLConstants.EXTRACT_FROM_FILENAME))) {
					String[] path = identifier.split("/");
					navigators.put(fieldName, new FileNameValueSource(path[path.length - 1], field));
				} else if (Boolean.parseBoolean(field.get(JulieXMLConstants.TIMESTAMP))) {
					navigators.put(fieldName, new TimestampValueSource());
				} else {
					LOG.warn("Field with name \""
							+ fieldName
							+ "\" does not define a source to get a value from (e.g. XML XPath or file name) and will not have imported any values.");
				}
			}

			return new Iterator<Map<String, Object>>() {

				int index = startIndex;

				public boolean hasNext() {
					return index != -1;
				}

				public Map<String, Object> next() {
					if (!hasNext())
						return null;
					Map<String, Object> row = new HashMap<String, Object>();
					try {
						for (String fieldName : navigators.keySet()) {
							FieldValueSource navi = navigators.get(fieldName);
							Object fieldValue = navi.getFieldValue();

							row.put(fieldName, fieldValue);
						}
						index = ap.evalXPath();
						return row;
					} catch (XPathEvalException e) {
						e.printStackTrace();
					} catch (NavException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}

				public void remove() {
				}

			};
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (VTDException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void declareNamespaces(AutoPilot ap, Map<String, String> namespaceMap) {
		for (Map.Entry<String, String> entry : namespaceMap.entrySet())
			ap.declareXPathNameSpace(entry.getKey(), entry.getValue());
	}

	private static Map<String, String> buildNamespaceMap(VTDNav vn) throws VTDException {
		Map<String, String> namespaceMap = new HashMap<String, String>();

		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//namespace::*");

		String nsDeclaration = null;
		try {

			int i = -1;
			while ((i = ap.evalXPath()) != -1) {
				nsDeclaration = vn.toString(i);
			        if (nsDeclaration.contains(":")) {
		                    String nsPrefix = nsDeclaration.split(":")[1];
		                    String nsUrl = vn.toString(i + 1);
		                    namespaceMap.put(nsPrefix, nsUrl);
			        }
                }
		} catch (ArrayIndexOutOfBoundsException e) {
			LOG.error(
					"This algorithm expects XML namespace declarations to be of the form \"xmlns:<ns-name>\". The declaration actually was: \""
							+ nsDeclaration + "\"", e);
		}

		return namespaceMap;
	}

	public static VTDNav getVTDNav(InputStream is, int bufferSize) throws ParseException, FileTooBigException {

		VTDGen vg = null;
		try {
			byte[] data = readStream(is, bufferSize);
			vg = new VTDGen();
			vg.setDoc(data);
			vg.parse(true);
		} catch (EncodingException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (EntityException e) {
			e.printStackTrace();
		} catch (FileTooBigException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			String message = e.getMessage();
			if (message.contains("file size too big"))
				throw new FileTooBigException(message);
		}
		return vg.getNav();
	}

	/**
	 * Reads an <code>InputStream</code> buffer wise, concatenates all buffers and returns one <code>byte[]</code> of
	 * exact length of the read data.
	 * 
	 * @param is
	 *            <code>InputStream</code> to read.
	 * @param bufferSize
	 *            Size of maximum bytes to read by one <code>is.read()</code> call.
	 * @return A <code>byte[]</code> containing all the data of the <code>InputStream</code>.
	 * @throws IOException
	 */
	public static byte[] readStream(InputStream is, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		List<byte[]> bufferList = new ArrayList<byte[]>();
		List<Integer> readBytesList = new ArrayList<Integer>();
		int bytesRead = 0;
		int allBytesRead = 0;
		while ((bytesRead = is.read(buffer)) != -1) {
			bufferList.add(buffer);
			readBytesList.add(bytesRead);
			buffer = new byte[bufferSize];
			// Overflow-check
			if (allBytesRead + bytesRead < allBytesRead) {
				LOG.warn("Array size overflow while reading file. The file you are attempting to read "
						+ "is propably greater than 2GB in size. Such files cannot be read. "
						+ "Consider splitting the file into subfiles of size less than 2GB.");
				throw new FileTooBigException("Input file could not be read because it is too big (>2GB)");
			}
			allBytesRead += bytesRead;
		}
		byte[] streamContent = new byte[allBytesRead];
		int pos = 0;
		try {
			for (int i = 0; i < bufferList.size(); ++i) {
				System.arraycopy(bufferList.get(i), 0, streamContent, pos, readBytesList.get(i));
				pos += readBytesList.get(i);
			}
		} catch (ArrayIndexOutOfBoundsException oob) {
			LOG.error(
					"Array index out of bounds - please check whether the file you try to read is less then 2GB in size.",
					oob);
		} finally {
			is.close();
		}
		return streamContent;
	}

	public static byte[] gzipData(byte[] data) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			GZIPOutputStream os = new GZIPOutputStream(baos);
			os.write(data);
			os.close();
			return baos.toByteArray();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] unGzipData(byte[] gzipData) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(gzipData);
		GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
		byte[] data = JulieXMLTools.readStream(gzipInputStream, 1024);
		return data;
	}

	public static URL getSolrServerURL(String urlStr, boolean calledByCLI, Logger LOG) {
		URL serverURL;
		try {
			serverURL = new URL(urlStr);
			return serverURL;
		} catch (MalformedURLException e) {
			String msg = "Solr server URL '" + urlStr + "' is malformed: ";
			if (calledByCLI)
				LOG.error(msg + e.getMessage());
			else
				LOG.error(msg, e);
		}
		return null;
	}

	public static String getElementText(VTDNav vn) throws NavException {
		StringBuilder sb = new StringBuilder();
		int depth = vn.getCurrentDepth();
		int i = vn.getCurrentIndex();
		while (vn.getTokenType(i) == VTDNav.TOKEN_STARTING_TAG)
			i++;
		while (vn.getTokenDepth(i) >= depth
				&& !(vn.getTokenType(i) == VTDNav.TOKEN_STARTING_TAG && vn.getTokenDepth(i) == depth)
				&& i < vn.getTokenCount()) {
			if (vn.getTokenType(i) == 5)
				sb.append(vn.toString(i));

			i++;
		}
		return sb.toString();
	}

	public static String getFragment(VTDNav vn, int fragmentType, boolean returnRawString) throws NavException {
		long fragment = fragmentType == ELEMENT_FRAGMENT ? vn.getElementFragment() : vn.getContentFragment();
		int offset = (int) fragment;
		int length = (int) (fragment >> 32);
		return returnRawString ? vn.toRawString(offset, length) : vn.toString(offset, length);
	}

	/**
	 * Sets the text content of an XML element pointed to by <code>xpath</code> to <code>text</code>.
	 * <p>
	 * The cursor of <code>vn</code> is moved to the element determined by <code>xpath</code>.
	 * </p>
	 * 
	 * @param vn
	 *            <code>VTDNav</code> object navigating the XML document to modify.
	 * @param ap
	 *            <code>AutoPilot</code> object bound to <code>vn</code>.
	 * @param xm
	 *            <code>XMLModifier</code> object bound to <code>vn</code>.
	 * @param xpath
	 *            An XPath expression pointing to the XML element whose text should be set.
	 * @param text
	 *            The text which is to be set to the XML element pointed to by <code>xpath</code>.
	 * @return The VTD index of the changes element, -1 otherwise.
	 * @throws VTDException
	 *             If something with navigation or modification of the XML document goes wrong.
	 * @throws UnsupportedEncodingException
	 */
	public static int setElementText(VTDNav vn, AutoPilot ap, XMLModifier xm, String xpath, String text)
			throws VTDException, UnsupportedEncodingException {
		ap.selectXPath(xpath);
		int elementIndex = ap.evalXPath();
		int textIndex = -1;
		// Go to the element to change, if existing.
		if (elementIndex != -1) {
			textIndex = vn.getText();
			// If the element already has text, change it.
			// Since VTD XML 2.11, a non-existent text node does not lead to a
			// -1 index (node does not exist) but to a text node with zero
			// length (node exists but has no text content).
			int tokenLength = vn.getTokenLength(textIndex);
			if (tokenLength > 0)
				xm.updateToken(textIndex, text);
			// If the element is empty, insert the new text.
			else {
				xm.insertAfterHead(text);
				// The following lines wont change anything since the new text
				// is only visible to the XMLModifier, not to the VTDNav...I
				// think :-)
				// textIndex = elementIndex;
				// Determine the new character data position.
				// while (vn.getTokenType(textIndex) !=
				// VTDNav.TOKEN_CHARACTER_DATA
				// && vn.getTokenType(textIndex) != VTDNav.TOKEN_CDATA_VAL) {
				// ++textIndex;
				// }
			}
		}
		return textIndex;
	}

	public static <T> String[] expandArrayEntries(T[] array, String fmtStr) {
		String[] expandedEntries = new String[array.length];
		for (int i = 0; i < expandedEntries.length; i++) {
			expandedEntries[i] = String.format(fmtStr, array[i]);
		}
		return expandedEntries;
	}

	public static <T> String[] expandArrayEntries(List<T> list, String fmtStr) {
		String[] array = new String[list.size()];
		list.toArray(array);
		return expandArrayEntries(array, fmtStr);
	}

	public static <T> String[] expandArrayEntries(T[] array, String[] fmtStrs) {
		if (array.length != fmtStrs.length)
			throw new IllegalArgumentException(
					"The size of the array with elements to be expanded must match the size of the array holding the extention format strings.");
		String[] expandedEntries = new String[array.length];
		for (int i = 0; i < expandedEntries.length; i++) {
			expandedEntries[i] = String.format(fmtStrs[i], array[i]);
		}
		return expandedEntries;
	}

}

interface FieldValueSource {
	public Object getFieldValue() throws Exception;
}

abstract class AbstractFieldValueSource implements FieldValueSource {
	protected byte[] gzipContent(Object content) {
		try {
			// TODO extend for array
			if (content instanceof String)
				return JulieXMLTools.gzipData(((String) content).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}

class ConstantFieldValueSource implements FieldValueSource {

	protected Object value;

	protected ConstantFieldValueSource() {
	};

	public ConstantFieldValueSource(String value) {
		this.value = value;
	}

	public Object getFieldValue() throws Exception {
		return value;
	}

}

class FileNameValueSource extends ConstantFieldValueSource {

	public FileNameValueSource(String fileName, Map<String, String> field) {
		String regex = field.get(JulieXMLConstants.REGEX);
		String replaceWith = field.get(JulieXMLConstants.REPLACE_WITH);
		value = fileName.replaceAll(regex, replaceWith);
	}
}

class TimestampValueSource extends ConstantFieldValueSource {

	public TimestampValueSource() {
		value = new Timestamp(System.currentTimeMillis());
	}

}

class Options {
	public boolean returnXMLFragment;
	public boolean returnArray;
	public String concatString;
	public boolean resolveEntities;
	public boolean performGzip;
}

/**
 * This helper class bundles all classes needed to navigate a particular XPath expression in an XML document.
 * Additionally, it maintains a pointer 'index' to the next occurence's position of the XPath expression in the
 * document. This is needed in case of elements referenced by an XPath which don't occur in all subtrees defined by the
 * 'forEach' attribute of this EntityProcessor. Thus, before returning the field value by 'getFieldValue', it must be
 * checked if the 'forEach' loop already reached the correct position in the document. This is indicated by the
 * 'forEachIndex' parameter which in fact is always one 'forEach' element ahead.
 * 
 * @author faessler
 * 
 */
class XPathNavigator extends AbstractFieldValueSource {
	private VTDNav vn;
	private AutoPilot apFE; // AutoPilot "ForEach"
	private AutoPilot apXP; // AutoPilot "XPath"
	private Options options;

	public XPathNavigator(VTDNav nv, AutoPilot apForEach, AutoPilot apXPath, Options options)
			throws XPathEvalException, NavException, XPathEvalException, NavException, XPathParseException {
		this.vn = nv;
		this.apFE = apForEach;
		this.apXP = apXPath;
		this.options = options;
	}

	public Object getFieldValue() throws XPathEvalException, NavException, NavException, IOException,
			XPathEvalException {
		List<String> retList = new ArrayList<String>();

		while (apFE.evalXPath() != -1) {
			if (options.returnXMLFragment) {
				long fragment = vn.getElementFragment();
				int offset = (int) fragment;
				int length = (int) (fragment >> 32);
				retList.add(options.resolveEntities ? vn.toString(offset, length) : vn.toRawString(offset, length));
			} else {
				retList.add(apXP.evalXPathToString());
			}
			apXP.resetXPath();
		}
		apFE.resetXPath();
		Object retobj = null;
		if (retList.size() > 0) {
			if (options.returnArray)
				retobj = retList.toArray(new String[retList.size()]);
			else if (retList.size() > 1)
				retobj = StringUtils.join(retList, options.concatString);
			else
				retobj = retList.get(0);
			if (options.performGzip)
				return gzipContent(retobj);
			return retobj;
		}
		return null;
	}
}

/**
 * Essentially the same as the XPathNavigator except this version uses the "Huge" classes from VTD XML intended to
 * process very large XML files.
 * 
 * @author faessler
 * 
 */
class XPathNavigatorHuge extends AbstractFieldValueSource {
	private VTDNavHuge vn;
	private AutoPilotHuge apFE; // AutoPilot "ForEach"
	private AutoPilotHuge apXP; // AutoPilot "XPath"
	private Options options;

	public XPathNavigatorHuge(VTDNavHuge nv, AutoPilotHuge apForEach, AutoPilotHuge apXPath, Options options)
			throws XPathEvalException, NavException, XPathEvalExceptionHuge, NavExceptionHuge, XPathParseExceptionHuge {
		this.vn = nv;
		this.apFE = apForEach;
		this.apXP = apXPath;
		if (apFE == null) {
			this.apFE = this.apXP;
			this.apXP = new AutoPilotHuge(vn);
			apXP.selectXPath(".");
		}
		this.options = options;
	}

	public Object getFieldValue() throws XPathEvalException, NavException, NavExceptionHuge, IOException,
			XPathEvalExceptionHuge {
		List<String> retList = new ArrayList<String>();

		while (apFE.evalXPath() != -1) {
			if (options.returnXMLFragment) {
				long[] fragment = vn.getElementFragment();
				long offset = fragment[0];
				long length = fragment[1];
				// Assumption: if the user wants the whole XML fragment,
				// it is likely he wants it to be valid XML, so don't
				// resolve
				// entities.
				try {
					// getting the XML fragment in the VTD-XML-Huge version
					// is a bit messy; we need to get the data storage
					// object
					// and write the required data into an OutputStream
					// which
					// we can read.
					JulieXMLBuffer mb = (JulieXMLBuffer) vn.getXML();
					byte[] fragmentBytes = mb.getFragment(offset, length);
					retList.add(new String(fragmentBytes));
				} catch (ClassCastException e) {
					JulieXMLTools.LOG.error("Casting from com.ximpleware.extended.IByteBuffer to "
							+ JulieXMLBuffer.class.getName() + " failed. You must pass an Instance of"
							+ JulieXMLBuffer.class.getName()
							+ " to the VTDGenHuge object which contains the XML data to be parsed.");
					e.printStackTrace();
				}
			} else {
				retList.add(apXP.evalXPathToString());
			}
			apXP.resetXPath();
		}
		apFE.resetXPath();
		Object retobj = null;
		if (retList.size() > 0) {
			if (options.returnArray)
				retobj = retList.toArray(new String[retList.size()]);
			else if (retList.size() > 1)
				retobj = StringUtils.join(retList, options.concatString);
			else retobj = retList.get(0);
			if (options.performGzip)
				return gzipContent(retobj);
			return retobj;
		}
		
		return null;
	}
}
