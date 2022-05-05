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
        List<List<ARecord>> entities = new ArrayList<>();

        int numEntities = in.readInt();

        if (numEntities == 0) {
            out.add(new Unpacked(new ArrayList<List<ARecord>>()));
        } else {
            for (int i = 0; i < numEntities; i++) {
                entities.add(unpackRecords(in));
            }

            Unpacked unpacked = new Unpacked(entities);
            out.add(unpacked);
        }
    }

    private List<ARecord> unpackRecords(ByteBuf in) {
        List<ARecord> out = new ArrayList<>();

        //iterate until new entity encountered
        outer:
        while (true) {
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
