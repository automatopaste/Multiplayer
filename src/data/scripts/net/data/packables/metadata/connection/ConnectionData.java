package data.scripts.net.data.packables.metadata.connection;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.io.BaseConnectionWrapper;

public class ConnectionData extends BasePackable {

    public ConnectionData(short connectionID, final BaseConnectionWrapper connection) {
        super(connectionID);

        putRecord(new ByteRecord(new BaseRecord.DeltaFunc<Byte>() {
            @Override
            public Byte get() {
                return (byte) connection.getConnectionState().ordinal();
            }
        }, ConnectionIDs.STATE));
        putRecord(new ByteRecord(new BaseRecord.DeltaFunc<Byte>() {
            @Override
            public Byte get() {
                return (byte) connection.getClientPort();
            }
        }, ConnectionIDs.CLIENT_PORT));

    }

    @Override
    public int getTypeID() {
        return ConnectionIDs.TYPE_ID;
    }
}
