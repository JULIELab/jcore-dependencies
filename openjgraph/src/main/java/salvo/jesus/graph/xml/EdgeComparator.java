package salvo.jesus.graph.xml;

import java.util.*;

/**
 * Comparator used internally by XGMMLDOMSerializer as a parameter
 * to a TreeSet constructor.
 *
 * @author  Jesus M. Salvo Jr.
 */
class EdgeComparator implements Comparator {
    public int compare( Object obj1, Object obj2 ) {
        if( obj1 == obj2 )
            return 0;
        else
            return ( obj1.hashCode() < obj2.hashCode() ? -1 : 1 );
    }

    public boolean equals( Object obj ) {
        return obj.equals( this );
    }
}