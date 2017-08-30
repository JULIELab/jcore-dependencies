package dragon.util;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * <p>Converting character number to digit number</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Conversion {
    private static String[] arrEngNum={"a","an","one","billion","eight","eighteen","eighty","eleven","five","fifteen","fifty","four","fourteen","forty",
                                "hundred", "million","nine","nineteen","ninty","seven","seventeen","seventy","six","sixteen","sixty","ten",
                                "thirteen","thirty","thousand","three","twelve","twenty","two","zero"};
    private static int[] arrInt={1,1,1,1000000000,8,18,80,11,5,15,50,4,14,40,100,1000000,9,19,90,7,17,70,6,16,60,10,13,30,1000,3,12,20,2,0};

    public Conversion() {
    }

    public static Date engDate(String str){

        try{
			return new SimpleDateFormat().parse(str);
		}
        catch(Exception e)
        {
            return null;
        }
    }

    public static int engInt(String str){
        int low, high,middle;
        int cmp;

        low=0;
        high=arrEngNum.length-1;
        while(low<high)
        {
            middle=(high+low)/2;
            if((cmp=arrEngNum[middle].compareToIgnoreCase(str))==0 ) return arrInt[middle];
            if(cmp>0) high=middle-1;
            else low=middle+1;
        }

        //if it is not pre-defined english word, see if it is a number.
        try{
            return Integer.parseInt(str);
        }
        catch(Exception e)
        {
            return -1;
        }
    }
}