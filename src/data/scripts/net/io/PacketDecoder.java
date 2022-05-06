package data.scripts.net.io;

import data.scripts.net.data.IDTypes;
import data.scripts.net.data.records.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.ArrayList;
import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }

//        List<List<ARecord>> entities = new ArrayList<>();
//        Unpacked unpacked = new Unpacked(entities);

//        int tick = in.readInt();
//        unpacked.setTick(tick);

//        int numEntities = in.readInt();
//
//        if (numEntities != 0) {
//            for (int i = 0; i < numEntities; i++) {
//                entities.add(unpackRecords(in));
//            }
//        }

        int tick = in.readInt();

        List<List<ARecord>> e = new ArrayList<>();
        List<ARecord> a = new ArrayList<>();

        while (in.readableBytes() > 0) {
            int type = in.readInt();

            switch(type) {
                case IDTypes.FLOAT_RECORD:
                    a.add(FloatRecord.read(in));
                    break;
                case IDTypes.V2F_RECORD:
                    a.add(Vector2fRecord.read(in));
                    break;
                case IDTypes.INT_RECORD:
                    a.add(IntRecord.read(in));
                    break;
                case IDTypes.STRING_RECORD:
                    a.add(StringRecord.read(in));
                    break;

                case IDTypes.SHIP:
                case IDTypes.INPUT_AGGREGATE:
                case IDTypes.SIMPLE_ENTITY:
                    e.add(a);
                    a = new ArrayList<>();
                    break;
            }
        }
        e.add(a);

        Unpacked unpacked = new Unpacked(e, tick);

        int readable = in.readableBytes();
        if (readable > 0) {
            throw new OutOfMemoryError(in.readableBytes() + " bytes left in packet decoder frame");
        }

        out.add(unpacked);
    }

//    private List<ARecord> unpackRecords(ByteBuf in) {
//        List<ARecord> out = new ArrayList<>();
//
//        //iterate until new entity encountered
//        outer:
//        while (in.readableBytes() > 0) {
//            // mark index so it can be reset if new entity is encountered
//            in.markReaderIndex();
//
//            int type = in.readInt();
//
//            switch(type) {
//                case IDTypes.FLOAT_RECORD:
//                    out.add(FloatRecord.read(in));
//                    break;
//                case IDTypes.V2F_RECORD:
//                    out.add(Vector2fRecord.read(in));
//                    break;
//                case IDTypes.INT_RECORD:
//                    out.add(IntRecord.read(in));
//                    break;
//                case IDTypes.STRING_RECORD:
//                    out.add(StringRecord.read(in));
//                    break;
//
//                case IDTypes.SHIP:
//                    //reset index if encountering a new entity
//                    in.resetReaderIndex();
//                    break outer;
//            }
//        }
//
//        return out;
//    }
}
