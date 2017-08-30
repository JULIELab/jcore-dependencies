package dragon.util;

import java.io.*;

/**
 * <p>Fast file input stream </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class FastFileInputStream
    extends InputStream {
    private byte[] buf;
    private int buf_length;
    private int buf_pos;
    private long available;
    private FileInputStream fis;
    private RandomAccessFile raf;
    private int inputType;
    private boolean class_open_file;

    public FastFileInputStream(String filename) {
        try {
            File f=new File(filename);
            available=f.length();
            fis = new FileInputStream(f);
            raf=null;
            buf_length =512;
            buf = new byte[buf_length];
            buf_pos = buf_length;
            inputType=1;
            class_open_file=true;
        }
        catch (Exception e) {
            fis = null;
            e.printStackTrace();
        }
    }

    public FastFileInputStream(File f) {
        try {
            available=f.length();
            fis = new FileInputStream(f);
            raf=null;
            buf_length = 512;
            buf = new byte[buf_length];
            buf_pos = buf_length;
            inputType=1;
            class_open_file=true;
        }
        catch (Exception e) {
            fis = null;
            e.printStackTrace();
        }
    }

    public FastFileInputStream(RandomAccessFile raf, long length){
        buf_length = 512;
        buf = new byte[buf_length];
        buf_pos = buf_length;
        this.raf=raf;
        fis=null;
        available=length;
        inputType=2;
        class_open_file=false;
     }

    public int read() throws java.io.IOException {
        refillBuffer();
        if (available<= 0) {
            return -1;
        }

        buf_pos++;
        available--;
        return (int) buf[buf_pos-1] & 0xFF;

    }
    public int read(byte[] buffer) throws java.io.IOException{
        return read(buffer,0,buffer.length);
    }

    public int read(byte[] buffer, int off, int len) throws java.io.IOException {
        int count, left;

        refillBuffer();
        if (available <= 0) {
            return -1;
        }

        count = buf_length - buf_pos;
        if(available<len) len=(int)available;

        if (len <= count) {
            System.arraycopy(buf, buf_pos, buffer, off, len);
            buf_pos += len;
            available=available-len;
            return len;
        }
        else {
            System.arraycopy(buf, buf_pos, buffer, off, count);
            buf_pos = buf_length;
            left =internalRead(buffer, off + count, len - count);
            if (left <= 0) {
                available = 0; //reach the end of the file
                return count;
            }
            else {
                available=available-(count+left);
                return count + left;
            }
        }
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int readLimit) {
        //not supported
    }

    public void reset() {
        //not supported
    }

    public long skip(long n) throws java.io.IOException {
        int count;
        long left;

        if(available<n) n=available;
        count = buf_length - buf_pos;
        if (n <= count) {
            buf_pos += n;
            available=available-n;
            return n;
        }
        else {
            buf_pos = buf_length;
            left =internalSkip(n - count);
            available=available-count-left;
            return count + left;
        }
    }

    public int available(){
        return (int)available;
    }

    public long remaining(){
        return available;
    }

    public void close() throws java.io.IOException {
        if(class_open_file){
            if(fis!=null) fis.close();
            if(raf!=null) raf.close();
        }
    }

    private void refillBuffer() {
        if (available<=0 || buf_pos < buf_length) {
            return;
        }

        try {
            buf_length =internalRead(buf,0,buf.length);
            buf_pos = 0;
        }
        catch (Exception e) {
            buf_length = 0;
            e.printStackTrace();
        }
    }

    private int internalRead(byte[] buffer, int off, int len) throws java.io.IOException {
        if(inputType==1)
            return fis.read(buffer,off,len);
        else
            return raf.read(buffer,off,len);
    }

    private long internalSkip(long n)throws java.io.IOException {
        if(inputType==1)
            return fis.skip(n);
        else
            return raf.skipBytes((int)n);
    }

}