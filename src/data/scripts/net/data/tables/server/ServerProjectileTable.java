package data.scripts.net.data.tables.server;

import data.scripts.net.data.SourcePackable;
import data.scripts.net.data.tables.OutboundEntityManager;

import java.util.Map;

public class ServerProjectileTable implements OutboundEntityManager {
    @Override
    public Map<Integer, SourcePackable> getOutbound() {
        return null;
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void register() {

    }

    @Override
    public PacketType getPacketType() {
        return PacketType.DATAGRAM;
    }
}
