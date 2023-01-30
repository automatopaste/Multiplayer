package data.scripts.net.data.tables;

import data.scripts.net.data.records.BaseRecord;

import java.util.Map;

public interface OutboundEntityManager extends BaseEntityManager {

    enum PacketType {
        SOCKET,
        DATAGRAM
    }

    Map<Short, Map<Byte, BaseRecord<?>>> getOutbound(byte typeID);

    PacketType getOutboundPacketType();
}
