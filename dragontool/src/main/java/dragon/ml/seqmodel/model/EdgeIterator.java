package dragon.ml.seqmodel.model;

/**
 * <p>Interface of Edge Iterator</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface EdgeIterator {
    /**
     * Starts scaning all edges in the graph
     */
    public void start();

    /**
     * Tests if the next edge exists
     * @return true if the next edge exists
     */
    public boolean hasNext();

    /**
     * Gets the next edge
     * @return the next edge
     */
    public Edge next();

    /**
     * @return true if the next edge is from the outer model
     */
    public boolean nextIsOuter();
};
