package data.scripts.net.data.tables.client;

import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.packables.metadata.PlayerShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerShipOutput implements OutboundEntityManager {

    private final PlayerShipData playerShipData;
    private final short instanceID;

    public PlayerShipOutput(short instanceID, final String playerShipID) {
        this.instanceID = instanceID;

        playerShipData = new PlayerShipData(instanceID, new SourceExecute<String>() {
            @Override
            public String get() {
                return playerShipID;
            }
        });
    }

    @Override
    public void update(float amount, MPPlugin plugin) {

    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(PlayerShipData.TYPE_ID, this);
    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound() {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();

        playerShipData.sourceExecute();
        Map<Byte, BaseRecord<?>> deltas = playerShipData.getDeltas();
        if (deltas != null && !deltas.isEmpty()) {
            out.put(instanceID, deltas);
        }

        return out;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }
}
