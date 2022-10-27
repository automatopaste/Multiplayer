package data.scripts.net.data.packables.metadata.connection;

import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.packables.DestPackable;
import data.scripts.net.data.records.IntRecord;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class ConnectionDest extends DestPackable {

    public ConnectionDest(int connectionID, Map<Integer, BaseRecord<?>> records) {
        super(connectionID, records);
    }

    @Override
    protected void initDefaultRecords() {
        putRecord(IntRecord.getDefault(ConnectionIDs.STATE));
        putRecord(IntRecord.getDefault(ConnectionIDs.CLIENT_PORT));
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

    @Override
    public int getTypeID() {
        return ConnectionIDs.TYPE_ID;
    }

    public static ConnectionDest getDefault() {
        return new ConnectionDest(-1, new HashMap<Integer, BaseRecord<?>>());
    }
}
