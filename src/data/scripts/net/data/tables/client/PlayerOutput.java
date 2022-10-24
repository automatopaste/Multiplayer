package data.scripts.net.data.tables.client;

import data.scripts.net.data.SourcePackable;
import data.scripts.net.data.packables.metadata.player.PlayerIDs;
import data.scripts.net.data.packables.metadata.player.PlayerSource;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;

import java.util.HashMap;
import java.util.Map;

public class PlayerOutput implements OutboundEntityManager {
    private final PlayerSource command;
    private final int instanceID;

    public PlayerOutput(int instanceID, PlayerSource command) {
        this.command = command;
        this.instanceID = instanceID;
    }

    @Override
    public Map<Integer, SourcePackable> getOutbound() {
        Map<Integer, SourcePackable> out = new HashMap<>();
        out.put(instanceID, command);
        return out;
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(PlayerIDs.TYPE_ID, this);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.DATAGRAM;
    }
}
