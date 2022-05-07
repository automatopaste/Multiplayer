package data.scripts.net.io;

import data.scripts.net.data.DataManager;
import data.scripts.net.data.packables.APackable;
import data.scripts.net.data.records.ARecord;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Big boy decoder module. Uses polymorphic tricks to allow mod-defined APackable and ARecord types
 */
public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) return;

        int tick = in.readInt();

        // integer keys are unique instance IDs
        Map<Integer, APackable> entities = new HashMap<>();
        // integer keys are unique record IDs
        Map<Integer, ARecord<?>> records = new HashMap<>();

        int type = in.readInt();
        int entityID = type;
        int entityInstanceID = in.readInt();

        while (in.readableBytes() > 0) {
            type = in.readInt();

            if (DataManager.entityTypeIDs.containsValue(type)) {
                // reached new entity
                if (records.isEmpty()) throw new NullPointerException("Entity read zero records: " + entityID);
                entities.put(entityInstanceID, getEntity(entityID, records));

                entityID = type;
                entityInstanceID = in.readInt();
                records = new HashMap<>();
            } else {
                int uniqueID = in.readInt();
                records.put(uniqueID, DataManager.recordFactory(type).read(in));
            }
        }
        if (records.isEmpty()) throw new NullPointerException("Entity read zero records: " + entityID);
        entities.put(entityInstanceID, getEntity(type, records));

        Unpacked unpacked = new Unpacked(entities, tick);

        int readable = in.readableBytes();
        if (readable > 0) throw new OutOfMemoryError(in.readableBytes() + " bytes left in packet decoder frame");

        out.add(unpacked);
    }

    private APackable getEntity(int id, Map<Integer, ARecord<?>> records) {
        return DataManager.entityFactory(id).unpack(id, records);
    }
}
