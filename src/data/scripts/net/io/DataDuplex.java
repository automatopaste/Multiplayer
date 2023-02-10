package data.scripts.net.io;

import data.scripts.net.data.InboundData;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.OutboundData;
import data.scripts.net.data.records.DataRecord;

import java.util.*;

/**
 * Manage data between game and network threads
 */
public class DataDuplex {
    /**
     * Map Type ID to
     */
    private final Map<Byte, Map<Short, Map<Byte, Object>>> inbound;
    private final Map<Byte, Set<Short>> inboundDeleted;
    private final Map<Byte, Map<Short, InstanceData>> outboundSocket;
    private final Map<Byte, Set<Short>> outboundSocketDeleted;
    private final Map<Byte, Map<Short, InstanceData>> outboundDatagram;
    private final Map<Byte, Set<Short>> outboundDatagramDeleted;

    public DataDuplex() {
        inbound = new HashMap<>();
        inboundDeleted = new HashMap<>();
        outboundSocket = new HashMap<>();
        outboundSocketDeleted = new HashMap<>();
        outboundDatagram = new HashMap<>();
        outboundDatagramDeleted = new HashMap<>();
    }

    /**
     * Get a map of delta compressed instance ids and their entity
     * @return List of entities with partial data
     */
    public InboundData getDeltas() {
        HashMap<Byte, Map<Short, Map<Byte, Object>>> in;
        HashMap<Byte, Set<Short>> deleted;

        synchronized (inbound) {
            in = new HashMap<>(inbound);
            inbound.clear();
        }
        synchronized (inboundDeleted) {
            deleted = new HashMap<>(inboundDeleted);
            inboundDeleted.clear();
        }

        return new InboundData(in, deleted);
    }

    /**
     * Get outbound data and clear store
     * @return outbound entities
     */
    public OutboundData getOutboundSocket() {
        Map<Byte, Map<Short, InstanceData>> outEntities;
        synchronized (outboundSocket) {
            outEntities = new HashMap<>(outboundSocket);
            outboundSocket.clear();
        }

        Map<Byte, Set<Short>> outDeleted;
        synchronized (outboundSocketDeleted) {
            outDeleted = new HashMap<>(outboundSocketDeleted);
            outboundSocketDeleted.clear();
        }

        return new OutboundData(outEntities, outDeleted);
    }

    public OutboundData getOutboundDatagram() {
        Map<Byte, Map<Short, InstanceData>> outEntities;
        synchronized (outboundDatagram) {
            outEntities = new HashMap<>(outboundDatagram);
            outboundDatagram.clear();
        }

        Map<Byte, Set<Short>> outDeleted;
        synchronized (outboundDatagramDeleted) {
            outDeleted = new HashMap<>(outboundDatagramDeleted);
            outboundDatagramDeleted.clear();
        }

        return new OutboundData(outEntities, outDeleted);
    }

    /**
     * Synchronises update of current data store
     * @param data inbound data
     */
    public void updateInbound(InboundData data) {
        synchronized (inbound) {
            for (Byte type : data.in.keySet()) {
                Map<Short, Map<Byte, Object>> inboundEntities = inbound.get(type);
                Map<Short, Map<Byte, Object>> deltas = data.in.get(type);

                if (inboundEntities == null) {
                    inboundEntities = new HashMap<>();
                    inbound.put(type, inboundEntities);
                }

                for (Short instance : deltas.keySet()) {
                    Map<Byte, Object> p = inboundEntities.get(instance);
                    Map<Byte, Object> d = deltas.get(instance);

                    if (p == null) {
                        inboundEntities.put(instance, d);
                    } else {
                        for (Byte k : p.keySet()) {
                            Object delta = d.get(k);
                            if (delta != null) p.put(k, delta);
                        }
                    }
                }
            }
        }

        synchronized (inboundDeleted) {
            for (Byte type : data.deleted.keySet()) {
                Set<Short> deleted = inboundDeleted.get(type);
                Set<Short> deltas = data.deleted.get(type);

                if (deleted == null) {
                    deleted = new HashSet<>();
                    inboundDeleted.put(type, deleted);
                }

                deleted.addAll(deltas);
            }
        }
    }

    public void updateOutboundSocket(OutboundData bufferData) {
        synchronized (outboundSocket) {
            updateEntities(outboundSocket, bufferData.out);
        }
        synchronized (outboundSocketDeleted) {
            updateDeleted(outboundSocketDeleted, bufferData.deleted);
        }
    }

    public void updateOutboundDatagram(OutboundData bufferData) {
        synchronized (outboundDatagram) {
            updateEntities(outboundDatagram, bufferData.out);
        }
        synchronized (outboundDatagramDeleted) {
            updateDeleted(outboundDatagramDeleted, bufferData.deleted);
        }
    }

    private void updateEntities(Map<Byte, Map<Short, InstanceData>> dest, Map<Byte, Map<Short, InstanceData>> deltaMap) {
        for (byte type : deltaMap.keySet()) {
            Map<Short, InstanceData> outboundEntities = dest.get(type);
            Map<Short, InstanceData> deltas = deltaMap.get(type);

            if (outboundEntities == null) {
                outboundEntities = new HashMap<>();
                dest.put(type, outboundEntities);
            }

            for (short instance : deltas.keySet()) {
                InstanceData outboundInstanceData = outboundEntities.get(instance);
                InstanceData deltaInstanceData = deltas.get(instance);

                if (outboundInstanceData == null) {
                    outboundInstanceData = deltaInstanceData;
                    outboundEntities.put(instance, outboundInstanceData);
                } else {
                    for (byte id : deltaInstanceData.records.keySet()) {
                        DataRecord<?> outboundRecord = outboundInstanceData.records.get(id);
                        DataRecord<?> delta = deltaInstanceData.records.get(id);

                        if (outboundRecord == null) {
                            outboundRecord = delta;
                            outboundInstanceData.records.put(id, outboundRecord);
                            outboundInstanceData.size += delta.size();
                        }

                        outboundRecord.overwrite(outboundRecord.getValue());
                    }
                }
            }
        }
    }

    private void updateDeleted(Map<Byte, Set<Short>> destMap, Map<Byte, Set<Short>> deltaMap) {
        for (byte type : deltaMap.keySet()) {
            Set<Short> dest = destMap.get(type);
            Set<Short> deltas = deltaMap.get(type);

            if (dest == null) {
                dest = new HashSet<>();
                destMap.put(type, dest);
            }

            dest.addAll(deltas);
        }
    }
}
