package data.scripts.net.io.tcp;

import data.scripts.data.DataGenManager;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.io.Unpacked;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.lazywizard.console.Console;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BufferUnpacker extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) return;

        int tick = in.readInt();

        Unpacked result;
        if (in.readableBytes() == 0) {
            result = new Unpacked(new HashMap<Integer, BasePackable>(), tick, null, null);
        } else {
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
                    if (records.isEmpty())
                        throw new NullPointerException("Entity read zero records: " + entityInstanceID);
                    BasePackable entity = DataGenManager.entityFactory(entityID).unpack(entityInstanceID, records);
                    entities.put(entityInstanceID, entity);

                    entityID = type;
                    entityInstanceID = in.readInt();
                    records = new HashMap<>();
                } else {
                    int uniqueID = in.readInt();
                    BaseRecord<?> record = DataGenManager.recordFactory(type);
                    BaseRecord<?> read = record.read(in);
                    records.put(uniqueID, read);
                }
            }
            if (records.isEmpty()) Console.showMessage("Entity read zero records: " + entityID);
            BasePackable entity = DataGenManager.entityFactory(entityID).unpack(entityInstanceID, records);
            entities.put(entityInstanceID, entity);
            result = new Unpacked(entities, tick, null, null);
        }

        if (in.readableBytes() > 0) throw new IndexOutOfBoundsException(in.readableBytes() + " bytes left in packet decoder frame");

        out.add(result);
    }
}
