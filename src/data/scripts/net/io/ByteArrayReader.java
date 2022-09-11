package data.scripts.net.io;

import java.nio.charset.Charset;
import java.util.Arrays;

public class ByteArrayReader {
    private int index;
    private final byte[] a;

    public ByteArrayReader(byte[] a) {
        this.a = a;
        index = 0;
    }

    public int readInt() {
        int i = ((a[index] & 0xFF) << 24) | ((a[index + 1] & 0xFF) << 16) | ((a[index + 2] & 0xFF) << 8) | ((a[index + 3] & 0xFF));
        index += 4;
        return i;
    }

    public byte[] readBytes(int length) {
        int i2 = index + length;
        byte[] out =  Arrays.copyOfRange(a, index, i2);
        index = i2;
        return out;
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public String readString(int length, Charset charset) {
        int i2 = index + length;
        String out = new String(a, index, i2, charset);
        index = i2;
        return out;
    }

    public int numBytes() {
        return a.length;
    }
}
