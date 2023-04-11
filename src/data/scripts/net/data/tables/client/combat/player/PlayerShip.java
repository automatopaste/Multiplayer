package data.scripts.net.data.tables.client.combat.player;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.packables.metadata.ClientPlayerData;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.tables.client.combat.entities.ClientShipTable;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerShip implements OutboundEntityManager {

    private final ClientPlayerData clientPlayerData;
    private final ClientShipTable clientShipTable;
    private final short instanceID;

    private String playerShipID;
    private String playerShipIDPrev;
    private ShipAPI playerShip;
    private short activeShipID;
    private short requestedShipID;

    public PlayerShip(short instanceID, ClientShipTable clientShipTable) {
        this.instanceID = instanceID;

        clientPlayerData = new ClientPlayerData(instanceID, this);
        this.clientShipTable = clientShipTable;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        clientPlayerData.update(amount, this, plugin);

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

    public void requestTransfer(ShipAPI dest) {
        Short id = clientShipTable.getShipIDs().get(dest);
        if (id != null) {
            clientPlayerData.setRequestedShipID(id);
        }
    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(ClientPlayerData.TYPE_ID, this);
    }

    @Override
    public Map<Short, InstanceData> getOutbound(byte typeID, byte connectionID, float amount) {
        Map<Short, InstanceData> out = new HashMap<>();

        InstanceData instanceData = clientPlayerData.sourceExecute(amount);
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

//    public PlayerShips.Controller getController(ShipAPI ship) {
//        Short id = .getRegistered().get(ship);
//        if (id == null) return PlayerShips.Controller.NULL;
//        else if (id == hostActiveShipID) return PlayerShips.Controller.HOST;
//        else if (playerShips.get(id) != null) return PlayerShips.Controller.CLIENT;
//        return PlayerShips.Controller.AI_CONTROL;
//    }

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
