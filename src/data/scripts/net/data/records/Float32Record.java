package data.scripts.net.data.records;

import io.netty.buffer.ByteBuf;

import java.util.Objects;

public class Float32Record extends DataRecord<Float> {
    public static byte TYPE_ID;

    public Float32Record(Float record) {
        super(record);
    }

    @Override
    protected boolean checkUpdate(Float delta) {
        return !Objects.equals(value, delta);
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeFloat(value);
    }

    @Override
    public Float read(ByteBuf in) {
        return in.readFloat();
    }

    @Override
    public Float linterp(float p, Float v1, Float v2) {
        return (p * (v2 - v1)) + v1;
    }

    public static void setTypeId(byte typeId) {
        Float32Record.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    public static DataRecord<Float> getDefault() {
        return new Float32Record(0f);
    }
}
