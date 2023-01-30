package data.scripts.net.io;

import data.scripts.net.data.packables.metadata.ConnectionData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseConnectionWrapper {
    public static final short DEFAULT_CONNECTION_ID = -10;

    public enum ConnectionState {
        INITIALISATION_READY,
        INITIALISING,
        LOADING_READY,
        LOADING,
        SPAWNING_READY,
        SPAWNING,
        SIMULATION_READY,
        SIMULATING,
        CLOSED
    }
    protected ConnectionState connectionState = ConnectionState.INITIALISATION_READY;

    protected ConnectionData connectionData;

    protected short connectionID = DEFAULT_CONNECTION_ID;
    protected int clientPort;

    protected MPPlugin localPlugin;

    protected final ByteBuf socketBuffer;
    protected final ByteBuf datagramBuffer;

    public BaseConnectionWrapper(MPPlugin localPlugin) {
        this.localPlugin = localPlugin;

        socketBuffer = PooledByteBufAllocator.DEFAULT.buffer();
        datagramBuffer = PooledByteBufAllocator.DEFAULT.buffer();
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void setConnectionID(short connectionID) {
        this.connectionID = connectionID;
    }

    public short getConnectionID() {
        return connectionID;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public abstract MessageContainer getSocketMessage() throws IOException;

    public abstract MessageContainer getDatagram() throws IOException;

    public static BaseConnectionWrapper.ConnectionState ordinalToConnectionState(int state) {
        switch (state) {
            case 0:
                return BaseConnectionWrapper.ConnectionState.INITIALISATION_READY;
            case 1:
                return BaseConnectionWrapper.ConnectionState.INITIALISING;
            case 2:
                return BaseConnectionWrapper.ConnectionState.LOADING_READY;
            case 3:
                return BaseConnectionWrapper.ConnectionState.LOADING;
            case 4:
                return ConnectionState.SPAWNING_READY;
            case 5:
                return ConnectionState.SPAWNING;
            case 6:
                return BaseConnectionWrapper.ConnectionState.SIMULATION_READY;
            case 7:
                return BaseConnectionWrapper.ConnectionState.SIMULATING;
            case 8:
                return BaseConnectionWrapper.ConnectionState.CLOSED;
            default:
                return null;
        }
    }

    public static ByteBuf initBuffer(int tick, int connectionID) {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
        buf.writeInt(tick);
        buf.writeInt(connectionID);
        return buf;
    }

    public static void writeBuffer(Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> map, ByteBuf dest) {
        for (byte type : map.keySet()) {
            // write type byte
            dest.writeByte(type);

            Map<Short, Map<Byte, BaseRecord<?>>> instances = map.get(type);

            // write num instances short
            dest.writeShort(instances.size());

            for (short instance : instances.keySet()) {
                // write instance short
                dest.writeShort(instance);

                Map<Byte, BaseRecord<?>> records = instances.get(instance);

                // write num records byte
                dest.writeByte(records.size());

                for (byte id : records.keySet()) {
                    // write record id byte
                    dest.writeByte(id);

                    BaseRecord<?> record = records.get(id);
                    //write record type byte
                    byte typeID = record.getTypeId();
                    dest.writeByte(typeID);

                    // write record data bytes
                    record.write(dest);
                }
            }
        }
    }

    public static Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> readBuffer(ByteBuf data) throws IOException {
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> out = new HashMap<>();

        while (data.readableBytes() > 0) {
            byte typeID = data.readByte();

            Map<Short, Map<Byte, BaseRecord<?>>> instances = out.get(typeID);
            if (instances == null) {
                instances = new HashMap<>();
                out.put(typeID, instances);
            }

            short numInstances = data.readShort();

            for (short i = 0; i < numInstances; i++) {
                short instanceID = data.readShort();

                Map<Byte, BaseRecord<?>> records = instances.get(instanceID);
                if (records == null) {
                    records = new HashMap<>();
                    instances.put(instanceID, records);
                }

                byte numRecords = data.readByte();

                for (byte j = 0; j < numRecords; j++) {
                    byte recordID = data.readByte();
                    byte recordTypeID = data.readByte();

                    try {
                        BaseRecord<?> record = DataGenManager.recordFactory(recordTypeID).read(data);
                        records.put(recordID, record);
                    } catch (NullPointerException e) {
                        throw new IOException(
                                "Incorrect record type ID for destination " +
                                DataGenManager.inboundDataDestinations.get(typeID).getClass().getSimpleName() +
                                " at record ID " + recordID + " with instance " + instanceID
                        );
                    }
                }
            }
        }

        return out;
    }

    public abstract void stop();

    public MPPlugin getLocalPlugin() {
        return localPlugin;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(short clientPort) {
        this.clientPort = clientPort;
    }
}
