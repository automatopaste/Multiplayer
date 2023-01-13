package data.scripts.net.data.records;

import data.scripts.net.data.util.DataGenManager;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListRecord<E> extends BaseRecord<List<E>> {
    public static byte TYPE_ID;
    private final byte elementTypeID;

    private final BaseRecord<E> writer;
    private final Map<Byte, E> toWrite;

    public ListRecord(List<E> collection, byte elementTypeID) {
        super(collection);
        this.elementTypeID = elementTypeID;

        writer = (BaseRecord<E>) DataGenManager.recordFactory(elementTypeID);
        toWrite = new HashMap<>();
    }

    @Override
    protected boolean checkNotEqual(List<E> delta) {
        if (value.size() > Byte.MAX_VALUE) throw new RuntimeException("List size exceeded " + Byte.MAX_VALUE + " elements");

        toWrite.clear();

        boolean update = false;
        for (byte i = 0; i < delta.size(); i++) {
            E d = delta.get(i);
            if (d == null) continue;

            E e = value.get(i);
            if (!e.equals(d)) {
                update = true;
                toWrite.put(i, e);
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
            writer.value = value.get(i);
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

    public void addElement(E e) {
        if (!(e instanceof BaseRecord)) throw new ClassCastException("Elements must extend " + BaseRecord.class.getName());
        value.add(e);
    }

    public static void setTypeId(byte typeId) {
        ListRecord.TYPE_ID = typeId;
    }

    @Override
    public byte getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String toString() {
        return "ListRecord{" +
                "record=" + value.toString() +
                '}';
    }
}
