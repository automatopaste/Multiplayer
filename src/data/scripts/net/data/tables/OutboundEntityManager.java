package data.scripts.net.data.tables;

import data.scripts.net.data.SourcePackable;

import java.util.Map;

public interface OutboundEntityManager extends BaseEntityManager {

    enum PacketType {
        SOCKET,
        DATAGRAM
    }

    Map<Integer, SourcePackable> getOutbound();

    PacketType getPacketType();
}
