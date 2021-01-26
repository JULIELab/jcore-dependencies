package dragon.matrix;

import java.util.Comparator;

/**
 * <p>The comparator for a coordinate of a sparse matrix which compares the two cell objects
 * regarding to a coordinate  </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class CoordinateComparator implements Comparator,java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public int compare(Object first, Object second){
        Cell firstCell, secondCell;

        firstCell = (Cell)first;
        secondCell=(Cell)second;

        if (firstCell.getRow()>secondCell.getRow())
            return 1;
        else if (firstCell.getRow()==secondCell.getRow()) {
            if (firstCell.getColumn()>secondCell.getColumn())
                return 1;
            else if (firstCell.getColumn()==secondCell.getColumn())
                return 0;
            else
                return -1;
        }
        else
            return -1;

    }
}