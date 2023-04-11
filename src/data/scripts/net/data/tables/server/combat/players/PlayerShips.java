package data.scripts.net.data.tables.server.combat.players;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.packables.metadata.ClientPlayerData;
import data.scripts.net.data.packables.metadata.ServerPlayerData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.tables.server.combat.entities.ShipTable;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerShips implements InboundEntityManager, OutboundEntityManager {

    public enum Controller {
        HOST,
        CLIENT,
        AI_CONTROL,
        NULL
    }

    // map client connection id to data object
    private final Map<Short, ClientPlayerData> clientPlayerData = new HashMap<>();
    private final Map<Short, ServerPlayerData> serverPlayerData = new HashMap<>();

    private final ShipTable shipTable;
    private short hostActiveShipID = DEFAULT_HOST_INSTANCE;

    public PlayerShips(ShipTable shipTable) {
        this.shipTable = shipTable;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine.getPlayerShip() == null || engine.getPlayerShip().isShuttlePod()) {
            hostActiveShipID = DEFAULT_HOST_INSTANCE;
        } else {
            try {
                hostActiveShipID = shipTable.getRegistered().get(engine.getPlayerShip());
            } catch (NullPointerException n) {
                Global.getLogger(PlayerShips.class).error("unable to find id for host ship in local table");
            }
        }

        for (short id : clientPlayerData.keySet()) {
            ClientPlayerData c = clientPlayerData.get(id);
            c.update(amount, this, plugin);
        }

        for (short id : clientPlayerData.keySet()) {
            ClientPlayerData c = clientPlayerData.get(id);
            short requested = c.getRequestedShipID();

            if (requested != -1) { // remote client is submitting an id to switch to
                ShipAPI dest = shipTable.getTable()[requested].getShip();

                transferControl(dest, false, c);
            }
        }

        for (short id : serverPlayerData.keySet()) {
            ServerPlayerData s = serverPlayerData.get(id);
            s.setHostID(hostActiveShipID);
        }
    }

    public void transferControl(ShipAPI dest, boolean host, ClientPlayerData playerData) {
        Short destID = shipTable.getRegistered().get(dest);
        if (destID == null) {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();
        if (host) {
            if (destID == hostActiveShipID) {
                return;
            }

            if (playerData == null) { // ship control is vacant
                engine.setPlayerShipExternal(dest);
            } else { // ship is controlled by a player
                // todo add config option to let host override player control
            }
        } else {
            if (destID == hostActiveShipID) {

            } else {
                playerData.transferPlayerShip(dest);
                serverPlayerData.get(playerData.getInstanceID()).setActiveID(destID);
            }
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ClientPlayerData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(ServerPlayerData.TYPE_ID, this);
    }

    public Map<Short, ClientPlayerData> getClientPlayerData() {
        return clientPlayerData;
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        ClientPlayerData data = clientPlayerData.get(instanceID);

        if (data == null) {
            data = new ClientPlayerData(instanceID, null);

            clientPlayerData.put(instanceID, data);

            data.destExecute(toProcess, tick);
            data.init(plugin, this);
        } else {
            data.destExecute(toProcess, tick);
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {
        ClientPlayerData data = clientPlayerData.get(instanceID);

        if (data != null) {
            data.delete();

            clientPlayerData.remove(instanceID);
        }
    }


    @Override
    public Map<Short, InstanceData> getOutbound(byte typeID, byte connectionID, float amount) {
        Map<Short, InstanceData> out = new HashMap<>();

        for (ServerPlayerData data : serverPlayerData.values()) {
            InstanceData instanceData = data.sourceExecute(amount);

            if (instanceData.records != null && !instanceData.records.isEmpty()) {
                out.put(data.getInstanceID(), instanceData);
            }
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

    public Short getHostShipID() {
        return hostActiveShipID;
    }

    public Controller getController(ShipAPI ship) {
        Short id = shipTable.getRegistered().get(ship);
        if (id == null) return Controller.NULL;
        else if (id == hostActiveShipID) return Controller.HOST;
        else if (clientPlayerData.get(id) != null) return Controller.CLIENT;
        return Controller.AI_CONTROL;
    }
}
