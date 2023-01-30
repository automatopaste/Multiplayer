package data.scripts.net.io;

import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionUtils {

    public static byte[] deflate(byte[] bytes) {
        Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
        compressor.setStrategy(Deflater.FILTERED);
        compressor.setInput(bytes);
        compressor.finish();
        byte[] compressed = new byte[bytes.length + 4];
        int length = compressor.deflate(compressed);
        compressor.end();

        return Arrays.copyOfRange(compressed, 0, length);
    }

    public static byte[] inflate(byte[] bytes, int size, int sizeCompressed) throws DataFormatException {
        Inflater decompressor = new Inflater();
        decompressor.setInput(bytes, 0, sizeCompressed);
        byte[] decompressed = new byte[size + 4];
        int decompressedLength = decompressor.inflate(decompressed);
        decompressor.end();

        return Arrays.copyOfRange(decompressed, 0, size);
    }
}
