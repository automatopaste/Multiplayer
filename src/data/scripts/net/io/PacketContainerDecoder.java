package data.scripts.net.io;

import data.scripts.net.data.IDTypes;
import data.scripts.net.data.records.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.ArrayList;
import java.util.List;

public class PacketContainerDecoder extends ByteToMessageDecoder {
    //private final Map<Integer, List<ARecord>> unpackedEntities;

    public PacketContainerDecoder() {
        //unpackedEntities = new HashMap<>();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) return;

        in.markReaderIndex();

        // wait for all data to arrive
        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        List<List<ARecord>> entities = new ArrayList<>();

        while (in.readerIndex() < length) {
            int type = in.readInt();

            entities.add(unpackRecords(in, length));
        }

        Unpacked unpacked = new Unpacked(entities);

        out.add(unpacked);
    }

    private List<ARecord> unpackRecords(ByteBuf in, int length) {
        List<ARecord> out = new ArrayList<>();

        //iterate until new entity encountered
        outer:
        while (in.readerIndex() < length) {
            // mark index so it can be reset if new entity is encountered
            in.markReaderIndex();

            int type = in.readInt();

            switch(type) {
                case IDTypes.FLOAT_RECORD:
                    out.add(FloatRecord.read(in));
                    break;
                case IDTypes.V2F_RECORD:
                    out.add(Vector2fRecord.read(in));
                    break;
                case IDTypes.INT_RECORD:
                    out.add(IntRecord.read(in));
                    break;
                case IDTypes.STRING_RECORD:
                    out.add(StringRecord.read(in));
                    break;

                case IDTypes.SHIP:
                    //reset index if encountering a new entity
                    in.resetReaderIndex();
                    break outer;
            }
        }

        return out;
    }
}
