package data.scripts.net.data.records;

import data.scripts.net.data.IDTypes;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringRecord extends ARecord<String> {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private String record;

    public StringRecord(String value) {
        record = value;
    }

    public boolean checkUpdate(String curr) {
        boolean isUpdated = !record.equals(curr);
        if (isUpdated) record = curr;

        return isUpdated;
    }

    @Override
    public void write(ByteBuffer output, int uniqueId) {
        super.write(output, uniqueId);

        byte[] bytes = record.getBytes(CHARSET);

        output.putInt(bytes.length);
        for (byte b : bytes) {
            output.put(b);
        }
    }

    public static StringRecord read(ByteBuf input) {
        int length = input.readInt();
        String value = input.readCharSequence(length, CHARSET).toString();

        return new StringRecord(value);
    }

    @Override
    public int getTypeId() {
        return IDTypes.STRING_RECORD;
    }

    public String getRecord() {
        return record;
    }

    @Override
    public String toString() {
        return "StringRecord{" +
                "record='" + record + '\'' +
                '}';
    }
}
