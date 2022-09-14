package data.scripts.net.data.tables.client;

import data.scripts.net.data.SourcePackable;
import data.scripts.net.data.packables.metadata.pilot.PilotIDs;
import data.scripts.net.data.packables.metadata.pilot.PilotSource;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;

import java.util.HashMap;
import java.util.Map;

public class PilotCommandOutput implements OutboundEntityManager {
    private final PilotSource command;
    private final int instanceID;

    public PilotCommandOutput(int instanceID, PilotSource command) {
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
        DataGenManager.registerOutboundEntityManager(PilotIDs.TYPE_ID, this);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.DATAGRAM;
    }
}
