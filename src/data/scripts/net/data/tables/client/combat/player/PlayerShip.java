package data.scripts.net.data.tables.client.combat.player;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.packables.metadata.ClientPlayerData;
import data.scripts.net.data.packables.metadata.ServerPlayerData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.tables.client.combat.entities.ClientShipTable;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerShip implements InboundEntityManager, OutboundEntityManager {

    private final ClientPlayerData clientPlayerData;
    private final ServerPlayerData serverPlayerData;
    private short prevActiveID;
    private final ClientShipTable clientShipTable;
    private final short instanceID;

    public PlayerShip(short instanceID, ClientShipTable clientShipTable) {
        this.instanceID = instanceID;

        clientPlayerData = new ClientPlayerData(instanceID, this);
        serverPlayerData = new ServerPlayerData(instanceID);

        this.clientShipTable = clientShipTable;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        clientPlayerData.update(amount, this, plugin);
        serverPlayerData.update(amount, this, plugin);

        if (serverPlayerData.getActiveID() != prevActiveID) {
            ShipData data = clientShipTable.getShips().get(serverPlayerData.getActiveID());

            if (data != null) {
                Global.getCombatEngine().setPlayerShipExternal(data.getShip());
            }
        }

        prevActiveID = serverPlayerData.getActiveID();
    }

    public void requestTransfer(ShipAPI dest) {
        Short id = clientShipTable.getShipIDs().get(dest);
        if (id != null) {
            clientPlayerData.setRequestedShipID(id);
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ServerPlayerData.TYPE_ID, this);
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
        return PacketType.SOCKET;
    }

    public short getActiveShipID() {
        return serverPlayerData.getActiveID();
    }

    public ShipAPI getActiveShip() {
        if (serverPlayerData.getActiveID() == -1) return null;
        return clientShipTable.getShips().get(serverPlayerData.getActiveID()).getShip();
    }

    public ShipAPI getHostShip() {
        if (serverPlayerData.getHostID() == -1) return null;
        return clientShipTable.getShips().get(serverPlayerData.getHostID()).getShip();
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        serverPlayerData.destExecute(toProcess, tick);
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {

    }
}
