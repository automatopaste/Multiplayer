package data.scripts.net.data.records.collections;

import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.DataGenManager;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends a one-way list of values
 * @param <E> Value type
 */
public class ListenArrayRecord<E> extends DataRecord<List<E>> {

    public static byte TYPE_ID;
    private final byte elementTypeID;

    private final DataRecord<E> writer;

    public ListenArrayRecord(List<E> value, byte elementTypeID) {
        super(value);

        this.elementTypeID = elementTypeID;

        if (elementTypeID != (byte) -1) {
            writer = (DataRecord<E>) DataGenManager.recordFactory(elementTypeID);
        } else {
            writer = null;
        }
    }

    @Override
    public void write(ByteBuf dest) {
        if (value.size() > 0b11111111) {
            throw new RuntimeException("List size exceeded " + 0b11111111 + " elements");
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
        int num = in.readByte() & 0xFF;

        DataRecord<E> reader = (DataRecord<E>) DataGenManager.recordFactory(type);

        for (int i = 0; i < num; i++) {
            out.add(reader.read(in));
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
