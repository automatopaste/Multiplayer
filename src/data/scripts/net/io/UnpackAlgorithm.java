package data.scripts.net.io;

import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.util.DataGenManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class UnpackAlgorithm {
    public static Unpacked unpack(ByteBuf in, InetSocketAddress remote, InetSocketAddress local) {
        int tick = in.readInt();
        int connectionID = in.readInt();

        Unpacked result;
        if (in.readableBytes() == 0) {
            result = new Unpacked(
                    new HashMap<Byte, Map<Short, Map<Byte, BaseRecord<?>>>>(),
                    tick,
                    remote,
                    local,
                    connectionID
            );
        } else {
            Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> data = new HashMap<>();

            byte nextID = in.readByte();
            Result r;
            do {
                r = readNextEntity(data, in, nextID);
                nextID = r.nextByte;
            } while (!r.result);

            result = new Unpacked(
                    data,
                    tick,
                    remote,
                    local,
                    connectionID
            );
        }

        return result;
    }

    private static Result readNextEntity(Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> data, ByteBuf in, byte entityTypeID) {
        Result result = new Result();

        short entityInstanceID = in.readShort();

        Map<Byte, BaseRecord<?>> records = new HashMap<>();

        byte nextByte = in.readByte();
        while (DataGenManager.recordTypeIDs.containsValue(nextByte)) {
            // type
            byte recordTypeID = nextByte;
            // unique
            byte recordUniqueID = in.readByte();

            //data
            BaseRecord<?> record = DataGenManager.recordFactory(recordTypeID).read(in, recordUniqueID);
            records.put(recordUniqueID, record);

            if (in.readableBytes() > 0) {
                nextByte = in.readByte();
            } else {
                result.result = true;
                break;
            }
        }

        Map<Short, Map<Byte, BaseRecord<?>>> entities = data.get(entityTypeID);
        if (entities == null) entities = new HashMap<>();

        entities.put(entityInstanceID, records);
        data.put(entityTypeID, entities);

        result.nextByte = nextByte;

        return result;
    }

    public static class Result {
        public boolean result = false;
        public byte nextByte;
    }

    public static Unpacked unpack(byte[] in, InetSocketAddress remote, InetSocketAddress local) {
        ByteBuf reader = PooledByteBufAllocator.DEFAULT.buffer(in.length);

        reader.writeBytes(in);
        Unpacked unpacked = unpack(reader, remote, local);
        reader.release();

        return unpacked;
    }
}
