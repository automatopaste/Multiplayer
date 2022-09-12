package data.scripts.net.io;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionUtils {

    public static byte[] deflate(byte[] bytes) {
        Deflater compressor = new Deflater(Deflater.BEST_SPEED);
        compressor.setInput(bytes);
        compressor.finish();
        byte[] compressed = new byte[bytes.length];
        int length = compressor.deflate(compressed);
        compressor.end();

        return compressed;
    }

    public static byte[] inflate(byte[] bytes, int size) throws DataFormatException {
        Inflater decompressor = new Inflater();
        decompressor.setInput(bytes);
        byte[] decompressed = new byte[size];
        decompressor.inflate(decompressed);
        decompressor.end();

        return decompressed;
    }
}
