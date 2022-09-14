package data.scripts.net.data.records;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.io.ByteArrayReader;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringRecord extends BaseRecord<String> {
    public static int TYPE_ID;

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public StringRecord(DeltaFunc<String> deltaFunc, int uniqueID) {
        super(deltaFunc, uniqueID);
    }

    public StringRecord(String value, int uniqueID) {
        super(value, uniqueID);
    }

    @Override
    public void get(ByteBuf dest) {
        dest.writeCharSequence(value, CHARSET);
    }

    @Override
    public BaseRecord<String> read(ByteBuf in, int uniqueID) {
        int length = in.readInt();
        String value = in.readCharSequence(length, CHARSET).toString();

        return new StringRecord(value, uniqueID);
    }

    @Override
    public BaseRecord<String> read(ByteArrayReader in, int uniqueID) {
        int length = in.readInt();
        String value = in.readString(length, CHARSET);

        return new StringRecord(value, uniqueID);
    }

    @Override
    public boolean check() {
        String delta = func.get();
        boolean isUpdated = value != null && !value.equals(delta);
        if (isUpdated) value = delta;

        return isUpdated;
    }

    public static void setTypeId(int typeId) {
        StringRecord.TYPE_ID = typeId;
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    public static StringRecord getDefault(int uniqueID) {
        return new StringRecord("DEFAULT", uniqueID);
    }

    @Override
    public String toString() {
        return "StringRecord{" +
                "record='" + value + '\'' +
                '}';
    }
}
