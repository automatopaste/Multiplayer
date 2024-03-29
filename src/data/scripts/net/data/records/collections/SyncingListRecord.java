package data.scripts.net.data.records.collections;

import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.DataGenManager;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Attempts to sync a List of values
 * broken pls fix
 * @param <E> Value type
 */
public class SyncingListRecord<E> extends DataRecord<List<E>> {
    public static byte TYPE_ID;
    private final byte elementTypeID;

    private final DataRecord<E> writer;

    private final Map<Byte, E> toWrite = new HashMap<>();

    public SyncingListRecord(List<E> collection, byte elementTypeID) {
        super(collection);
        this.elementTypeID = elementTypeID;

        if (elementTypeID != (byte) -1) {
            writer = (DataRecord<E>) DataGenManager.recordFactory(elementTypeID);
        } else {
            writer = null;
        }
    }

    @Override
    public boolean sourceExecute(SourceExecute<List<E>> sourceExecute) {
//        List<E> delta = sourceExecute.get();
//
//        boolean update = false;
//
//        for (byte i = 0; i < delta.size(); i++) {
//            E d = delta.get(i);
//
//            if (i + 1 > value.size()) {
//                toWrite.put(i, d);
//                update = true;
//            } else if (!d.equals(value.get(i))) {
//                toWrite.put(i, d);
//                update = true;
//            }
//        }
//
//        value = delta;
//
//        return update;
        value = sourceExecute.get();
        return true;
    }


    @Override
    public void write(ByteBuf dest) {
        if (value.size() > Byte.MAX_VALUE) {
            throw new RuntimeException("List size exceeded " + Byte.MAX_VALUE + " elements");
        }

//        if (toWrite.size() == 0) return;
//
//        dest.writeByte(elementTypeID);
//        dest.writeByte(toWrite.size());
//
//        for (byte i : toWrite.keySet()) {
//            // write index
//            dest.writeByte(i);
//
//            // write data
//            writer.overwrite(toWrite.get(i));
//            writer.write(dest);
//        }
//
//        toWrite.clear();

        dest.writeByte(elementTypeID);
        dest.writeByte(value.size());

        for (byte i = 0; i < value.size(); i++) {
            E e = value.get(i);

            dest.writeByte(i);

            writer.overwrite(e);
            writer.write(dest);
        }
    }

    @Override
    public List<E> read(ByteBuf in) {
        byte type = in.readByte();
        byte num = in.readByte();

        DataRecord<E> reader = (DataRecord<E>) DataGenManager.recordFactory(type);

        Map<Byte, E> data = new HashMap<>();
        int max = 0;
        for (int i = 0; i < num; i++) {
            byte index = in.readByte();

            max = Math.max(index, max);

            E e = reader.read(in);

            data.put(index, e);
        }
        max++;

        List<E> value = new ArrayList<>(max);
        for (int i = 0; i < max; i++) {
            value.add(null);
        }

        for (byte b : data.keySet()) {
            value.set(b, data.get(b));
        }

        return value;
    }

    @Override
    public void overwrite(Object o) {
        List<E> delta = (List<E>) o;

        List<E> temp;
        int size = Math.max(delta.size(), value.size());
        temp = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            temp.add(null);
        }

        for (int i = 0; i < size; i++) {
            E d = i < delta.size() ? delta.get(i) : null;
            E v = i < value.size() ? value.get(i) : null;

            if (d != null) {
                temp.set(i, d);
            } else if (v != null) {
                temp.set(i, v);
            }
        }

        value = temp;
    }

    public static void setTypeId(byte typeId) {
        SyncingListRecord.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    protected boolean checkUpdate(List<E> delta) {
        return true;
    }

    @Override
    public int size() {
        return 2 + (value.size() * writer.size());
    }
}
