package dragon.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.RandomAccessFile;

/**
 * <p>Fast binary data reader</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FastBinaryReader extends DataInputStream {
    public FastBinaryReader(String filename) {
        super(new FastFileInputStream(filename));
    }

    public FastBinaryReader(File file) {
        super(new FastFileInputStream(file));
    }

    public FastBinaryReader(RandomAccessFile raf, long length) {
        super(new FastFileInputStream(raf,length));
    }

    public long remaining(){
        return ((FastFileInputStream)in).remaining();
    }

}