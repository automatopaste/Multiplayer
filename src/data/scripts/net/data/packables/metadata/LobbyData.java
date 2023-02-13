package data.scripts.net.data.packables.metadata;

import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.ShortRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.collections.SyncingListRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.server.PlayerLobby;
import data.scripts.net.data.tables.server.PlayerShips;
import data.scripts.plugins.MPPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends information about lobby data to clients
 */
public class LobbyData extends EntityData {

    public static byte TYPE_ID;

    private List<Short> players;
    private List<String> playerShipIDs;

    public LobbyData(short instanceID, final PlayerLobby playerLobby, final PlayerShips playerShips) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                new SyncingListRecord<>(new ArrayList<Short>(), ShortRecord.TYPE_ID).setDebugText("player ship instances"),
                new SourceExecute<List<Short>>() {
                    @Override
                    public List<Short> get() {
                        List<Short> out = new ArrayList<>();

                        // server ship
                        out.add((short) -1);

                        out.addAll(playerLobby.getPlayers().keySet());

                        return out;

                    }
                },
                new DestExecute<List<Short>>() {
                    @Override
                    public void execute(List<Short> value, EntityData packable) {
                        LobbyData lobbyData = (LobbyData) packable;
                        lobbyData.setPlayers(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new SyncingListRecord<>(new ArrayList<String>(), StringRecord.TYPE_ID).setDebugText("player piloted ship ids"),
                new SourceExecute<List<String>>() {
                    @Override
                    public List<String> get() {
                        List<String> out = new ArrayList<>();

                        // server ship
                        out.add(playerShips.getHostShipID());

                        for (PlayerShipData playerShipData : playerShips.getPlayerShips().values()) {
                            out.add(playerShipData.getPlayerShipID());
                        }

                        return out;
                    }
                },
                new DestExecute<List<String>>() {
                    @Override
                    public void execute(List<String> value, EntityData packable) {
                        LobbyData lobbyData = (LobbyData) packable;
                        lobbyData.setPlayerShipIDs(value);
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount, BaseEntityManager manager) {

    }

    @Override
    public void delete() {

    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    public List<Short> getPlayers() {
        return players;
    }

    public void setPlayers(List<Short> players) {
        this.players = players;
    }

    public List<String> getPlayerShipIDs() {
        return playerShipIDs;
    }

    public void setPlayerShipIDs(List<String> playerShipIDs) {
        this.playerShipIDs = playerShipIDs;
    }
}
