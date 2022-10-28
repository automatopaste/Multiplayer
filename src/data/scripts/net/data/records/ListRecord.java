package data.scripts.net.data.records;

import data.scripts.net.data.util.DataGenManager;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ListRecord<E> extends BaseRecord<List<E>> {
    public static byte TYPE_ID;
    private final byte elementTypeID;

    public ListRecord(List<E> collection, byte uniqueID, byte elementTypeID) {
        super(collection, uniqueID);
        this.elementTypeID = elementTypeID;
    }

    public ListRecord(DeltaFunc<List<E>> deltaFunc, byte uniqueID, byte elementTypeID) {
        super(deltaFunc, uniqueID);
        this.elementTypeID = elementTypeID;
    }

    @Override
    public boolean check() {
//        for (E e : value) {
//            BaseRecord<?> record = (BaseRecord<?>) e;
//            if (record.check()) return true;
//        }
//        return false;

        // always returns true because contents all have null deltas
        return true;
    }

    @Override
    public void write(ByteBuf dest) {
        dest.writeByte(elementTypeID);
        dest.writeByte(value.size());

        for (E e : value) {
            BaseRecord<?> record = (BaseRecord<?>) e;
            record.write(dest);
        }
    }

    @Override
    public BaseRecord<List<E>> read(ByteBuf in, byte uniqueID) {
        byte type = in.readByte();
        byte num = in.readByte();

        BaseRecord<?> reader = DataGenManager.recordFactory(type);
        List<E> data = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            data.add((E) reader.read(in, (byte) -1));
        }

        return new ListRecord<>(data, uniqueID, type);
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

    public static ListRecord<?> getDefault(byte uniqueID, byte elementTypeID) {
        return new ListRecord<>(new ArrayList<>(), uniqueID, elementTypeID);
    }
}
