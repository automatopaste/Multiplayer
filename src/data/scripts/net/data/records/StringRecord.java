package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringRecord extends BaseRecord<String> {
    public static byte TYPE_ID;

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public StringRecord(String value) {
        super(value);
    }

    @Override
    public void write(ByteBuf dest) {
        byte[] bytes = value.getBytes(CHARSET);

        dest.writeInt(bytes.length);
        dest.writeBytes(bytes);
    }

    @Override
    public BaseRecord<String> read(ByteBuf in) {
        int length = in.readInt();
        String value = in.readCharSequence(length, CHARSET).toString();

        return new StringRecord(value);
    }

    @Override
    public boolean checkNotEqual(String delta) {
        return value != null && !value.equals(delta);
    }

    public static void setTypeId(byte typeId) {
        StringRecord.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static StringRecord getDefault() {
        return new StringRecord("DEFAULT");
    }

    @Override
    public String toString() {
        return "StringRecord{" +
                "record='" + value + '\'' +
                '}';
    }
}
