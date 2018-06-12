/**
 * UtilsTest.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 07.04.2011
 **/

package de.julielab.xml;

import static de.julielab.xml.JulieXMLConstants.CONSTANT_VALUE;
import static de.julielab.xml.JulieXMLConstants.NAME;
import static de.julielab.xml.JulieXMLConstants.XPATH;
import static de.julielab.xml.JulieXMLTools.createField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.ximpleware.*;
import org.assertj.core.api.Condition;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
/**
 * Tests for the Utils class.
 * 
 * @author faessler
 */
public class JulieXmlToolsTest {

	@Test
	public void testSetElementText() throws VTDException, IOException {
		// At first test whether existing text is replaced correctly.
		String testXMLWithText = "<test>this is text content</test>";
		VTDGen vg = new VTDGen();
		vg.setDoc(testXMLWithText.getBytes());
		vg.parse(true);
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		XMLModifier xm = new XMLModifier(vn);

		int index = JulieXMLTools.setElementText(vn, ap, xm, "/test", "This is new!");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		xm.output(bos);
		assertEquals("<test>This is new!</test>", new String(bos.toByteArray()));
		assertEquals(2, index);

		// Now check, if an empty element's text content is set correctly.
		String testXMLWithoutText = "<test></test>";
		vg.setDoc_BR(testXMLWithoutText.getBytes());
		vg.parse(true);
		vn = vg.getNav();
		ap = new AutoPilot(vn);
		xm = new XMLModifier(vn);
		index = JulieXMLTools.setElementText(vn, ap, xm, "/test", "This is even newer!");
		bos = new ByteArrayOutputStream();
		xm.output(bos);
		assertEquals("<test>This is even newer!</test>", new String(bos.toByteArray()));
		assertEquals(2, index);

		// And what with elements which are terminated immediately?
		String testXMLWithoutTextAndClosingTag = "<test/>";
		vg.setDoc_BR(testXMLWithoutTextAndClosingTag.getBytes());
		vg.parse(true);
		vn = vg.getNav();
		ap = new AutoPilot(vn);
		xm = new XMLModifier(vn);
		index = JulieXMLTools.setElementText(vn, ap, xm, "/test", "Totally newest.");
		bos = new ByteArrayOutputStream();
		xm.output(bos);
		assertEquals("<test>Totally newest.</test>", new String(bos.toByteArray()));
		assertEquals(2, index);

		// If the element doesn't exist at all, the returned index should equal
		// -1.
		vg.setDoc_BR(testXMLWithoutTextAndClosingTag.getBytes());
		vg.parse(true);
		vn = vg.getNav();
		ap = new AutoPilot(vn);
		xm = new XMLModifier(vn);
		index = JulieXMLTools.setElementText(vn, ap, xm, "/doesntexist", "Element doesn't exist!.");
		bos = new ByteArrayOutputStream();
		xm.output(bos);
		assertEquals(-1, index);
	}

	@Test
	public void testGetElementText() throws Exception {
		// At first test whether existing text is replaced correctly.
		String testXMLWithText = "<test><one>some text</one><two>this is <m>mixed</m> text content</two></test>";
		VTDGen vg = new VTDGen();
		vg.setDoc(testXMLWithText.getBytes());
		vg.parse(true);
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("/test/two");
		int index = ap.evalXPath();
		assertTrue(index > -1);
		
		String elementText = JulieXMLTools.getElementText(vn);
		assertEquals("this is mixed text content", elementText);
	}
	
	@Test
	public void parseUnicodeBeyondBMP() throws NavException, FileNotFoundException, IOException, EncodingException, EOFException, EntityException, ParseException {
		// character codpoint 0x10400
		String unicode = "<supplementary>\uD801\uDC00</supplementary>";
		byte[] unicodeBytes = unicode.getBytes();
		assertEquals(unicode, new String(unicodeBytes, "UTF-8"));
		
		VTDGen vg = new VTDGen();
		vg.setDoc(unicodeBytes);
		vg.parse(false);
		VTDNav vn = vg.getNav();
		long fragment = vn.getContentFragment();
		int offset = (int) fragment;
		int length = (int) (fragment >> 32);
		String originalBytePortion = new String(Arrays.copyOfRange(unicodeBytes, offset, offset+length));
		String vtdString = vn.toRawString(offset, length);
		// this actually succeeds
		assertEquals("\uD801\uDC00", originalBytePortion);
		// this fails ;-( the returned character is Ѐ, codepoint 0x400, thus the high surrogate is missing
		assertEquals("\uD801\uDC00", vtdString);
	}

    /**
     * Tests whether the reading of a ZIP archive works as intended. The test archive includes three files, each with
     * a value for the XPath <code>/root/testcontent</code>. We should the the values in a single iterator.
     */
	@Test
	public void testArchiveReading() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(createField(NAME, "contents", XPATH, "testcontent"));
        Iterator<Map<String, Object>> rowIt = JulieXMLTools.constructRowIterator("src/test/resources/zipArchiveReading/archive.zip", 1024, "/root", fields, false);
        Set<String> values = new HashSet<>();
        while (rowIt.hasNext()) {
            Map<String, Object> row =  rowIt.next();
            values.add((String) row.get("contents"));
        }
        assertThat(values).contains("first", "second", "third");
    }

    @Test
    public void testConstantFieldValue() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(createField(NAME, "contents", XPATH, "."));
        fields.add(createField(NAME, "constValueField", CONSTANT_VALUE, "THIS IS A CONSTANT"));
        Iterator<Map<String, Object>> rowIt = JulieXMLTools.constructRowIterator("src/test/resources/simple-test.xml", 1024, "/testdoc/level1/level2", fields, false);
        while (rowIt.hasNext()) {
            Map<String, Object> row = rowIt.next();
            assertThat(row).hasValueSatisfying(new Condition<>(val -> val.equals("THIS IS A CONSTANT"), "Constant field value"));
        }
    }
}
