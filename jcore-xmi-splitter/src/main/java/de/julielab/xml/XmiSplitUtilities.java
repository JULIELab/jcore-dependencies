package de.julielab.xml;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.events.StartElement;
import java.util.Arrays;
import java.util.List;

public class XmiSplitUtilities {
    public static final String CAS_NULL = "uima.cas.NULL";
    public static final String CAS_VIEW = "uima.cas.View";
    public static final String CAS_SOFA = CAS.TYPE_NAME_SOFA;
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
    private final static Logger log = LoggerFactory.getLogger(XmiSplitUtilities.class);

    /**
     * Extracts the first part of the fully qualified java name from the
     * namespace uri.
     *
     * @param nameSpace The namespace uri of the element.
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
     * @param element The start element.
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
        return annotationType.getName().equals(CAS.TYPE_NAME_FS_ARRAY);
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

    public static boolean isAnnotationType(String qualifiedTypename) {
        // The uima.cas prefix is given to all types in package org.apache.uima.jcas.cas which includes all
        // the technical array types and the TOP type. That should qualify as the "non-annotation" types.
        return !qualifiedTypename.startsWith("uima.cas");
    }

    public static boolean isReferenceAttribute(Type annotationType, String attributeName, TypeSystem ts) {
        if (annotationType.isArray() && attributeName.equals("elements")) return !annotationType.getComponentType().isPrimitive();
        return isReferenceFeature(annotationType.getFeatureByBaseName(attributeName), ts);
    }

    public static boolean isReferenceFeature(Feature f, TypeSystem ts) {
        if (f.isMultipleReferencesAllowed()) return true;
        final Type range = f.getRange();
        // arrays with or without complex types
        if (range.isArray() && !range.getComponentType().isPrimitive()) return true;
        else if (range.isArray()) return false;
        // Lists pointing to feature structures
        if (ts.subsumes(ts.getType(CAS.TYPE_NAME_FS_LIST), range)) return true;
        // Lists containing primitive type data
        if (ts.subsumes(ts.getType(CAS.TYPE_NAME_LIST_BASE), range)) return false;
        if (!range.isPrimitive()) return true;
        return false;
    }

    public static boolean isMultiValuedFeatureAttribute(Type type, String attrName) {
        return type.isArray() && attrName.equals("elements");
    }

    public static boolean isListTypeName(String typeName) {
        final String resolvedName = resolveListSubtypes(typeName);
        return resolvedName.equals(CAS.TYPE_NAME_FLOAT_LIST)
                || resolvedName.equals(CAS.TYPE_NAME_FS_LIST)
                || resolvedName.equals(CAS.TYPE_NAME_INTEGER_LIST)
                || resolvedName.equals(CAS.TYPE_NAME_STRING_LIST);
    }

    public static String resolveListSubtypes(String typeName) {
        switch (typeName) {
            case CAS.TYPE_NAME_NON_EMPTY_FLOAT_LIST:
                return CAS.TYPE_NAME_FLOAT_LIST;
            case CAS.TYPE_NAME_NON_EMPTY_FS_LIST:
                return CAS.TYPE_NAME_FS_LIST;
            case CAS.TYPE_NAME_NON_EMPTY_INTEGER_LIST:
                return CAS.TYPE_NAME_INTEGER_LIST;
            case CAS.TYPE_NAME_NON_EMPTY_STRING_LIST:
                return CAS.TYPE_NAME_STRING_LIST;
            default:
                return typeName;
        }
    }
}
