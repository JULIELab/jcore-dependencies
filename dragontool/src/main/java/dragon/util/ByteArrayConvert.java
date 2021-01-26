package dragon.util;

/**
 * <p>Converting byte array </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ByteArrayConvert
{
    public static final byte[] toByte(int i)
    {
        byte abyte0[] = new byte[4];
        for(byte byte0 = 0; byte0 <= 3; byte0++)
            abyte0[byte0] = (byte)(i >>> (3 - byte0) * 8);
        return abyte0;
    }

    public static final void toByte(int i, byte[] abyte0, int offset)
    {
        for(byte byte0 = 0; byte0 <= 3; byte0++)
            abyte0[byte0+offset] = (byte)(i >>> (3 - byte0) * 8);
    }

    public static final byte[] toByte(short word0)
    {
        byte abyte0[] = new byte[2];
        for(byte byte0 = 0; byte0 <= 1; byte0++)
            abyte0[byte0] = (byte)(word0 >>> (1 - byte0) * 8);
        return abyte0;
    }

    public static final void toByte(short word0, byte[] abyte0, int offset)
    {
        for(byte byte0 = 0; byte0 <= 1; byte0++)
            abyte0[byte0+offset] = (byte)(word0 >>> (1 - byte0) * 8);
    }

    public static final byte[] toByte(long l)
    {
        byte abyte0[] = new byte[8];
        for(byte byte0 = 0; byte0 <= 7; byte0++)
            abyte0[byte0] = (byte)(int)(l >>> (7 - byte0) * 8);
        return abyte0;
    }

    public static final void toByte(long l, byte[] abyte0, int offset)
    {
        for(byte byte0 = 0; byte0 <= 7; byte0++)
            abyte0[byte0+offset] = (byte)(l >>> (7 - byte0) * 8);
    }

    public static final byte[] toByte(char c)
    {
        byte abyte0[] = new byte[2];
        for(byte byte0 = 0; byte0 <= 1; byte0++)
            abyte0[byte0] = (byte)(c >>> (1 - byte0) * 8);
        return abyte0;
    }

    public static final void toByte(char c, byte[] abyte0, int offset)
    {
        for(byte byte0 = 0; byte0 <= 1; byte0++)
            abyte0[byte0+offset] = (byte)(c >>> (1 - byte0) * 8);
    }

    public static final byte[] toByte(float f)
    {
        byte abyte0[] = new byte[4];
        int i = Float.floatToIntBits(f);
        abyte0 = toByte(i);
        return abyte0;
    }

    public static final void toByte(float f, byte[] abyte0, int offset)
    {
        int i = Float.floatToIntBits(f);
        toByte(i,abyte0,offset);
    }

    public static final byte[] toByte(double d)
    {
        byte abyte0[] = new byte[8];
        long l = Double.doubleToLongBits(d);
        abyte0 = toByte(l);
        return abyte0;
    }

    public static final void toByte(double d, byte[] abyte0, int offset)
    {
        long l = Double.doubleToLongBits(d);
        toByte(l,abyte0,offset);
    }

    public static final int toInt(byte abyte0[], int offset)
    {
        int i = 0;

        for(int byte0 =offset; byte0 <= offset+3; byte0++)
        {
            int j;
            if(abyte0[byte0] < 0)
            {
                abyte0[byte0] = (byte)(abyte0[byte0] & 0x7f);
                j = abyte0[byte0];
                j |= 0x80;
            } else
            {
                j = abyte0[byte0];
            }
            i |= j;
            if(byte0 < 3+offset)
                i <<= 8;
        }
        return i;
    }

    public static final int toInt(byte abyte0[])
    {
        return toInt(abyte0, 0);
    }

    public static final short toShort(byte abyte0[], int offset)
    {
        short word0 = 0;

        for(int byte0 = offset; byte0 <= offset+1; byte0++)
        {
            short word1;
            if(abyte0[byte0] < 0)
            {
                abyte0[byte0] = (byte)(abyte0[byte0] & 0x7f);
                word1 = abyte0[byte0];
                word1 |= 0x80;
            } else
            {
                word1 = abyte0[byte0];
            }
            word0 |= word1;
            if(byte0 < 1+offset)
                word0 <<= 8;
        }

        return word0;
    }

    public static final short toShort(byte abyte0[])
    {
        return toShort(abyte0, 0);
    }

    public static final long toLong(byte abyte0[], int offset)
    {
        long l = 0L;

        for(int byte0 =offset; byte0 <= offset+7; byte0++)
        {
            long l1;
            if(abyte0[byte0] < 0)
            {
                abyte0[byte0] = (byte)(abyte0[byte0] & 0x7f);
                l1 = abyte0[byte0];
                l1 |= 128L;
            } else
            {
                l1 = abyte0[byte0];
            }
            l |= l1;
            if(byte0 < 7+offset)
                l <<= 8;
        }
        return l;
    }

    public static final long toLong(byte abyte0[])
    {
        return toLong(abyte0, 0);
    }

    public static final char toChar(byte abyte0[], int offset)
    {
        char c = '\0';

        c = (char)((c | (char)abyte0[offset]) << 8);
        c |= (char)abyte0[offset+1];
        return c;
    }

    public static final char toChar(byte abyte0[])
    {
        return toChar(abyte0, 0);
    }

    public static final float toFloat(byte abyte0[], int offset)
    {
        float f = 0.0F;
        int i = toInt(abyte0, offset);
        f = Float.intBitsToFloat(i);
        return f;
    }

    public static final float toFloat(byte abyte0[])
    {
        return toFloat(abyte0, 0);
    }

    public static final double toDouble(byte abyte0[], int offset)
    {
        double d = 0.0D;
        long l = toLong(abyte0, offset);
        d = Double.longBitsToDouble(l);
        return d;
    }

    public static final double toDouble(byte abyte0[])
    {
        return toDouble(abyte0, 0);
    }

    public static String toHexString(byte[] data, int offset, int len) {
        StringBuffer sb = new StringBuffer(len*2);
        int i;
        for (i = offset; i <len; i++) {
            sb.append(Integer.toHexString( ( (int) data[i]) & 0xFF));
        }
        return sb.toString();
    }
}
