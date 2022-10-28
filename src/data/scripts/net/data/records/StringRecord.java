package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringRecord extends BaseRecord<String> {
    public static byte TYPE_ID;

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public StringRecord(DeltaFunc<String> deltaFunc, byte uniqueID) {
        super(deltaFunc, uniqueID);
    }

    public StringRecord(String value, byte uniqueID) {
        super(value, uniqueID);
    }

    @Override
    public void write(ByteBuf dest) {
        byte[] bytes = value.getBytes(CHARSET);

        dest.writeInt(bytes.length);
        dest.writeBytes(bytes);
    }

    @Override
    public BaseRecord<String> read(ByteBuf in, byte uniqueID) {
        int length = in.readInt();
        String value = in.readCharSequence(length, CHARSET).toString();

        return new StringRecord(value, uniqueID);
    }

    @Override
    public boolean check() {
        String delta = func.get();
        boolean isUpdated = value != null && !value.equals(delta);
        if (isUpdated) value = delta;

        return isUpdated;
    }

    public static void setTypeId(byte typeId) {
        StringRecord.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static StringRecord getDefault(byte uniqueID) {
        return new StringRecord("DEFAULT", uniqueID);
    }

    @Override
    public String toString() {
        return "StringRecord{" +
                "record='" + value + '\'' +
                '}';
    }
}
