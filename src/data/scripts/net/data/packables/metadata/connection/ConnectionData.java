package data.scripts.net.data.packables.metadata.connection;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.io.BaseConnectionWrapper;

public class ConnectionData extends BasePackable {

    public ConnectionData(int connectionID, final BaseConnectionWrapper connection) {
        super(connectionID);

        putRecord(new IntRecord(new BaseRecord.DeltaFunc<Integer>() {
            @Override
            public Integer get() {
                return connection.getConnectionState().ordinal();
            }
        }, ConnectionIDs.STATE));
        putRecord(new IntRecord(new BaseRecord.DeltaFunc<Integer>() {
            @Override
            public Integer get() {
                return connection.getClientPort();
            }
        }, ConnectionIDs.CLIENT_PORT));

    }

    @Override
    public int getTypeID() {
        return ConnectionIDs.TYPE_ID;
    }
}
