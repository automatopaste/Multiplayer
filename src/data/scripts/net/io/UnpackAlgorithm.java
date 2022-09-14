package data.scripts.net.io;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.util.DataGenManager;
import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class UnpackAlgorithm {
    public static Unpacked unpack(ByteBuf in, InetSocketAddress remote, InetSocketAddress local) {
        int tick = in.readInt();

        Unpacked result;
        if (in.readableBytes() == 0) {
            result = new Unpacked(
                    new HashMap<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>>(),
                    tick,
                    remote,
                    local
            );
        } else {
            Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> data = new HashMap<>();

            int nextID = in.readInt();
            while (nextID != Integer.MIN_VALUE) {
                nextID = readNextEntity(data, in, nextID);
            }

            result = new Unpacked(
                    data,
                    tick,
                    remote,
                    local
            );
        }

        return result;
    }

    private static int readNextEntity(Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> data, ByteBuf in, int entityTypeID) {
        int entityInstanceID = in.readInt();

        Map<Integer, BaseRecord<?>> records = new HashMap<>();

        int n = in.readInt();
        while (DataGenManager.recordTypeIDs.containsValue(n)) {
            // type
            int recordTypeID = n;
            // unique
            int recordUniqueID = in.readInt();

            //data
            BaseRecord<?> record = DataGenManager.recordFactory(recordTypeID).read(in, recordUniqueID);
            records.put(recordUniqueID, record);

            if (in.readableBytes() > 0) {
                n = in.readInt();
            } else {
                n = Integer.MIN_VALUE;
                break;
            }
        }

        Map<Integer, Map<Integer, BaseRecord<?>>> entities = data.get(entityTypeID);
        if (entities == null) entities = new HashMap<>();

        entities.put(entityInstanceID, records);
        data.put(entityTypeID, entities);

        return n;
    }

    public static Unpacked unpack(byte[] in, InetSocketAddress remote, InetSocketAddress local) {
        ByteArrayReader reader = new ByteArrayReader(in);

        int tick = reader.readInt();

        Unpacked result;
        if (reader.numBytes() == 0) {
            result = new Unpacked(
                    new HashMap<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>>(),
                    tick,
                    remote,
                    local
            );
        } else {
            Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> data = new HashMap<>();

            int nextID = reader.readInt();
            while (nextID != Integer.MIN_VALUE) {
                nextID = readNextEntity(data, reader, nextID);
            }

            result = new Unpacked(
                    data,
                    tick,
                    remote,
                    local
            );
        }

        return result;
    }

    private static int readNextEntity(Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> data, ByteArrayReader reader, int entityTypeID) {
        int entityInstanceID = reader.readInt();

        Map<Integer, BaseRecord<?>> records = new HashMap<>();

        int n = reader.readInt();
        while (DataGenManager.recordTypeIDs.containsValue(n)) {
            // type
            int recordTypeID = n;
            // unique
            int recordUniqueID = reader.readInt();

            //data
            BaseRecord<?> record = DataGenManager.recordFactory(recordTypeID).read(reader, recordUniqueID);
            records.put(recordUniqueID, record);

            if (reader.numBytes() > 0) {
                n = reader.readInt();
            } else {
                n = Integer.MIN_VALUE;
                break;
            }
        }

        Map<Integer, Map<Integer, BaseRecord<?>>> entities = data.get(entityTypeID);
        if (entities == null) entities = new HashMap<>();

        entities.put(entityInstanceID, records);
        data.put(entityTypeID, entities);

        return n;
    }

}
