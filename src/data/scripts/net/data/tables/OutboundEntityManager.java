package data.scripts.net.data.tables;

import data.scripts.net.data.InstanceData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OutboundEntityManager extends BaseEntityManager {

    enum PacketType {
        SOCKET,
        DATAGRAM
    }

    Map<Byte, Map<Short, InstanceData>> getOutbound(byte typeID, float amount, List<Byte> connectionIDs);

    Set<Short> getDeleted(byte typeID, byte connectionID);

    PacketType getOutboundPacketType();
}
