package dragon.ir.index;

/**
 * <p>Interface of IRTerm Index List</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface IRTermIndexList {
    /**
     * Gets the IRTerm object specified by the index
     * @param index the index of the term
     * @return the IRTerm object specified by the index
     */
    public IRTerm get(int index);

    /**
     * Adds the term to the list
     * @param curTerm the term for adding
     * @return true if addes successfully
     */
    public boolean add(IRTerm curTerm);

    /**
     * Releases occupied resources
     */
    public void close();

    /**
     * Gets the number of terms in the list
     * @return the number of terms in the list
     */
    public int size();
}