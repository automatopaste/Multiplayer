package data.scripts.net.data.packables.metadata.lobby;

import data.scripts.net.data.packables.DestPackable;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.ListRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class LobbyDest extends DestPackable {

    /**
     * Destination constructor
     *
     * @param instanceID unique
     * @param records    incoming deltas
     */
    public LobbyDest(int instanceID, Map<Integer, BaseRecord<?>> records) {
        super(instanceID, records);
    }

    @Override
    public int getTypeID() {
        return 0;
    }

    @Override
    protected void initDefaultRecords() {
        putRecord(ListRecord.getDefault(LobbyIDs.PLAYER_CONNECTION_IDS, StringRecord.TYPE_ID));
        putRecord(ListRecord.getDefault(LobbyIDs.PLAYER_SHIP_IDS, StringRecord.TYPE_ID));
    }

    @Override
    public void update(float amount) {

    }

    @Override
    public void init(MPPlugin plugin) {

    }

    @Override
    public void delete() {

    }

    public static LobbyDest getDefault() {
        return new LobbyDest(-1, new HashMap<Integer, BaseRecord<?>>());
    }
}
