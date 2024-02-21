package data.scripts.net.data.tables.server.combat.players;

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
import data.scripts.net.data.tables.server.combat.entities.ships.ShipTable;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultShipAIPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerShips implements InboundEntityManager, OutboundEntityManager {

    public static final short NULL_SHIP_ID = Short.MAX_VALUE;

    public enum Controller {
        HOST,
        CLIENT,
        AI_CONTROL,
        NULL
    }

    // map client connection id to data object
    private final Map<Short, ClientPlayerData> controlData = new HashMap<>();
    private final Map<Short, ServerPlayerData> serverPlayerData = new HashMap<>();

    private final ShipTable shipTable;
    private short hostActiveShipID = NULL_SHIP_ID;

    public PlayerShips(ShipTable shipTable) {
        this.shipTable = shipTable;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        CombatEngineAPI engine = Global.getCombatEngine();

        short prevHostActiveShipID = hostActiveShipID;
        if (engine.getPlayerShip() == null || engine.getPlayerShip().isShuttlePod() || shipTable.getRegistered().isEmpty()) {
            hostActiveShipID = NULL_SHIP_ID;
        } else {
            hostActiveShipID = shipTable.getRegistered().get(engine.getPlayerShip());
        }
        for (short id : serverPlayerData.keySet()) {
            ServerPlayerData s = serverPlayerData.get(id);
            s.setHostID(hostActiveShipID);
        }

        for (short id : controlData.keySet()) {
            ClientPlayerData c = controlData.get(id);

            c.update(amount, this, plugin);

            short requested = c.getRequestedShipID();

            if (requested != NULL_SHIP_ID && requested != c.getPlayerShipID()) { // remote client is submitting an id to switch to
                if (requested < shipTable.getShipTable().limit) {
                    ShipData shipData = shipTable.getShipTable().array()[requested];

                    if (shipData != null) {
                        ShipAPI dest = shipData.getShip();
                        transferControl(dest, false, c);
                    }
                } else {
                    Global.getLogger(PlayerShips.class).error("client requested id out of range of local table");
                }
            }
        }
    }

    public void transferControl(ShipAPI dest, boolean host, ClientPlayerData playerData) {
        Short destID = shipTable.getRegistered().get(dest);
        if (destID == null) {
            destID = shipTable.createEntry(dest);
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
                // do nothing
            } else {
                ShipAPI prev = playerData.getShip();
                if (prev != null) {
                    prev.resetDefaultAI();
                    prev.getShipAI().forceCircumstanceEvaluation();
                }

                playerData.setShip(dest);

                serverPlayerData.get(playerData.getInstanceID()).setActiveID(destID);

                dest.setShipAI(new MPDefaultShipAIPlugin());
            }
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ClientPlayerData.TYPE_ID, this);
        DataGenManager.registerOutboundEntityManager(ServerPlayerData.TYPE_ID, this);
    }

    public Map<Short, ClientPlayerData> getControlData() {
        return controlData;
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        ClientPlayerData data = controlData.get(instanceID);

        if (data == null) {
            data = new ClientPlayerData(instanceID, null);

            controlData.put(instanceID, data);
            ServerPlayerData s = new ServerPlayerData(instanceID);
            serverPlayerData.put(instanceID, s);
            s.destExecute(new HashMap<Byte, Object>(), tick);
            s.init(plugin, this);

            data.destExecute(toProcess, tick);
            data.init(plugin, this);
        } else {
            data.destExecute(toProcess, tick);
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {
        ClientPlayerData data = controlData.get(instanceID);

        if (data != null) {
            data.delete();

            controlData.remove(instanceID);
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

    public Map<Short, ServerPlayerData> getServerPlayerData() {
        return serverPlayerData;
    }

    public ShipTable getShipTable() {
        return shipTable;
    }
}
