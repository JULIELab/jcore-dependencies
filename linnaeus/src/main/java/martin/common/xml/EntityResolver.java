package martin.common.xml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

public class EntityResolver implements org.xml.sax.EntityResolver {

	private DefaultHandler defaultHandler = new DefaultHandler();
	private String[] dtdLocations;
	private String[] scanStrings;

	public EntityResolver(String[] dtdLocations){
		this.dtdLocations = dtdLocations;
		scanStrings = new String[dtdLocations.length];

		for (int i = 0; i < dtdLocations.length; i++){
			String dtdLocation = dtdLocations[i];
			String[] temp = dtdLocation.split("/|\\\\");
			scanStrings[i] = temp[temp.length-1];
		}
	}

	public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {

		for (int i = 0; i < scanStrings.length; i++)
			if (arg1.indexOf(scanStrings[i]) != -1){
				return new InputSource(dtdLocations[i]);
			}

		return defaultHandler.resolveEntity(arg0, arg1);
	}
}
