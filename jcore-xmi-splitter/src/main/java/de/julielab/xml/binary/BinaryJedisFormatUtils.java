package de.julielab.xml.binary;

import java.io.IOException;
import java.io.InputStream;

public class BinaryJedisFormatUtils {
    /**
     * Reads the first two bytes of the InputStream and compares to {@link BinaryJeDISNodeEncoder#JEDIS_BINARY_MAGIC}.
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static boolean checkForJeDISBinaryFormat(InputStream is) throws IOException {
        byte[] firstTwoBytes = new byte[2];
        is.read(firstTwoBytes);
        return checkForJeDISBinaryFormat(firstTwoBytes);
    }
    /**
     * Compares the first two bytes to {@link BinaryJeDISNodeEncoder#JEDIS_BINARY_MAGIC}.
     *
     * @param firstTwoBytes
     * @return
     * @throws IOException
     */
    public static boolean checkForJeDISBinaryFormat(byte[] firstTwoBytes) {
        short header = (short) ((firstTwoBytes[0] << 8) | (0xff & firstTwoBytes[1]));
        if (header != BinaryJeDISNodeEncoder.JEDIS_BINARY_MAGIC)
            return false;
        return true;
    }
}
