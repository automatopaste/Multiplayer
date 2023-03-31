package data.scripts.net.data.packables.metadata;

import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.plugins.MPPlugin;

public class ServerConnectionData extends EntityData {

    public static byte TYPE_ID;

    private byte connectionState;
    private byte connectionID;
    private long timestamp;
    private long latency;

    public ServerConnectionData(short instanceID, final byte connectionID, final BaseConnectionWrapper connection) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("connection state"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        return (byte) connection.getConnectionState().ordinal();
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        ServerConnectionData serverConnectionData = (ServerConnectionData) packable;
                        serverConnectionData.setConnectionState(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("connection id"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        return connectionID;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        ServerConnectionData serverConnectionData = (ServerConnectionData) packable;
                        serverConnectionData.setConnectionID(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("server flag"),
                new SourceExecute<Byte>() {
                    @Override
                    public Byte get() {
                        if (connection.sReceive == 0) { // this is very much a hack but it works
                            latency = System.currentTimeMillis() - timestamp;
                            connection.sReceive = 1;

                            timestamp = System.currentTimeMillis();
                            return (byte) 1;
                        }

                        return (byte) 0;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        connection.sListen = value;
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("client listen"),
                new SourceExecute<java.lang.Byte>() {
                    @Override
                    public java.lang.Byte get() {
                        return connection.cListen;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        connection.cReceive = value;
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount, BaseEntityManager manager, MPPlugin plugin) {

    }

    @Override
    public void delete() {

    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    public byte getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(byte connectionState) {
        this.connectionState = connectionState;
    }

    public void setConnectionID(byte connectionID) {
        this.connectionID = connectionID;
    }

    public byte getConnectionID() {
        return connectionID;
    }

    public long getLatency() {
        return latency;
    }
}
