package data.scripts.net.data.records;

import data.scripts.net.data.DataManager;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringRecord extends ARecord<String> {
    private static final int typeID;
    static {
        typeID = DataManager.registerRecordType(StringRecord.class, new StringRecord(null));
    }

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public StringRecord(String record) {
        super(record);
    }

    @Override
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

    @Override
    public StringRecord read(ByteBuf input) {
        int length = input.readInt();
        String value = input.readCharSequence(length, CHARSET).toString();

        return new StringRecord(value);
    }

    @Override
    public int getTypeId() {
        return typeID;
    }

    @Override
    public String toString() {
        return "StringRecord{" +
                "record='" + record + '\'' +
                '}';
    }
}
