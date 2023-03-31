package data.scripts.net.data.tables.client;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.packables.metadata.PlayerShipData;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerShip implements OutboundEntityManager {

    private final PlayerShipData playerShipData;
    private final short instanceID;

    private String playerShipID;
    private String playerShipIDPrev;
    private ShipAPI playerShip;
    private short activeShipID;
    private short requestedShipID;

    public PlayerShip(short instanceID) {
        this.instanceID = instanceID;

        playerShipData = new PlayerShipData(instanceID, this);
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        playerShipData.update(amount, this, plugin);

        if (playerShipID != null && playerShipIDPrev == null || playerShipID != null && !playerShipIDPrev.equals(playerShipID)) {
            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                if (ship.getFleetMemberId().equals(playerShipID)) {
                    Global.getCombatEngine().setPlayerShipExternal(ship);
                    playerShip = ship;

                    ClientShipTable clientShipTable = (ClientShipTable) plugin.getEntityManagers().get(ClientShipTable.class);
                    for (ShipData shipData : clientShipTable.getShips().values()) {
                        if (shipData.getShip().equals(playerShip)) {
                            activeShipID = shipData.getInstanceID();
                        }
                    }

                    break;
                }
            }
        }

        playerShipIDPrev = playerShipID;
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(PlayerShipData.TYPE_ID, this);
    }

    @Override
    public Map<Short, InstanceData> getOutbound(byte typeID, byte connectionID, float amount) {
        Map<Short, InstanceData> out = new HashMap<>();

        InstanceData instanceData = playerShipData.sourceExecute(amount);
        if (instanceData.records != null && !instanceData.records.isEmpty()) {
            out.put(instanceID, instanceData);
        }

        return out;
    }

    @Override
    public Set<Short> getDeleted(byte typeID, byte connectionID) {
        return null;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.DATAGRAM;
    }

    public String getPlayerShipID() {
        return playerShipID;
    }

    public short getActiveShipID() {
        return activeShipID;
    }

    public short getRequestedShipID() {
        return requestedShipID;
    }

    public ShipAPI getPlayerShip() {
        return playerShip;
    }

    public void setPlayerShipID(String playerShipID) {
        this.playerShipID = playerShipID;
    }
}
