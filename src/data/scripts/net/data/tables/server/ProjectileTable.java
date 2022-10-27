package data.scripts.net.data.tables.server;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.tables.EntityTable;
import data.scripts.net.data.tables.OutboundEntityManager;

import java.util.Map;

public class ProjectileTable extends EntityTable implements OutboundEntityManager {

    @Override
    public void update(float amount) {

    }

    @Override
    public void register() {

    }

    @Override
    protected int getSize() {
        return 0;
    }

    @Override
    public Map<Integer, BasePackable> getOutbound(int entityID) {
        return null;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return null;
    }
}
