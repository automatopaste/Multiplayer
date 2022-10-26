package data.scripts.net.data.packables.metadata.connection;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.SourcePackable;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.net.io.ClientConnectionWrapper;

public class ConnectionSource extends SourcePackable {

    public ConnectionSource(int connectionID, final BaseConnectionWrapper connection) {
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
                return ((ClientConnectionWrapper) connection).getLocalPort();
            }
        }, ConnectionIDs.CLIENT_PORT));

    }

    @Override
    public int getTypeId() {
        return ConnectionIDs.TYPE_ID;
    }
}
