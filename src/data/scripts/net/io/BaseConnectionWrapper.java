package data.scripts.net.io;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InboundData;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.OutboundData;
import data.scripts.net.data.packables.metadata.ChatListenData;
import data.scripts.net.data.records.DataRecord;
import data.scripts.plugins.MPPlugin;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public abstract class BaseConnectionWrapper {
    public static final int MAX_PACKET_SIZE = Math.min(2012, Global.getSettings().getInt("MP_PacketSize"));

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

    protected int clientPort;

    protected MPPlugin localPlugin;

    public BaseConnectionWrapper(MPPlugin localPlugin) {
        this.localPlugin = localPlugin;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
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
        buf.writeByte(connectionID);
        return buf;
    }

    public static List<MessageContainer> writeBuffer(OutboundData data, int tick, InetSocketAddress address, byte connectionID) throws IOException {
        List<MessageContainer> out = new ArrayList<>();

        List<OutboundData> toWrite = new ArrayList<>();
        OutboundData activeDest = new OutboundData(new HashMap<Byte, Map<Short, InstanceData>>(), new HashMap<Byte, Set<Short>>(), connectionID);
        toWrite.add(activeDest);

        int size = 0;
        for (byte type : data.out.keySet()) {
            Map<Short, InstanceData> instances = data.out.get(type);

            Map<Short, InstanceData> activeInstanceDest = activeDest.out.get(type);
            if (activeInstanceDest == null) {
                activeInstanceDest = new HashMap<>();
                activeDest.out.put(type, activeInstanceDest);
            }

            for (short instance : instances.keySet()) {
                InstanceData instanceData = instances.get(instance);

                if (size + instanceData.size > MAX_PACKET_SIZE) {
                    activeDest.setSize(size);

                    activeDest = new OutboundData(new HashMap<Byte, Map<Short, InstanceData>>(), new HashMap<Byte, Set<Short>>(), connectionID);
                    toWrite.add(activeDest);

                    activeInstanceDest = new HashMap<>();
                    activeDest.out.put(type, activeInstanceDest);

                    size = 0;
                }

                size += instanceData.size;

                activeInstanceDest.put(instance, instanceData);
            }
        }

        outer:
        for (byte type : data.deleted.keySet()) {
            Set<Short> deleted = data.deleted.get(type);

            int bigness = (1 + deleted.size()) * 2;

            if (bigness >= MAX_PACKET_SIZE) {
                throw new RuntimeException("Deleted instance buffer of size " + bigness + " exceeded maximum buffer size " + MAX_PACKET_SIZE);
            }

            for (OutboundData outboundData : toWrite) {
                int d = MAX_PACKET_SIZE - outboundData.size;

                if (d > bigness) {
                    outboundData.deleted.put(type, deleted);
                    continue outer;
                }
            }

            // unable to find packet to fit in at this point
            Map<Byte, Set<Short>> d = new HashMap<>();
            d.put(type, deleted);
            toWrite.add(new OutboundData(new HashMap<Byte, Map<Short, InstanceData>>(), d, connectionID));
        }

        for (OutboundData outboundData : toWrite) {
            ByteBuf entities = UnpooledByteBufAllocator.DEFAULT.buffer(MAX_PACKET_SIZE);
            ByteBuf deleted = UnpooledByteBufAllocator.DEFAULT.buffer(MAX_PACKET_SIZE);

            for (byte type : outboundData.out.keySet()) {
                // write type byte
                entities.writeByte(type);

                Map<Short, InstanceData> instances = outboundData.out.get(type);

                // write num instances short
                entities.writeShort(instances.size());

                for (short instance : instances.keySet()) {
                    InstanceData instanceData = instances.get(instance);

                    // write instance short
                    entities.writeShort(instance);

                    // write num records byte
                    entities.writeByte(instanceData.records.size());

                    for (byte id : instanceData.records.keySet()) {
                        DataRecord<?> record = instanceData.records.get(id);

                        // write record id byte
                        entities.writeByte(id);

                        //write record type byte
                        byte typeID = record.getTypeId();
                        entities.writeByte(typeID);

                        // write record data bytes
                        record.write(entities);

                        if (type == ChatListenData.TYPE_ID) {
                            float f = 0f;
                        }
                    }
                }
            }

            for (byte type : outboundData.deleted.keySet()) {
                // write type byte
                deleted.writeByte(type);

                Set<Short> instances = outboundData.deleted.get(type);

                // write num instances short
                entities.writeShort(instances.size());

                for (short instance : instances) {
                    deleted.writeShort(instance);
                }
            }

            out.add(container(outboundData.out.size(), entities, outboundData.deleted.size(), deleted, tick, address, connectionID));

            entities.release();
            deleted.release();
        }

        return out;
    }

    private static MessageContainer container(int numTypes, ByteBuf entities, int numDeletedTypes, ByteBuf deleted, int tick, InetSocketAddress address, byte connectionID) throws IOException {
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
