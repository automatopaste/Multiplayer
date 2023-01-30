package data.scripts.net.data.packables.metadata;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.ShortRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.records.collections.ListRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.server.PlayerMap;
import data.scripts.net.data.tables.server.PlayerShipMap;
import data.scripts.plugins.MPPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends information about lobby data to clients
 */
public class LobbyData extends BasePackable {

    public static byte TYPE_ID;

    private short[] players;
    private List<String> playerShipIDs;

    public LobbyData(short instanceID, final PlayerMap playerMap, final PlayerShipMap playerShipMap) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                new ListRecord<>(new ArrayList<Short>(), ShortRecord.TYPE_ID),
                new SourceExecute<List<Short>>() {
                    @Override
                    public List<Short> get() {
                        List<Short> out = new ArrayList<>();

                        // server ship
                        out.add((short) -1);

                        out.addAll(playerMap.getPlayers().keySet());

                        return out;
                    }
                },
                new DestExecute<List<Short>>() {
                    @Override
                    public void execute(BaseRecord<List<Short>> record, BasePackable packable) {
                        LobbyData lobbyData = (LobbyData) packable;
                        lobbyData.setPlayers(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListRecord<>(new ArrayList<String>(), StringRecord.TYPE_ID),
                new SourceExecute<List<String>>() {
                    @Override
                    public List<String> get() {
                        List<String> out = new ArrayList<>();

                        // server ship
                        out.add(playerShipMap.getHostShipID());

                        for (PlayerShipData playerShipData : playerShipMap.getPlayerShips().values()) {
                            out.add(playerShipData.getPlayerShipID());
                        }

                        return out;
                    }
                },
                new DestExecute<List<String>>() {
                    @Override
                    public void execute(BaseRecord<List<String>> record, BasePackable packable) {
                        LobbyData lobbyData = (LobbyData) packable;
                        lobbyData.setPlayerShipIDs(record.getValue());
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void delete() {

    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    public short[] getPlayers() {
        return players;
    }

    public void setPlayers(List<Short> players) {
        short[] p = new short[players.size()];
        for (short s = 0; s < players.size(); s++) p[s] = players.get(s);
        this.players = p;
    }

    public List<String> getPlayerShipIDs() {
        return playerShipIDs;
    }

    public void setPlayerShipIDs(List<String> playerShipIDs) {
        this.playerShipIDs = playerShipIDs;
    }
}
