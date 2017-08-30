package dragon.util;

import java.io.*;

/**
 * <p>Writing byte array</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ByteArrayWriter implements DataOutput{
    private ByteArrayOutputStream baos;

    public ByteArrayWriter(int capacity) {
        baos=new ByteArrayOutputStream(capacity);
    }

    public ByteArrayWriter() {
        baos=new ByteArrayOutputStream();
    }

    public void write(int value) throws IOException {
        baos.write(value);
    }

    public void writeByte (int value) throws IOException {
        baos.write(value);
    }

    public void writeBytes (String value) throws IOException {
            baos.write(value.getBytes());
    }

    public void writeBoolean (boolean value) throws IOException {
        if(value)
            baos.write(1);
        else
            baos.write(0);
    }

    public void writeChar (int value) throws IOException {
            baos.write(ByteArrayConvert.toByte((char)value));
    }

    public void writeChars (String value) throws IOException {
        int i, len;

        len=value.length();
        for(i=0;i<len;i++)
            baos.write(ByteArrayConvert.toByte(value.charAt(i)));
    }

    public void writeShort (int value) throws IOException {
            baos.write(ByteArrayConvert.toByte((short)value));
    }

    public void writeInt (int value) throws IOException {
        baos.write(ByteArrayConvert.toByte(value));
    }

    public void writeLong (long value) throws IOException {
        baos.write(ByteArrayConvert.toByte(value));
    }

    public void writeFloat (float value) throws IOException {
        baos.write(ByteArrayConvert.toByte(value));
    }

    public void writeDouble (double value) throws IOException {
        baos.write(ByteArrayConvert.toByte(value));
    }

    public void write (byte[] value) throws IOException {
        baos.write(value);
    }

    public void write (byte[] value, int off, int len) throws IOException {
        baos.write(value, off, len);
    }

    public void writeUTF (String value) throws IOException {
        System.out.println("not implemented yet!");
    }

    public void reset(){
        baos.reset();
    }

    public int size(){
        return baos.size();
    }

    public byte[] toByteArray(){
        return baos.toByteArray();
    }

    public void close() throws IOException{
        baos.close();
    }
}