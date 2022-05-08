package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringRecord extends ARecord<String> {
    private static int typeID;

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
    public void doWrite(ByteBuffer output) {
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

    public static void setTypeID(int typeID) {
        StringRecord.typeID = typeID;
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
