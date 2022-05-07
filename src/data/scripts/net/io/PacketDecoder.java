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

        Unpacked unpacked = getUnpacked(in, tick);

        int readable = in.readableBytes();
        if (readable > 0) throw new OutOfMemoryError(in.readableBytes() + " bytes left in packet decoder frame");

        out.add(unpacked);
    }

    private Unpacked getUnpacked(ByteBuf in, int tick) {
        if (in.readableBytes() == 0) return new Unpacked(new HashMap<Integer, APackable>(), tick);

        // integer keys are unique instance IDs
        Map<Integer, APackable> entities = new HashMap<>();
        // integer keys are unique record IDs
        Map<Integer, ARecord<?>> records = new HashMap<>();

        int entityID = in.readInt();
        int entityInstanceID = in.readInt();

        while (in.readableBytes() > 0) {
            int type = in.readInt();

            if (DataManager.entityTypeIDs.containsValue(type)) {
                // reached new entity
                if (records.isEmpty()) throw new NullPointerException("Entity read zero records: " + entityInstanceID);
                APackable entity = DataManager.entityFactory(entityID).unpack(entityInstanceID, records);
                entities.put(entityInstanceID, entity);

                entityID = type;
                entityInstanceID = in.readInt();
                records = new HashMap<>();
            } else {
                int uniqueID = in.readInt();
                records.put(uniqueID, DataManager.recordFactory(type).read(in));
            }
        }
        if (records.isEmpty()) throw new NullPointerException("Entity read zero records: " + entityInstanceID);
        APackable entity = DataManager.entityFactory(entityID).unpack(entityInstanceID, records);
        entities.put(entityInstanceID, entity);

        return new Unpacked(entities, tick);
    }
}
