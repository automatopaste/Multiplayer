package data.scripts.net.data.tables.client.combat.connection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.metadata.LobbyData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class LobbyInput implements InboundEntityManager {

    private final byte clientID;
    private LobbyData lobby;

    private String clientPilotedShipID = null;

    private final Map<Byte, String> usernames = new HashMap<>();
    private final Map<Byte, Short> pilotedShipIDs = new HashMap<>();
    private Short localPilotedShipID;

    public LobbyInput(byte clientID) {
        this.clientID = clientID;
        lobby = null;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {
        if (lobby != null) {
            lobby.update(amount, this, plugin);

            usernames.putAll(lobby.getPlayerUsernames());
            pilotedShipIDs.putAll(lobby.getPlayerShipIDs());

            Short pilotedShipID = pilotedShipIDs.get(clientID);
            if (pilotedShipID != localPilotedShipID) {
                ShipAPI ship = ((MPClientPlugin) plugin).getShipTable().getShips().get(pilotedShipID).getShip();
                if (ship != null) {
                    Global.getCombatEngine().setPlayerShipExternal(ship);
                }
            }

            localPilotedShipID = pilotedShipID;
        }
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(LobbyData.TYPE_ID, this);
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        if (lobby == null) {
            lobby = new LobbyData(instanceID, null, null);

            lobby.destExecute(toProcess, tick);

            lobby.init(plugin, this);
        } else {
            lobby.destExecute(toProcess, tick);
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {
        LobbyData data = lobby;

        if (data != null) {
            data.delete();
            lobby = null;
        }
    }

    public LobbyData getLobby() {
        return lobby;
    }

    public Map<Byte, String> getUsernames() {
        return usernames;
    }

    public Map<Byte, Short> getPilotedShipIDs() {
        return pilotedShipIDs;
    }
}
