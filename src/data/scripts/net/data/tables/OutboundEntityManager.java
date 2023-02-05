package data.scripts.net.data.tables;

import data.scripts.net.data.records.DataRecord;

import java.util.Map;
import java.util.Set;

public interface OutboundEntityManager extends BaseEntityManager {

    enum PacketType {
        SOCKET,
        DATAGRAM
    }

    Map<Short, Map<Byte, DataRecord<?>>> getOutbound(byte typeID);

    Set<Short> getDeleted(byte typeID);

    PacketType getOutboundPacketType();
}
