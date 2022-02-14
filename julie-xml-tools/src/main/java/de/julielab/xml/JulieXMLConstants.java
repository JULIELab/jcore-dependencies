package de.julielab.xml;

import com.ximpleware.VTDNav;
import com.ximpleware.extended.VTDNavHuge;

import java.util.List;

public abstract class JulieXMLConstants {

    /**
     * Constant for the name of a definition attribute in the configuration file for the CoStoSys DataBaseConnector. The
     * value of the <code>FOR_EACH</code> attribute is the XPath expression which determines the XML elements for each
     * of which particular fields with values within these elements should be constructed.
     * <p>
     * <samp> <b>Example:</b>
     * <p>
     * Assume we have a file with the following structure:
     *
     * <pre>
     * &lt;MedlineCitationSet&gt;
     * 		&lt;MedlineCitation&gt;
     * 			&lt;PMID&gt;123456&lt;/PMID&gt;
     * 			...more content...
     * 		&lt;/MedlineCitation&gt;
     * 			...
     * 		&lt;MedlineCitation&gt;
     * 		...some content...
     * 		&lt;/MedlineCitation&gt;
     * 			...
     * &lt;/MedlineCitationSet&gt;
     * </pre>
     * <p>
     * We want to traverse each of the &lt;MedlineCitation&gt; elements and extract information from their inner
     * elements, e.g. their PMID. For this purpose we set the <code>FOR_EACH</code> attribute (or the corresponding
     * method parameters in the Java code) to the XPath "/MedlineCitationSet/MedlineCitation". </samp>
     * </p>
     */
    public static final String FOR_EACH = "forEach";

    /**
     * Constant for the name of a definition attribute in the configuration file for the CoStoSys DataBaseConnector.
     * Indicates whether the data in the respective table is stored in JeDIS binary format.
     */
    public static final String BINARY = "binary";

    /**
     * Constant for the name of a field attribute.
     * <p>
     * If set to true, the database table column corresponding to the field definition with the <code>PRIMARY_KEY</code>
     * attribute will be part of the primary key.
     */
    public static final String PRIMARY_KEY = "primaryKey";

    /**
     * <p>
     * Constant for the name of a field attribute.
     * </p>
     * <p>
     * If set to true, the database table column corresponding to the field definition with the <code>NOT_NULL</code>
     * attribute will prohibit null values.
     * </p>
     */
    public static final String NOT_NULL = "notNull";

    /**
     * Constant for the name of a field attribute.
     * <p>
     * If set to true, the value of the database table column corresponding to the field definition with the
     * <code>RETRIEVE</code> attribute will be retrieved by the CoStoSys DataBaseConnector's query methods.
     */
    public static final String RETRIEVE = "retrieve";

    /**
     * Constant for the name of a field attribute. The <code>XPATH</code> attribute is holding the XPath expression that
     * determines which XML element holds the desired information for the field.
     */
    public static final String XPATH = "xpath";

    /**
     * Constant for the name of a field. Will correspond to the name of the database-field which holds the information
     * retrieved by the field <code>Map</code>.
     */
    public static final String NAME = "name";

    /**
     * Constant for the name of a field attribute.
     * <p>
     * The <code>RETURN_XML_FRAGMENT</code> attribute determines whether the complete XML code pointed to by the
     * <code>XPATH</code> attribute should be returned (<code>RETURN_XML_FRAGMENT</code> set to <code>true</code>).
     * <p>
     * <samp> <b>Example:</b>
     * <p>
     * A field with the following attribute-value-pairs
     * </p>
     * <p>
     * <em>&lt;field name="xml" xpath="/MedlineCitationSet/MedlineCitation" returnXMLFragment="true"&gt;</em>
     * </p>
     * returns the complete XML fragment for a MedlineCitation node, including opening and closing tags. </samp> </p>
     */
    public static final String RETURN_XML_FRAGMENT = "returnXMLFragment";

    /**
     * Constant for the name of a field attribute.
     * <p>
     * If a field's XPath expression has several hits (e.g. an XPath pointing to an author in a Medline document will
     * most probably find multiple matches), <code>RETURN_ARRAY</code> determines whether the extracted values should be
     * returned as a String array. If set to false, multiple values will be concatenated using the String given by
     * <code>CONCAT_STRING</code> or, if missing, the default (',').
     */
    public static final String RETURN_ARRAY = "returnValuesAsArray";

    public static final String GZIP = "gzip";

    /**
     * Constant for the name of a field attribute.
     * <p>
     * Determines the String to be used when concatenating multiple hits of an XPath. Multiple hits can also be returned
     * as an array. See the <code>RETURN_ARRAY</code> constant for more information.
     */
    public static final String CONCAT_STRING = "concatString";

    /**
     * Constant for the name of a field attribute.
     * <p>
     * If set to true, the file name - if the XML document is read from file - is used to extract values. This is done
     * on a 'match with regular expression and replace with' fashion. Therefore, using this attribute requires to
     * deliver values for the attributes {@link #REGEX} and {@link #REPLACE_WITH} as well.
     */
    public static final String EXTRACT_FROM_FILENAME = "extractFromFileName";

    /**
     * Constant for the name of a field attribute.
     * <p>
     *     If set to true, the value of this field will be the complete contents of the XML document, including doc type declaration.
     * </p>
     */
    public static final String COMPLETE_XML = "completeXml";

    /**
     * Constant for the name of a field attribute.
     * <p>
     * If set to true, extracted XML text passages will be XML entity resolved. That is, special characters will be substituted by their human readable counterpart.
     *
     * <samp>
     * <b>Example:</b>
     * <p>
     * The text
     * <pre>The population of butterflies \& bees represents &#x0026;lt; 30% of all insects</pre>
     * contains the XML entities '&' and '<' in an escaped fashion so not to collide with the XML parsing process. Switching <code>RESOLVE_ENTITIES</code> to true will
     * result in the string
     * <pre>The population of butterflies & bees represents < 30% of all insects</pre>
     * </samp>
     */
    public static final String RESOLVE_ENTITIES = "resolveEntities";

    /**
     * Constant for the name of a field attribute. Fields which have set the <code>TIMESTAMP</code> attribute to "true"
     * will be given the time stamp of the last update for the corresponding record. This is required for delta-updates.
     */
    public static final String TIMESTAMP = "timestamp";

    /**
     * Constant for the name of a field attribute.
     * <p>
     * Used together with <code>EXTRACT_FROM_FILENAME</code> and <code>REPLACE_WITH</code> attributes. Determines the
     * regular expression whose matches on the XML document file name are substituted by the value given by
     * <code>REPLACE_WITH</code>.
     */
    public static final String REGEX = "regex";

    /**
     * Constant for the name of a field attribute.
     * <p>
     * Used together with <code>EXTRACT_FROM_FILENAME</code> and <code>REGEX</code> attributes. Determines the
     * substitute expression to replace characters of the XML document filename which match the regular expression given
     * by the <code>REGEX</code> attribute.
     */
    public static final String REPLACE_WITH = "replaceWith";

    /**
     * Constant for the name of a field attribute.
     * <p>
     * Used to set the type of a parsed value, e.g. String
     */
    public static final String TYPE = "type";

    /**
     * Constant for the name of a field attribute>
     * <p>
     * The value of this attribute will always be the value of the field that specifies the attribute.
     * </p>
     */
    public static final String CONSTANT_VALUE = "constantValue";

    /**
     * <p>
     * Used by the {@link JulieXMLTools#constructRowIterator(VTDNav, String, List, String)} and
     * {@link JulieXMLTools#constructRowIterator(VTDNavHuge, String, List, String)} methods. Adds to each row
     * the VTD token index of the beginning of the respective row, i.e. the XML element.
     * </p>
     * <p>
     * When using this functionality to navigate the XML with the VTDNav object given to the row constructor,
     * take care to always set back the original VTDNav state for the current row. This can be achieved by
     * calling {@link VTDNav#push()} before and {@link VTDNav#pop()} after manipulations of the VTDNav
     * object.
     * </p>
     */
    public static final String VTD_INDEX = "vtdIndex";
}
