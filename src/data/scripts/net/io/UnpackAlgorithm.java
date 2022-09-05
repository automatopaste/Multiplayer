package data.scripts.net.io;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.util.DataGenManager;
import io.netty.buffer.ByteBuf;
import org.lazywizard.console.Console;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class UnpackAlgorithm {
    public static Unpacked unpack(ByteBuf in, InetSocketAddress remote, InetSocketAddress local) {
        int tick = in.readInt();

        Unpacked result;
        if (in.readableBytes() == 0) {
            result = new Unpacked(
                    new HashMap<Integer, Map<Integer, BasePackable>>(),
                    tick,
                    remote,
                    local
            );
        } else {
            Map<Integer, Map<Integer, BasePackable>> data = new HashMap<>();

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

    private static void insertEntity(Map<Integer, Map<Integer, BasePackable>> types, int entityTypeID, int entityInstanceID, Map<Integer, BaseRecord<?>> records) {
        if (records.isEmpty()) Console.showMessage("Entity read zero records: " + entityTypeID);
        BasePackable entity = DataGenManager.entityFactory(entityTypeID).unpack(entityInstanceID, records);

        Map<Integer, BasePackable> entities = types.get(entityTypeID);
        if (entities == null) entities = new HashMap<>();
        entities.put(entityInstanceID, entity);
        types.put(entityTypeID, entities);
    }

    private static int readNextEntity(Map<Integer, Map<Integer, BasePackable>> data, ByteBuf in, int entityTypeID) {
        int entityInstanceID = in.readInt();

        Map<Integer, BaseRecord<?>> records = new HashMap<>();

        int n = in.readInt();
        while (DataGenManager.recordTypeIDs.containsValue(n)) {
            // type
            int recordTypeID = n;
            // unique
            int recordUniqueID = in.readInt();

            //data
            BaseRecord<?> record = DataGenManager.recordFactory(recordTypeID).read(in);
            records.put(recordUniqueID, record);

            if (in.readableBytes() > 0) {
                n = in.readInt();
            } else {
                n = Integer.MIN_VALUE;
                break;
            }
        }

        Map<Integer, BasePackable> entities = data.get(entityTypeID);
        if (entities == null) entities = new HashMap<>();

        BasePackable entity = DataGenManager.entityFactory(entityTypeID).unpack(entityInstanceID, records);
        entities.put(entityInstanceID, entity);
        data.put(entityTypeID, entities);

        return n;
    }
}
