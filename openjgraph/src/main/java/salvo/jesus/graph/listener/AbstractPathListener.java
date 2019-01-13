package salvo.jesus.graph.listener;
import salvo.jesus.graph.*;

/**
 * AbstractPathListener represents a common interface for
 * PathListener, SimplePathListener, and CyclePathListener.
 *
 * @author John V. Sichi
 * @version $Id: AbstractPathListener.java,v 1.1 2002/03/12 07:48:34 perfecthash Exp $
 */
public interface AbstractPathListener extends GraphListener
{
    public Vertex getFirstVertex();
    public Vertex getLastVertex();
}

// End AbstractPathListener.java
