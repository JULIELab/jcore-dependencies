package salvo.jesus.graph.xml;

import org.apache.log4j.Category;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author  Jesus M. Salvo Jr.
 */

public class XMLToGraphDelegatorHandler extends DefaultHandler {

    private XMLToGraphHandler   delegate;

    private String              vertexElementName;
    private String              edgeElementName;

    /**
     * Log4J Category. The name of the category is the fully qualified name of the
     * enclosing class.
     */
    static        Category    logger;

    static {
        logger = Category.getInstance( XMLToGraphDelegatorHandler.class.getName());
    }

    public XMLToGraphDelegatorHandler( XMLToGraphHandler handler ) {
        this.delegate = handler;
        this.vertexElementName = handler.getVertexElementName();
        this.edgeElementName = handler.getEdgeElementName();
    }

    public void startDocument() throws SAXException {
        this.delegate.startDocument();
    }

    public void endDocument() throws SAXException {
        this.delegate.endDocument();
    }

    public void startElement( String uri, String localName,
            String qualifiedName, Attributes attribs )
    {
        logger.info( "Encountered start of " + localName );
        if( logger.isDebugEnabled()) {
            int numAttribs = attribs.getLength();
            if( numAttribs > 0 ) {
                logger.debug( "Attributes of " + localName + " are: " );
                for( int i = 0; i < numAttribs; i++ ) {
                    logger.debug( "\t" + attribs.getLocalName( i ) + " = " +
                        attribs.getValue( i ));
                }
            }
        }

        if( localName.equals( this.vertexElementName ) && this.delegate.getGraph() == null ) {
            logger.info( "Instantiating graph: instantiateGraph()" );
            try {
                this.delegate.instantiateGraph();
            }
            catch( Exception ex ) {
                logger.error( "Error instantiating graph", ex );
            }
        }

        String  methodName =    "start" + localName.substring( 0, 1 ).toUpperCase() +
                                localName.substring( 1 ) + "Element";
        Class   attribInterface =   new AttributesImpl().getClass().getInterfaces()[0];
        Class   paramClasses[] = { attribInterface };
        Method  method;

        try {
            method = this.delegate.getClass().getMethod( methodName, paramClasses );

            logger.info( "Dynamically invoking " + methodName );
            Object  paramObjects[] = { attribs };
            method.invoke( this.delegate, paramObjects );
        }
        catch( NoSuchMethodException ex ) {
            logger.info( "Method name " + methodName + " non-existent" );
        }
        catch( SecurityException ex ) {
            logger.info( "Unable to determine if " + methodName + " exsists ", ex );
        }
        catch( InvocationTargetException ex ) {
            logger.error( "Dynamically called method " + methodName + " threw an exception", ex );
        }
        catch( Exception ex ) {
            logger.error( "Unable to dynamically call " + methodName, ex );
        }

        logger.info( "Statically invoking startElement" );
        this.delegate.startElement( uri, localName, qualifiedName, attribs );
    }

    public void endElement( String uri, String localName,
            String qualifiedName )
    {
        logger.info( "Encountered end of " + localName );
        String  methodName =    "end" + localName.substring( 0, 1 ).toUpperCase() +
                                localName.substring( 1 ) + "Element";
        Method  method;

        try {
            method = this.delegate.getClass().getMethod( methodName, null );

            logger.info( "Dynamically invoking " + methodName );
            method.invoke( this.delegate, null );
        }
        catch( NoSuchMethodException ex ) {
            logger.info( "Method name " + methodName + " non-existent" );
        }
        catch( SecurityException ex ) {
            logger.info( "Unable to determine if " + methodName + " exsists ", ex );
        }
        catch( InvocationTargetException ex ) {
            logger.error( "Dynamically called method " + methodName + " threw an exception", ex );
        }
        catch( Exception ex ) {
            logger.error( "Unable to dynamically call " + methodName, ex );
        }

        if( localName.equals( this.vertexElementName )) {
            logger.info( "Instantiating vertex: instantiateVertex()" );
            try {
                this.delegate.instantiateVertex();
            }
            catch( Exception ex ) {
                logger.error( "Error instantiating vertex", ex );
            }

        }
        else if( localName.equals( this.edgeElementName )) {
            logger.info( "Instantiating edge: instantiateEdge()" );
            try {
                this.delegate.instantiateEdge();
            }
            catch( Exception ex ) {
                logger.error( "Error instantiating edge", ex );
            }
        }

        logger.info( "Statically invoking endElement" );
        this.delegate.endElement( uri, localName, qualifiedName );
    }

}