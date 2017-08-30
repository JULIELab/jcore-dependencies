package dragon.ir.index;

/**
 * <p>Interface of IRDoc Index List</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface IRDocIndexList {
    /**
     * Gets the IRDoc specified by its index
     * @param index the index of the document
     * @return the IRDoc object
     */
    public IRDoc get(int index);

    /**
     * Adds an IRDoc object to the index list
     * @param curDoc the IRDoc for adding
     * @return true if added successfully.
     */
    public boolean add(IRDoc curDoc);

    /**
     * Releases occupied resources
     */
    public void close();

    /**
     * Gets the number of documents in the list.
     * @return the number of documents in the list.
     */
    public int size();
}