package data.scripts.net.io.tcp;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.io.Unpacked;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.lazywizard.console.Console;

import java.net.InetSocketAddress;
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
            result = new Unpacked(
                    new HashMap<Integer, Map<Integer, BasePackable>>(),
                    tick,
                    (InetSocketAddress) channelHandlerContext.channel().remoteAddress(),
                    (InetSocketAddress) channelHandlerContext.channel().localAddress()
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

                    if (records.isEmpty())
                        throw new NullPointerException("Entity read zero records: " + entityInstanceID);
                    BasePackable entity = DataGenManager.entityFactory(entityTypeID).unpack(entityInstanceID, records);
                    types.get(entityTypeID).put(entityInstanceID, entity);

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
            if (records.isEmpty()) Console.showMessage("Entity read zero records: " + entityTypeID);
            BasePackable entity = DataGenManager.entityFactory(entityTypeID).unpack(entityInstanceID, records);
            types.get(entityTypeID).put(entityInstanceID, entity);
            result = new Unpacked(types,
                    tick,
                    (InetSocketAddress) channelHandlerContext.channel().remoteAddress(),
                    (InetSocketAddress) channelHandlerContext.channel().localAddress()
            );
        }

        if (in.readableBytes() > 0) throw new IndexOutOfBoundsException(in.readableBytes() + " bytes left in buffer decoder frame");

        out.add(result);
    }
}
