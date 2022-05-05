package data.scripts.net.client;

import data.scripts.net.data.IDTypes;
import data.scripts.net.data.packables.ShipData;
import data.scripts.net.data.records.ARecord;
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

            switch (type) {
                case IDTypes.SHIP:
                    unpacked.add(ShipData.unpack(in));
                    break;
            }
        }
    }
}
