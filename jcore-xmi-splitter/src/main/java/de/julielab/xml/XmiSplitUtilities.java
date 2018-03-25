package de.julielab.xml;

import java.util.Arrays;
import java.util.List;

import javax.xml.stream.events.StartElement;

import org.apache.uima.cas.Type;

public class XmiSplitUtilities {

	/**
	 * The default types namespace that is assumed if not the fully qualified
	 * java name is given for an annotation.
	 */
	public static final String TYPES_NAMESPACE = "de.julielab.jules.types.";
	
	/**
	 * Ranges of features that will erroneously (for our purposes) have the
	 * status of not being primitive. TODO: Are there any other ranges that have
	 * to be considered (e.g. lists)?
	 */
	public static final List<String> primitives = Arrays.asList(
			"uima.cas.BooleanArray", "uima.cas.ByteArray",
			"uima.cas.DoubleArray", "uima.cas.FloatArray",
			"uima.cas.IntegerArray", "uima.cas.LongArray",
			"uima.cas.ShortArray", "uima.cas.StringArray");

	/**
	 * Extracts the first part of the fully qualified java name from the
	 * namespace uri.
	 * 
	 * @param nameSpace
	 *            The namespace uri of the element.
	 * @return
	 */
	public static String convertNSUri(String nameSpace) {
		if (nameSpace.startsWith("http:")) {
			nameSpace = nameSpace.substring(5);
		}
		while (nameSpace.startsWith("/")) {
			nameSpace = nameSpace.substring(1);
		}
		if (nameSpace.endsWith(".ecore")) {
			nameSpace = nameSpace.substring(0, nameSpace.length() - 6);
		}
		String javaNamePart = nameSpace.replace('/', '.') + '.';
		return javaNamePart;
	}

	/**
	 * Extracts the fully qualified java name for an element.
	 * 
	 * @param element
	 *            The start element.
	 * @return
	 */
	public static String getTypeJavaName(StartElement element) {
		String NSUri = element.getName().getNamespaceURI();
		String typeJavaName = convertNSUri(NSUri);
																				return typeJavaName;
	}

	/**
	 * TODO: Are there any Feature Structures other than FSArray that link
	 * annotations (e.g. FSList)?
	 * 
	 * @param annotationType
	 * @return
	 */
	public static boolean isFSArray(Type annotationType) {
		boolean isFSArray = false;
		if (annotationType.isArray()) {
			isFSArray = true;
		}
		return isFSArray;
	}


	public static boolean isPrimitive(Type annotationType, String featureName) {
		boolean isPrimitive = false;
		Type featureType = annotationType.getFeatureByBaseName(featureName)
				.getRange();
        return isPrimitive(featureType);
	}

    /**
     * TODO: See class variable <b>primitives</b>... Are there any features
     * other than "sofa" that are complex but should not be stored recursively?
     *
     * @param type
     * @return
     */
    public static boolean isPrimitive(Type type) {
        boolean isPrimitive = false;

        if (type.isPrimitive()
                || primitives.contains(type.toString())
                || type.toString().equals("uima.cas.Sofa")) {
            isPrimitive = true;
        }
        return isPrimitive;
    }

	public static Type getFeatureType(Type annotationType, String featureName) {
		return annotationType.getFeatureByBaseName(featureName)
				.getRange();
	}


}
