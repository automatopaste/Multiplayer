package data.scripts.net.client;

import data.scripts.net.data.IDTypes;
import data.scripts.net.data.records.*;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ServerPacketReconstructor {
    private final List<List<ARecord>> unpacked;

    public ServerPacketReconstructor() {
        unpacked = new ArrayList<>();
    }

    public List<List<ARecord>> getUnpacked() {
        return unpacked;
    }

    public void unpack(ByteBuf in, int length) {
        while (in.readerIndex() < length) {
            int type = in.readInt();


            unpacked.add(unpackRecords(in, length));

//            switch (type) {
//                case IDTypes.SHIP:
//                    break;
//            }
        }
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
