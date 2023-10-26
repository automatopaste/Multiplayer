package data.scripts.net.data.tables.client.combat.player;

import cmu.CMUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.entities.ships.ClientPlayerData;
import data.scripts.net.data.packables.entities.ships.ShipData;
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
    private final ClientShipTable clientShipTable;
    private final short instanceID;

    private boolean requestingTransfer = false;
    private short requestedID = -1;

    public PlayerShip(short instanceID, ClientShipTable clientShipTable) {
        this.instanceID = instanceID;

        clientPlayerData = new ClientPlayerData(instanceID, this);
        serverPlayerData = new ServerPlayerData(instanceID);

        this.clientShipTable = clientShipTable;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        CombatEngineAPI engine = Global.getCombatEngine();

        clientPlayerData.update(amount, this, plugin);
        serverPlayerData.update(amount, this, plugin);

        if (requestingTransfer) {
            if (serverPlayerData.getActiveID() == requestedID) {
                requestingTransfer = false;
                requestedID = -1;

                ShipData data = clientShipTable.getShipTable().array()[serverPlayerData.getActiveID()];
                if (data != null) {
                    engine.setPlayerShipExternal(data.getShip());
                }

                clientPlayerData.setRequestedShipID((short)-1);
            }
        }

        boolean update = false;
        short id = -1;
        if (engine.getPlayerShip() != null) {
            Short i = clientShipTable.getShipIDs().get(engine.getPlayerShip());
            if (i != null) {
                id = i;
            }

            update = true;
        }

        CMUtils.getGuiDebug().putText(PlayerShip.class, "SERVER ACTIVE", "SERVER ACTIVE: " + serverPlayerData.getActiveID());
        CMUtils.getGuiDebug().putText(PlayerShip.class, "ACTUAL ACTIVE", "ACTUAL ACTIVE: " + id);

        if (update && serverPlayerData.getActiveID() != -1) {
            ShipData data = clientShipTable.getShipTable().array()[serverPlayerData.getActiveID()];
            if (data != null && data.getShip() != null) {
                Global.getCombatEngine().setPlayerShipExternal(data.getShip());
            }
        }
    }

    public void requestTransfer(ShipAPI dest) {
        Short id = clientShipTable.getShipIDs().get(dest);
        if (id != null) {
            clientPlayerData.setRequestedShipID(id);

            requestingTransfer = true;
            requestedID = id;
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
        return clientShipTable.getShipTable().array()[serverPlayerData.getActiveID()].getShip();
    }

    public ShipAPI getHostShip() {
        if (serverPlayerData.getHostID() == -1) return null;
        ShipData shipData = clientShipTable.getShipTable().array()[serverPlayerData.getHostID()];
        if (shipData != null) {
            return shipData.getShip();
        }
        return null;
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        serverPlayerData.destExecute(toProcess, tick);
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {

    }
}
