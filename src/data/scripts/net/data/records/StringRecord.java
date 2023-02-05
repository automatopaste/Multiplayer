package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringRecord extends DataRecord<String> {
    public static byte TYPE_ID;

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public StringRecord(String value) {
        super(value);
    }

    @Override
    public void write(ByteBuf dest) {
        byte[] bytes = value == null ? "NONE".getBytes(CHARSET) : value.getBytes(CHARSET);

        if (bytes.length > Byte.MAX_VALUE) {
            byte[] b = new byte[Byte.MAX_VALUE];
            System.arraycopy(bytes, 0, b, 0, b.length);
            bytes = b;
        }

        dest.writeByte(bytes.length);
        dest.writeBytes(bytes);
    }

    @Override
    public String read(ByteBuf in) {
        int length = in.readByte();
        String value = in.readCharSequence(length, CHARSET).toString();
        if (value.equals("NONE")) value = null;

        return value;
    }

    @Override
    public boolean checkUpdate(String delta) {
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
