package data.scripts.net.io;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InboundData;
import data.scripts.net.data.OutboundData;
import data.scripts.net.data.packables.metadata.ConnectionData;
import data.scripts.net.data.records.DataRecord;
import data.scripts.plugins.MPPlugin;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public abstract class BaseConnectionWrapper {
    public static final short DEFAULT_CONNECTION_ID = -10;

    public static final int MAX_PACKET_SIZE = Math.min(2048, Global.getSettings().getInt("MP_PacketSize"));

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

    public BaseConnectionWrapper(MPPlugin localPlugin) {
        this.localPlugin = localPlugin;
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

    public abstract List<MessageContainer> getSocketMessages() throws IOException;

    public abstract List<MessageContainer> getDatagrams() throws IOException;

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

    public static List<MessageContainer> writeBuffer(OutboundData data, int tick, InetSocketAddress address, int connectionID) throws IOException {
        List<MessageContainer> out = new ArrayList<>();

        int numTypes = 0;
        int numDeletedTypes = 0;

        ByteBuf entities = UnpooledByteBufAllocator.DEFAULT.buffer(MAX_PACKET_SIZE);
        ByteBuf deleted = UnpooledByteBufAllocator.DEFAULT.buffer(MAX_PACKET_SIZE);

        ByteBuf temp = UnpooledByteBufAllocator.DEFAULT.buffer(MAX_PACKET_SIZE);

        for (byte type : data.out.keySet()) {
            numTypes++;

            // write type byte
            temp.writeByte(type);

            Map<Short, Map<Byte, DataRecord<?>>> instances = data.out.get(type);

            // write num instances short
            temp.writeShort(instances.size());

            for (short instance : instances.keySet()) {
                // write instance short
                temp.writeShort(instance);

                Map<Byte, DataRecord<?>> records = instances.get(instance);

                // write num records byte
                temp.writeByte(records.size());

                for (byte id : records.keySet()) {
                    DataRecord<?> record = records.get(id);

                    // write record id byte
                    temp.writeByte(id);

                    //write record type byte
                    byte typeID = record.getTypeId();
                    temp.writeByte(typeID);

                    // write record data bytes
                    record.write(temp);
                }
            }

            // check if buffer will breach cap
            if (entities.writerIndex() + temp.writerIndex() > entities.capacity()) {
                out.add(container(numTypes, entities, numDeletedTypes, deleted, tick, address, connectionID));
                entities.clear();
                numTypes = 0;
            }

            entities.writeBytes(temp);
            temp.clear();
        }

//        for (byte type : data.deleted.keySet()) {
//            numDeletedTypes++;
//
//            // write type byte
//            temp.writeByte(type);
//
//            Set<Short> instances = data.deleted.get(type);
//
//            // write num instances short
//            temp.writeShort(instances.size());
//
//            for (short instance : instances) {
//                // write deleted instance ids
//                temp.writeShort(instance);
//            }
//
//            // check if buffer will breach cap
//            if (deleted.writerIndex() + entities.writerIndex() + temp.writerIndex() > deleted.capacity() + entities.capacity()) {
//                out.add(container(numTypes, entities, numDeletedTypes, deleted, tick, address, connectionID));
//                entities.clear();
//                deleted.clear();
//                numTypes = 0;
//                numDeletedTypes = 0;
//            }
//
//            deleted.writeBytes(temp);
//            temp.clear();
//        }

        if (entities.writerIndex() > 0 || deleted.writerIndex() > 0) {
            out.add(container(numTypes, entities, numDeletedTypes, deleted, tick, address, connectionID));
            entities.clear();
            deleted.clear();
        }

        entities.release();
        deleted.release();
        temp.release();

        return out;
    }

    private static MessageContainer container(int numTypes, ByteBuf entities, int numDeletedTypes, ByteBuf deleted, int tick, InetSocketAddress address, int connectionID) throws IOException {
        ByteBuf dest = initBuffer(tick, connectionID);

        dest.writeByte(numTypes);
        dest.writeBytes(entities);
        dest.writeByte(numDeletedTypes);
        dest.writeBytes(deleted);

        return new MessageContainer(dest, tick, address, connectionID);
    }

    public static InboundData readBuffer(ByteBuf data) throws IOException {
        Map<Byte, Map<Short, Map<Byte, Object>>> inbound = new HashMap<>();
        Map<Byte, Set<Short>> deleted = new HashMap<>();

        byte numTypes = data.readByte();

        for (byte i = 0; i < numTypes; i++) {
            byte typeID = data.readByte();

            Map<Short, Map<Byte, Object>> instances = new HashMap<>();
            inbound.put(typeID, instances);

            short numInstances = data.readShort();

            for (short j = 0; j < numInstances; j++) {
                short instanceID = data.readShort();

                Map<Byte, Object> records = instances.get(instanceID);
                if (records == null) {
                    records = new HashMap<>();
                    instances.put(instanceID, records);
                }

                byte numRecords = data.readByte();

                for (byte k = 0; k < numRecords; k++) {
                    byte recordID = data.readByte();
                    byte recordTypeID = data.readByte();

                    try {
                        Object value = DataGenManager.recordFactory(recordTypeID).read(data);
                        records.put(recordID, value);
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

        byte numDeletedTypes = data.readByte();

        for (byte i = 0; i < numDeletedTypes; i++) {
            byte typeID = data.readByte();

            Set<Short> instances = new HashSet<>();
            deleted.put(typeID, instances);

            short numDeleted = data.readShort();

            for (int j = 0; j < numDeleted; j++) {
                short instance = data.readShort();
                instances.add(instance);
            }
        }

        return new InboundData(inbound, deleted);
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
