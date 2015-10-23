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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.ximpleware.AutoPilot;
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
}
