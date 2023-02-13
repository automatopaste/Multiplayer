package data.scripts.net.data.packables.metadata;

import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.plugins.MPPlugin;

public class ClientConnectionData extends EntityData {

    public static byte TYPE_ID;

    private byte connectionState;
    private int clientPort;
    private byte connectionID;

    private BaseConnectionWrapper.ConnectionState state = BaseConnectionWrapper.ConnectionState.INITIALISATION_READY;

    public ClientConnectionData(short instanceID, final BaseConnectionWrapper connection) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("connection state"),
                new SourceExecute<Byte>() {
                    @Override
                    public java.lang.Byte get() {
                        return (byte) state.ordinal();
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
                IntRecord.getDefault().setDebugText("client port"),
                new SourceExecute<Integer>() {
                    @Override
                    public Integer get() {
                        return connection.getClientPort();
                    }
                },
                new DestExecute<Integer>() {
                    @Override
                    public void execute(Integer value, EntityData packable) {
                        setClientPort(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("connection id"),
                new SourceExecute<java.lang.Byte>() {
                    @Override
                    public java.lang.Byte get() {
                        return -5;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        setConnectionID(value);
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount, BaseEntityManager manager) {

    }

    @Override
    public void delete() {

    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    public BaseConnectionWrapper.ConnectionState getState() {
        return state;
    }

    public void setState(BaseConnectionWrapper.ConnectionState state) {
        this.state = state;
    }

    public byte getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(byte connectionState) {
        this.connectionState = connectionState;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public void setConnectionID(byte connectionID) {
        this.connectionID = connectionID;
    }

    public byte getConnectionID() {
        return connectionID;
    }
}