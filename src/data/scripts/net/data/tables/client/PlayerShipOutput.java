package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.metadata.playership.PlayerShipData;
import data.scripts.net.data.packables.metadata.playership.PlayerShipIDs;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;

import java.util.HashMap;
import java.util.Map;

public class PlayerShipOutput implements OutboundEntityManager {

    private final PlayerShipData playerShip;
    private final short instanceID;

    public PlayerShipOutput(short instanceID) {
        this.instanceID = instanceID;

        playerShip = new PlayerShipData(instanceID, new BaseRecord.DeltaFunc<String>() {
            @Override
            public String get() {
                return Global.getCombatEngine().getPlayerShip().getFleetMemberId();
            }
        });
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(PlayerShipIDs.TYPE_ID, this);
    }

    @Override
    public Map<Short, BasePackable> getOutbound() {
        Map<Short, BasePackable> out = new HashMap<>();
        out.put(instanceID, playerShip);
        return out;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }
}
