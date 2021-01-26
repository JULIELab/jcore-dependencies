package dragon.ir.index;

/**
 * <p>Interface of IRRelation Index List</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface IRRelationIndexList {
    /**
     * Gets the IRRelation object specified by its index
     * @param index the index of the relation
     * @return the IRRelation object specified by its index
     */
    public IRRelation get(int index) ;

    /**
     * Adds a relation to the list.
     * @param curRelation the relation for adding
     * @return true if added successfully.
     */
    public boolean add(IRRelation curRelation) ;

    /**
     * Gets the number of relations in the list.
     * @return the number of relations in the list.
     */
    public int size();

    /**
     * Releases occupied resources.
     */
    public void close();
}