package data.scripts.net.data.packables.metadata.connection;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.DestPackable;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.records.StringRecord;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class ConnectionDest extends DestPackable {

    private BaseConnectionWrapper connection;

    public ConnectionDest(int connectionID, Map<Integer, BaseRecord<?>> records) {
        super(connectionID, records);
    }

    @Override
    protected void initDefaultRecords() {
        putRecord(IntRecord.getDefault(ConnectionIDs.STATE));
        putRecord(StringRecord.getDefault(ConnectionIDs.CLIENT_ACTIVE_SHIP_ID));
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
    public int getTypeId() {
        return ConnectionIDs.TYPE_ID;
    }

    public void setConnection(BaseConnectionWrapper connection) {
        this.connection = connection;
    }

    public static ConnectionDest getDefault() {
        return new ConnectionDest(-1, new HashMap<Integer, BaseRecord<?>>());
    }
}