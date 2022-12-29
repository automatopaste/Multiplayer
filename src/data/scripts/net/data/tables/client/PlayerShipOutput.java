package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.packables.SourceLambda;
import data.scripts.net.data.packables.metadata.playership.PlayerShipData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;

import java.util.HashMap;
import java.util.Map;

public class PlayerShipOutput implements OutboundEntityManager {

    private final PlayerShipData playerShipData;
    private final short instanceID;

    private ShipAPI playerShip;

    public PlayerShipOutput(short instanceID) {
        this.instanceID = instanceID;

        playerShipData = new PlayerShipData(instanceID, new SourceLambda<String>() {
            @Override
            public String get() {
                return playerShipData.getPlayerShipID();
            }
        });
    }

    @Override
    public void execute() {
        playerShipData.execute();
    }

    @Override
    public void update(float amount) {
        playerShip = Global.getCombatEngine().getPlayerShip();
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(PlayerShipData.TYPE_ID, this);
    }

    @Override
    public Map<Short, Map<Byte, BaseRecord<?>>> getOutbound() {
        Map<Short, Map<Byte, BaseRecord<?>>> out = new HashMap<>();
        out.put(instanceID, playerShipData.getDeltas());
        return out;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }

    public ShipAPI getPlayerShip() {
        return playerShip;
    }
}
