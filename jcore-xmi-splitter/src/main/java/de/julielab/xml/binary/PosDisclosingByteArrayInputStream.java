package de.julielab.xml.binary;

import java.io.ByteArrayInputStream;

public class PosDisclosingByteArrayInputStream extends ByteArrayInputStream {
    public PosDisclosingByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    public PosDisclosingByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    public int getPos() {
        return pos;
    }
}
