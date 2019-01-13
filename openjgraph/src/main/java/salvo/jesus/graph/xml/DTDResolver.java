package salvo.jesus.graph.xml;

import org.xml.sax.*;
import java.net.URL;
import java.io.*;
import org.apache.log4j.Category;

/**
 * Resolver for DTD so that the XGMML DTD need not be retrieved via HTTP.
 * Instead, this resolver will return the DTD from within the <tt>CLASSPATH</tt>,
 * specifically looking for <tt>salvo/jesus/graph/xml/xgmml.dtd</tt>.
 *
 * @author  Egon Willighagen
 */

public class DTDResolver implements EntityResolver {

    /**
     * Log4J Category. The name of the category is the fully qualified name of the
     * enclosing class.
     */
    static        Category    logger;

    static {
        logger = Category.getInstance( DTDResolver.class.getName());
    }

    public InputSource resolveEntity (String publicId, String systemId) {
        systemId = systemId.toLowerCase();
        if ((systemId.indexOf("xgmml.dtd") != -1) || publicId.equals(XGMML.PUBLIC_ID)) {
            return getDTD( "salvo/jesus/graph/xml/xgmml.dtd");
        } else {
            return null;
        }
    }

    private InputSource getDTD( String type ) {
        try {
            URL url = ClassLoader.getSystemResource(type);
            return new InputSource(new BufferedReader(new InputStreamReader(url.openStream())));
        } catch (Exception e) {
            logger.error( "Error while trying to read DTD (" + type + "): ", e );
            return null;
        }
    }

}
