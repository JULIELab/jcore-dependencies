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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

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
		VTDGen vg = new VTDGen();
		byte[] bytes = JulieXMLTools.readStream(new FileInputStream("src/test/resources/23700993.xml"), 1024);
		String utf8String = new String(bytes, "UTF-8");
		Matcher m = Pattern.compile("Affiliation>(.*)</Affiliation>").matcher(utf8String);
		m.find();
		String affiliation = m.group(1);
		String name = affiliation.split(" ")[7];
//		name = name.replaceAll("[^\u0000-\uFFFF]", "");
		for (int i = 0; i < name.length(); i++) {
			System.out.println(name.charAt(i) + " " + name.codePointAt(i) + " " + ((int)name.charAt(i)));
		}
		
		
//		vg.parseFile("src/test/resources/23700993.xml", true);
		vg.setDoc(bytes);
		vg.parse(true);
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectElement("Affiliation");
		while (ap.iterate()) {
			int t = vn.getText();
			if (t != -1) {
				String value = vn.toString(t);
				System.out.println(value);
				String word = value.split(" ")[7];
				for (int i = 0; i < word.length(); i++) {
					System.out.println(word.charAt(i) + " " + word.codePointAt(i) + " " + ((int)word.charAt(i)));
				}
				
				
//				String sanitizedString = word.replaceAll("[^\u0000-\uFFFF]", "");
			}
		}
		
	}
}
