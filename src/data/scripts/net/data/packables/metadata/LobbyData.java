package data.scripts.net.data.packables.metadata;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.ListRecord;
import data.scripts.net.data.records.ShortRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.tables.server.PlayerMap;
import data.scripts.net.data.tables.server.PlayerShipMap;
import data.scripts.plugins.MPPlugin;

import java.util.ArrayList;

/**
 * Sends information about lobby data to clients
 */
public class LobbyData extends BasePackable {

    public static byte TYPE_ID;

    private java.util.List<Short> players;
    private java.util.List<java.lang.String> playerShipIDs;

    public LobbyData(short instanceID, final PlayerMap playerMap, final PlayerShipMap playerShipMap) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                new ListRecord<>(new ArrayList<Short>(), ShortRecord.TYPE_ID),
                new SourceExecute<java.util.List<Short>>() {
                    @Override
                    public java.util.List<Short> get() {
                        java.util.List<Short> out = new ArrayList<>();

                        // server ship
                        out.add((short) -1);

                        out.addAll(playerMap.getPlayers().keySet());

                        return out;
                    }
                },
                new DestExecute<java.util.List<Short>>() {
                    @Override
                    public void execute(BaseRecord<java.util.List<Short>> record, BasePackable packable) {
                        LobbyData lobbyData = (LobbyData) packable;
                        lobbyData.setPlayers(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                new ListRecord<>(new ArrayList<java.lang.String>(), StringRecord.TYPE_ID),
                new SourceExecute<java.util.List<java.lang.String>>() {
                    @Override
                    public java.util.List<java.lang.String> get() {
                        java.util.List<java.lang.String> out = new ArrayList<>();

                        // server ship
                        out.add(playerShipMap.getHostShipID());

                        for (PlayerShipData playerShipData : playerShipMap.getPlayerShips().values()) {
                            out.add(playerShipData.getPlayerShipID());
                        }

                        return out;
                    }
                },
                new DestExecute<java.util.List<java.lang.String>>() {
                    @Override
                    public void execute(BaseRecord<java.util.List<java.lang.String>> record, BasePackable packable) {
                        LobbyData lobbyData = (LobbyData) packable;
                        lobbyData.setPlayerShipIDs(record.getValue());
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin) {

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

    public java.util.List<Short> getPlayers() {
        return players;
    }

    public void setPlayers(java.util.List<Short> players) {
        this.players = players;
    }

    public java.util.List<java.lang.String> getPlayerShipIDs() {
        return playerShipIDs;
    }

    public void setPlayerShipIDs(java.util.List<java.lang.String> playerShipIDs) {
        this.playerShipIDs = playerShipIDs;
    }
}
