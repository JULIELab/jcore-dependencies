package de.julielab.xml;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JulieXMLToolsCLIRecords {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out
                    .println("Usage: "
                            + JulieXMLToolsCLI.class.getName()
                            + " <XML file> <record XPath expression> <field XPath expression> <field XPath expression>*");
            System.out.println("This tool is meant to extract particular fields of XML files representing a list of records. The result is a table where each field XPath expression is evaluated for one field.");
            System.out.println("The record XPath expression must point to the list of records, i.e. typically a repeating path. The field XPath must be relative to the record XPath!");
            System.out.println("If a field XPath may be evaluated multiple times, i.e. the element pointed to exists more than once (lists), all values will be concatenated with a comma by default. Set the system property de.julielab.xmltools.delim to another delimiter if required. The special value 'explode' on this property will cause the output of all combinations of all arrays elements with all other column values.");
            System.exit(1);
        }

        String fileName = args[0];
        String forEach = args[1];
        String[] fieldPaths = new String[args.length - 2];
        System.arraycopy(args, 2, fieldPaths, 0, fieldPaths.length);
        String delimiter = System.getProperty("de.julielab.xmltools.delim");

        List<Map<String, String>> fields = new ArrayList<>();
        for (int i = 0; i < fieldPaths.length; i++) {
            String path = fieldPaths[i];
            Map<String, String> field = new HashMap<String, String>();
            field.put(JulieXMLConstants.NAME, "fieldvalue" + i);
            field.put(JulieXMLConstants.XPATH, path);
            if (delimiter != null && !delimiter.equals("explode"))
                field.put(JulieXMLConstants.CONCAT_STRING, delimiter);
            else if (delimiter != null)
                field.put(JulieXMLConstants.RETURN_ARRAY, "true");
            fields.add(field);
        }

        Iterator<Map<String, Object>> rowIterator = JulieXMLTools.constructRowIterator(fileName, 4096, forEach, fields, false);


        PrintStream out = null;
        try {
            out = new PrintStream(System.out, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        List<List<String>> outputRowList = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Map<String, Object> row = rowIterator.next();
            for (int i = 0; i < fieldPaths.length; i++) {
                Object value = row.get("fieldvalue" + i);
                if (value instanceof String) {
                    // The condition is required for 'explode' mode. Then, the first element(s) are just
                    // added to the current list of output rows instead of extending already existing values
                    // because there are none yet.
                    if (i == 0) {
                        outputRowList.add(Stream.of((String) value).collect(Collectors.toList()));
                    } else {
                        // If there are already values from previous columns, just append the current value
                        // to all of them (may well just be a single row when there are no XPaths with
                        // multiple values).
                        for (List<String> outputRow : outputRowList)
                            outputRow.add((String) value);
                    }
                } else if (value instanceof String[]) {
                    // this can happen when we use 'explode' as the delimiter value; then, multiple values for
                    // a single XPath are returned as an array.
                    List<List<String>> extendedOutputRowList = new ArrayList<>();
                    String[] array = (String[]) value;
                    // The condition is required for 'explode' mode. Then, the first element(s) are just
                    // added to the current list of output rows instead of extending already existing values
                    // because there are none yet.
                    if (i == 0) {
                        extendedOutputRowList.add(Arrays.stream(array).collect(Collectors.toList()));
                    } else {
                        // Append each array value to each existing row.
                        for (String columnValue : array) {
                            for (List<String> outputRow : outputRowList) {
                                List<String> extendedOutputRow = new ArrayList<>(outputRow);
                                extendedOutputRow.add(columnValue);
                                extendedOutputRowList.add(extendedOutputRow);
                            }
                        }
                    }
                    outputRowList = extendedOutputRowList;
                }
            }
            for (List<String> outputRow : outputRowList)
                out.println(StringUtils.join(outputRow, "\t"));
            outputRowList.clear();
        }
    }

}
