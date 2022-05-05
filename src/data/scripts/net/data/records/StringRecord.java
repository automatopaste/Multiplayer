package data.scripts.net.data.records;

import data.scripts.net.data.IDTypes;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringRecord extends ARecord {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private String record;
    private final int uniqueId;

    public StringRecord(String value, int uniqueId) {
        record = value;
        this.uniqueId = uniqueId;
    }

    public boolean update(String curr) {
        boolean isUpdated = !record.equals(curr);
        if (isUpdated) record = curr;

        return true;
        //return isUpdated;
    }

    @Override
    public void write(ByteBuffer output) {
        super.write(output);

        byte[] bytes = record.getBytes(CHARSET);

        output.putInt(bytes.length);
        for (byte b : bytes) {
            output.put(b);
        }
    }

    public static StringRecord read(ByteBuf input) {
        int uniqueId = ARecord.readID(input);

        int length = input.readInt();
        String value = input.readCharSequence(length, CHARSET).toString();

        return new StringRecord(value, uniqueId);
    }

    @Override
    public int getTypeId() {
        return IDTypes.STRING_RECORD;
    }

    @Override
    public int getUniqueId() {
        return 0;
    }

    @Override
    public String toString() {
        return "StringRecord{" +
                "record='" + record + '\'' +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
