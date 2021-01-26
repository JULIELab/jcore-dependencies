package dragon.util;

import java.io.*;

/**
 * <p>Fast binary data writer</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FastBinaryWriter extends DataOutputStream {
    private static final int BUF_SIZE = 1024 * 1024;
    private ByteArrayOutputStream baos;
    private DataOutputStream dos;
    private long offset;

    public FastBinaryWriter(String outputFile) {
        super(new ByteArrayOutputStream());
        baos = (ByteArrayOutputStream) out;
        try {
            dos = new DataOutputStream(new FileOutputStream(new File(outputFile)));

            offset = 0;
        }
        catch (Exception e) {
            dos = null;
            e.printStackTrace();
        }
    }

    public FastBinaryWriter(String outputFile, boolean append) {
        super(new ByteArrayOutputStream());
        baos = (ByteArrayOutputStream) out;

        File file = new File(outputFile);
        try {
            dos = new DataOutputStream(new FileOutputStream(file, append));
            offset = file.length();
        }
        catch (Exception e) {
            dos = null;
            e.printStackTrace();
        }
    }

    public long getFilePointer() {
        return offset + size();
    }

    public int bytesInBuffer(){
        return baos.size();
    }

    public void flush() {
        try {
            baos.writeTo(dos);
            baos.reset();
            dos.flush();
            super.flush();
        }
        catch (Exception e) {
        	//JDK6 will throw out a misleading exception: the handle is invalid
            //e.printStackTrace();
        }
    }

    public void close() {
        try {
            flush();
            baos.close();
            dos.close();
            super.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] buf, int off, int len) {
        try {
            super.write(buf, off, len);
            if (baos.size() >= BUF_SIZE) flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(InputStream src, long length) {
        byte[] buf;
        int count;

        try {
            if(length<=0) return;

            buf=new byte[(int)(10240<length?10240:length)];
            while (length > 0) {
                count = (int) (length > buf.length ? buf.length : length);
                count = src.read(buf, 0, count);
                if (count > 0) {
                    write(buf, 0, count);
                    length = length - count;
                }
                else {
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(RandomAccessFile src, long length) {
        int count;
        byte[] buf;

        try {
            buf=new byte[(int)(10240<length?10240:length)];
            while (length > 0) {
                count = (int) (length > buf.length ? buf.length : length);
                count = src.read(buf, 0, count);
                if (count > 0) {
                    write(buf, 0, count);
                    length = length - count;
                }
                else {
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}