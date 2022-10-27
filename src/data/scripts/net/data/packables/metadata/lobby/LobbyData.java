package data.scripts.net.data.packables.metadata.lobby;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.metadata.playership.PlayerShipDest;
import data.scripts.net.data.packables.metadata.playership.PlayerShipIDs;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.ListRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.data.tables.server.PlayerMap;
import data.scripts.net.data.tables.server.PlayerShipMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends information about lobby data to clients
 */
public class LobbyData extends BasePackable {
    public LobbyData(int instanceID, final PlayerMap playerMap, final PlayerShipMap playerShipMap) {
        super(instanceID);

        putRecord(new ListRecord<>(new BaseRecord.DeltaFunc<List<IntRecord>>() {
            @Override
            public List<IntRecord> get() {
                List<IntRecord> out = new ArrayList<>();

                // server ship
                out.add(new IntRecord(-1, -1));

                for (Integer connectionID : playerMap.getPlayers().keySet()) {
                    out.add(new IntRecord(connectionID, -1));
                }

                return out;
            }
        }, LobbyIDs.PLAYER_CONNECTION_IDS, IntRecord.TYPE_ID));
        putRecord(new ListRecord<>(new BaseRecord.DeltaFunc<List<StringRecord>>() {
            @Override
            public List<StringRecord> get() {
                List<StringRecord> out = new ArrayList<>();

                // server ship
                out.add(new StringRecord(playerShipMap.getHostShipID(), -1));

                for (PlayerShipDest playerShip : playerShipMap.getPlayerShips().values()) {
                    out.add(new StringRecord((String) playerShip.getRecord(PlayerShipIDs.CLIENT_ACTIVE_SHIP_ID).getValue(), -1));
                }

                return out;
            }
        }, LobbyIDs.PLAYER_SHIP_IDS, StringRecord.TYPE_ID));
    }

    @Override
    public int getTypeID() {
        return LobbyIDs.TYPE_ID;
    }
}
