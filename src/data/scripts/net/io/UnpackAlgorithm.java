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
            // integer keys are unique type IDs
            Map<Integer, Map<Integer, BasePackable>> types = new HashMap<>();
            // integer keys are unique record IDs
            Map<Integer, BaseRecord<?>> records = new HashMap<>();

            int entityTypeID = in.readInt();
            int entityInstanceID = in.readInt();

            while (in.readableBytes() > 0) {
                int type = in.readInt();

                if (DataGenManager.entityTypeIDs.containsValue(type)) {
                    // reached new entity
                    entityTypeID = type;

                    insertEntity(types, entityTypeID, entityInstanceID, records);

                    entityInstanceID = in.readInt();
                    records = new HashMap<>();
                } else {
                    int recordTypeID = type;
                    int recordUniqueID = in.readInt();

                    BaseRecord<?> record = DataGenManager.recordFactory(recordTypeID);
                    BaseRecord<?> read = record.read(in);

                    records.put(recordUniqueID, read);
                }
            }

            insertEntity(types, entityTypeID, entityInstanceID, records);

            result = new Unpacked(types,
                    tick,
                    remote,
                    local
            );
        }

        if (in.readableBytes() > 0) throw new IndexOutOfBoundsException(in.readableBytes() + " bytes left in buffer decoder frame");

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
}
