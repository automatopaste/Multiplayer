package data.scripts.net.io;

import data.scripts.data.DataGenManager;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;
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
        if (readable > 0) throw new IndexOutOfBoundsException(in.readableBytes() + " bytes left in packet decoder frame");

        out.add(unpacked);
    }

    private Unpacked getUnpacked(ByteBuf in, int tick) {
        if (in.readableBytes() == 0) return new Unpacked(new HashMap<Integer, BasePackable>(), tick);

        // unpack deleted instance IDs
//        int numDeleted = in.readInt();
//        int n = 0;
//        List<Integer> deleted = new ArrayList<>();
//        while (n < numDeleted) {
//            deleted.add(in.readInt());
//            n++;
//        }

        if (in.readableBytes() == 0) {
            return new Unpacked(new HashMap<Integer, BasePackable>(), tick);
        }

        // integer keys are unique instance IDs
        Map<Integer, BasePackable> entities = new HashMap<>();
        // integer keys are unique record IDs
        Map<Integer, BaseRecord<?>> records = new HashMap<>();

        int entityID = in.readInt();
        int entityInstanceID = in.readInt();

        while (in.readableBytes() > 0) {
            int type = in.readInt();

            if (DataGenManager.entityTypeIDs.containsValue(type)) {
                // reached new entity
                if (records.isEmpty()) throw new NullPointerException("Entity read zero records: " + entityInstanceID);
                BasePackable entity = DataGenManager.entityFactory(entityID).unpack(entityInstanceID, records);
                entities.put(entityInstanceID, entity);

                entityID = type;
                entityInstanceID = in.readInt();
                records = new HashMap<>();
            } else {
                int uniqueID = in.readInt();
                records.put(uniqueID, DataGenManager.recordFactory(type).read(in));
            }
        }
        if (records.isEmpty()) throw new NullPointerException("Entity read zero records: " + entityInstanceID);
        BasePackable entity = DataGenManager.entityFactory(entityID).unpack(entityInstanceID, records);
        entities.put(entityInstanceID, entity);

        return new Unpacked(entities, tick);
    }
}
