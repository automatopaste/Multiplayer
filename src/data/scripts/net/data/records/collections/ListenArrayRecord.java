package data.scripts.net.data.records.collections;

import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.util.DataGenManager;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends a one-way list of values
 * @param <E> Value type
 */
public class ListenArrayRecord<E> extends BaseRecord<List<E>> {

    public static byte TYPE_ID;
    private final byte elementTypeID;

    private final BaseRecord<E> writer;

    public ListenArrayRecord(List<E> value, byte elementTypeID) {
        super(value);

        this.elementTypeID = elementTypeID;

        if (elementTypeID != (byte) -1) {
            writer = (BaseRecord<E>) DataGenManager.recordFactory(elementTypeID);
        } else {
            writer = null;
        }
    }

    @Override
    public void write(ByteBuf dest) {
        if (value.size() > Byte.MAX_VALUE) {
            throw new RuntimeException("List size exceeded " + Byte.MAX_VALUE + " elements");
        }

        dest.writeByte(elementTypeID);
        dest.writeByte(value.size());

        for (E e : value) {
            writer.overwrite(e);
            writer.write(dest);
        }
    }

    @Override
    public List<E> read(ByteBuf in) {
        List<E> out = new ArrayList<>();

        byte type = in.readByte();
        byte num = in.readByte();

        BaseRecord<E> reader = (BaseRecord<E>) DataGenManager.recordFactory(type);

        for (byte i = 0; i < num; i++) {
            reader.read(in);
            out.add(reader.getValue());
        }

        return out;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    protected boolean checkUpdate(List<E> delta) {
        return !delta.isEmpty();
    }

    public static void setTypeId(byte typeId) {
        ListenArrayRecord.TYPE_ID = typeId;
    }
}
