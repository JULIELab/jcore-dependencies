package dragon.util;

import java.text.DecimalFormat;
/**
 * <p>Utility class for decimal format</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FormatUtil {
    public FormatUtil() {
    }

    public static DecimalFormat getNumericFormat(int intDigits,int fractionDigits){
        DecimalFormat fm;

        fm = new DecimalFormat();
        fm.setMinimumIntegerDigits(intDigits);
        fm.setMaximumFractionDigits(fractionDigits);
        fm.setMinimumFractionDigits(fractionDigits);
        fm.setGroupingUsed(false);
        return fm;
    }


}