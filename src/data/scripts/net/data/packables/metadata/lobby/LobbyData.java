package data.scripts.net.data.packables.metadata.lobby;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.metadata.playership.PlayerShipDest;
import data.scripts.net.data.packables.metadata.playership.PlayerShipIDs;
import data.scripts.net.data.records.*;
import data.scripts.net.data.tables.server.PlayerMap;
import data.scripts.net.data.tables.server.PlayerShipMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends information about lobby data to clients
 */
public class LobbyData extends BasePackable {
    public LobbyData(short instanceID, final PlayerMap playerMap, final PlayerShipMap playerShipMap) {
        super(instanceID);

        putRecord(new ListRecord<>(new BaseRecord.DeltaFunc<List<ShortRecord>>() {
            @Override
            public List<ShortRecord> get() {
                List<ShortRecord> out = new ArrayList<>();

                // server ship
                out.add(new ShortRecord((short) -1, (byte) -1));

                for (Short connectionID : playerMap.getPlayers().keySet()) {
                    out.add(new ShortRecord(connectionID, (byte) -1));
                }

                return out;
            }
        }, LobbyIDs.PLAYER_CONNECTION_IDS, IntRecord.TYPE_ID));
        putRecord(new ListRecord<>(new BaseRecord.DeltaFunc<List<StringRecord>>() {
            @Override
            public List<StringRecord> get() {
                List<StringRecord> out = new ArrayList<>();

                // server ship
                out.add(new StringRecord(playerShipMap.getHostShipID(), (byte) -1));

                for (PlayerShipDest playerShip : playerShipMap.getPlayerShips().values()) {
                    out.add(new StringRecord((String) playerShip.getRecord(PlayerShipIDs.CLIENT_ACTIVE_SHIP_ID).getValue(), (byte) -1));
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
