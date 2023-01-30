package data.scripts.net.data.packables.metadata;

import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.records.IntRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.plugins.MPPlugin;

import java.net.InetSocketAddress;

public class ConnectionData extends BasePackable {

    public static byte TYPE_ID;

    private byte connectionState;
    private int clientPort;

    public ConnectionData(short connectionID, final BaseConnectionWrapper connection) {
        super(connectionID);

        addRecord(new RecordLambda<>(
                ByteRecord.getDefault(),
                new SourceExecute<java.lang.Byte>() {
                    @Override
                    public java.lang.Byte get() {
                        return (byte) connection.getConnectionState().ordinal();
                    }
                },
                new DestExecute<java.lang.Byte>() {
                    @Override
                    public void execute(BaseRecord<java.lang.Byte> record, BasePackable packable) {
                        ConnectionData connectionData = (ConnectionData) packable;
                        connectionData.setConnectionState(record.getValue());
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                IntRecord.getDefault(),
                new SourceExecute<Integer>() {
                    @Override
                    public Integer get() {
                        return connection.getClientPort();
                    }
                },
                new DestExecute<Integer>() {
                    @Override
                    public void execute(BaseRecord<Integer> record, BasePackable packable) {
                        ConnectionData connectionData = (ConnectionData) packable;
                        connectionData.setClientPort(record.getValue());
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

    public static short getConnectionID(InetSocketAddress address) {
        byte[] ids = address.getAddress().getAddress();

        short id = 0;
        byte o = 0;
        for (int i = 0; i < 4; i++) {
            byte d = ids[i];
            id <<= (4 - i) * 4;
            id += o ^ d;
            o = d;
        }

        id = (short) ~ id;

        return id;
    }
}
