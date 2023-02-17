package data.scripts.net.io;

import data.scripts.net.data.InboundData;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.OutboundData;
import data.scripts.net.data.records.DataRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerDuplex {

    private final Map<Byte, InboundData> inbound = new HashMap<>();
    private final Map<Byte, OutboundData> outboundSocket = new HashMap<>();
    private final Map<Byte, OutboundData> outboundDatagram = new HashMap<>();
    private final Map<Byte, Integer> latencies = new HashMap<>();

    /**
     * Get a map of delta compressed instance ids and their entity
     * @return List of entities with partial data
     */
    public synchronized Map<Byte, InboundData> getDeltas() {
        Map<Byte, InboundData> in = new HashMap<>(inbound);
        inbound.clear();
        return in;
    }

    public void updateInbound(InboundData data, byte connectionID) {
        InboundData inboundData;
        synchronized (inbound) {
            inboundData = this.inbound.get(connectionID);
            if (inboundData == null) {
                inboundData = new InboundData();
                this.inbound.put(connectionID, inboundData);
            }
        }

        synchronized (latencies) {
            if (data.latency != -1) {
                latencies.put(connectionID, data.latency);
            }
        }

        for (Byte type : data.in.keySet()) {
            Map<Short, Map<Byte, Object>> inboundEntities = inboundData.in.get(type);
            Map<Short, Map<Byte, Object>> deltas = data.in.get(type);

            if (inboundEntities == null) {
                inboundEntities = new HashMap<>();
                inboundData.in.put(type, inboundEntities);
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

        for (Byte type : data.deleted.keySet()) {
            Set<Short> deleted = inboundData.deleted.get(type);
            Set<Short> deltas = data.deleted.get(type);

            if (deleted == null) {
                deleted = new HashSet<>();
                inboundData.deleted.put(type, deleted);
            }

            deleted.addAll(deltas);
        }
    }

    public void updateOutboundSocket(byte connectionID, OutboundData outboundData) {
        OutboundData data;
        synchronized (outboundSocket) {
            data = outboundSocket.get(connectionID);
            if (data == null) {
                data = new OutboundData(connectionID);
                outboundSocket.put(connectionID, data);
            }
        }

        updateEntities(data.out, outboundData.out);
        updateDeleted(data.deleted, outboundData.deleted);
    }

    public void updateOutboundDatagram(byte connectionID, OutboundData outboundData) {
        OutboundData data;
        synchronized (outboundDatagram) {
            data = outboundDatagram.get(connectionID);
            if (data == null) {
                data = new OutboundData(connectionID);
                outboundDatagram.put(connectionID, data);
            }
        }

        updateEntities(data.out, outboundData.out);
        updateDeleted(data.deleted, outboundData.deleted);
    }

    public OutboundData getOutboundSocket(byte connectionID) {
        synchronized (outboundSocket) {
            OutboundData out = outboundSocket.get(connectionID);
            if (out == null) out = new OutboundData(connectionID);

            outboundSocket.put(connectionID, new OutboundData(connectionID));
            return out;
        }
    }

    public OutboundData getOutboundDatagram(byte connectionID) {
        synchronized (outboundDatagram) {
            OutboundData out = outboundDatagram.get(connectionID);
            if (out == null) out = new OutboundData(connectionID);

            outboundDatagram.put(connectionID, new OutboundData(connectionID));
            return out;
        }
    }

    public static void updateEntities(Map<Byte, Map<Short, InstanceData>> dest, Map<Byte, Map<Short, InstanceData>> deltaMap) {
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
                        DataRecord<?> record = outboundInstanceData.records.get(id);
                        DataRecord<?> delta = deltaInstanceData.records.get(id);

                        if (record == null) {
                            outboundInstanceData.records.put(id, delta);
                        } else {
                            outboundInstanceData.size -= record.size();
                            outboundInstanceData.size += delta.size();

                            record.overwrite(delta.getValue());
                        }
                    }
                }
            }
        }
    }

    public static void updateDeleted(Map<Byte, Set<Short>> destMap, Map<Byte, Set<Short>> deltaMap) {
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

    public Map<Byte, Integer> getLatencies() {
        synchronized (latencies) {
            return latencies;
        }
    }
}
