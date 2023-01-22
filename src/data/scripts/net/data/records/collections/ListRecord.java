package data.scripts.net.data.records.collections;

import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.util.DataGenManager;
import io.netty.buffer.ByteBuf;

import java.util.*;


public class ListRecord<E> extends BaseRecord<List<E>> {
    public static byte TYPE_ID;
    private final byte elementTypeID;

    private final BaseRecord<E> writer;
    private final Map<Byte, E> toWrite;

    public ListRecord(List<E> collection, byte elementTypeID) {
        super(collection);
        this.elementTypeID = elementTypeID;

        if (elementTypeID != (byte) -1) {
            writer = (BaseRecord<E>) DataGenManager.recordFactory(elementTypeID);
        } else {
            writer = null;
        }

        toWrite = new HashMap<>();
    }

    @Override
    protected boolean checkNotEqual(List<E> delta) {
        if (value.size() > Byte.MAX_VALUE) throw new RuntimeException("Array size exceeded " + Byte.MAX_VALUE + " elements");

        toWrite.clear();

        boolean update = delta.size() != value.size();

        for (byte i = 0; i < delta.size(); i++) {
            E d = delta.get(i);

            if (i + 1> value.size()) {
                toWrite.put(i, d);
            } else {
                if (!d.equals(value.get(i))) {
                    toWrite.put(i, d);
                    update = true;
                }
            }
        }

        return update;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeByte(elementTypeID);

        if (value.size() > Byte.MAX_VALUE) throw new RuntimeException("List size exceeded " + Byte.MAX_VALUE + " elements");

        // write num entry updates
        dest.writeByte(toWrite.size());

        for (byte i : toWrite.keySet()) {
            // write index
            dest.writeByte(i);

            // write data
            writer.overwrite(toWrite.get(i));
            writer.write(dest);
        }
    }

    @Override
    public BaseRecord<List<E>> read(ByteBuf in) {
        byte type = in.readByte();
        byte num = in.readByte();

        BaseRecord<?> reader = DataGenManager.recordFactory(type);
        List<E> data = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            data.add((E) reader.read(in));
        }

        return new ListRecord<>(data, type);
    }

    public static void setTypeId(byte typeId) {
        ListRecord.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }
}
