package de.julielab.xml;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class JulieXMLToolsCLIRecords {

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out
					.println("Usage: "
							+ JulieXMLToolsCLI.class.getName()
							+ " <XML file> <record XPath expression> <field XPath expression> <field XPath expression>*");
			System.out.println("This tool is meant to extract particular fields of XML files representing a list of records. The result is a table where each field XPath expression is evaluated for one field.");
			System.out.println("The record XPath expression must point to the list of records, i.e. typically a repeating path. The field XPath must be relative to the record XPath!");
			System.exit(1);
		}
		
		String fileName = args[0];
		String forEach = args[1];
		String[] fieldPaths = new String[args.length-2];
		System.arraycopy(args, 2, fieldPaths, 0, fieldPaths.length);
		
		List<Map<String, String>> fields = new ArrayList<>();
		for (int i = 0; i < fieldPaths.length; i++) {
			String path = fieldPaths[i];
			Map<String, String> field = new HashMap<String, String>();
			field.put(JulieXMLConstants.NAME, "fieldvalue" + i);
			field.put(JulieXMLConstants.XPATH, path);
			fields.add(field);
		}
		
		Iterator<Map<String, Object>> rowIterator = JulieXMLTools.constructRowIterator(fileName, 1024, forEach, fields, false);
		
		
		PrintStream out = null;
			try {
				out = new PrintStream(System.out, true, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		while (rowIterator.hasNext()) {
			Map<String, Object> row = rowIterator.next();
			List<String> rowValues = new ArrayList<>();
			for (int i = 0; i < fieldPaths.length; i++) {
				String value = (String) row.get("fieldvalue" + i);
				rowValues.add(value);
			}
			out.println(StringUtils.join(rowValues, "\t"));
		}
	}

}
