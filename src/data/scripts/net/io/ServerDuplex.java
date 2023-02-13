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

    /**
     * Get a map of delta compressed instance ids and their entity
     * @return List of entities with partial data
     */
    public synchronized Map<Byte, InboundData> getDeltas() {
        Map<Byte, InboundData> in = new HashMap<>(inbound);
        inbound.clear();
        return in;
    }

    public synchronized void updateInbound(InboundData data, byte connectionID) {
        InboundData inbound = this.inbound.get(connectionID);
        if (inbound == null) inbound = new InboundData();

        for (Byte type : data.in.keySet()) {
            Map<Short, Map<Byte, Object>> inboundEntities = inbound.in.get(type);
            Map<Short, Map<Byte, Object>> deltas = data.in.get(type);

            if (inboundEntities == null) {
                inboundEntities = new HashMap<>();
                inbound.in.put(type, inboundEntities);
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
            Set<Short> deleted = inbound.deleted.get(type);
            Set<Short> deltas = data.deleted.get(type);

            if (deleted == null) {
                deleted = new HashSet<>();
                inbound.deleted.put(type, deleted);
            }

            deleted.addAll(deltas);
        }
    }

    public synchronized void updateOutboundSocket(byte connectionID, OutboundData outboundData) {
        OutboundData data = outboundSocket.get(connectionID);
        if (data == null) {
            data = new OutboundData(connectionID);
            outboundSocket.put(connectionID, data);
        }

        updateEntities(data.out, outboundData.out);
        updateDeleted(data.deleted, outboundData.deleted);
    }

    public synchronized void updateOutboundDatagram(byte connectionID, OutboundData outboundData) {
        OutboundData data = outboundDatagram.get(connectionID);
        if (data == null) {
            data = new OutboundData(connectionID);
            outboundDatagram.put(connectionID, data);
        }

        updateEntities(data.out, outboundData.out);
        updateDeleted(data.deleted, outboundData.deleted);
    }

    public synchronized Map<Byte, OutboundData> getOutboundSocket() {
        Map<Byte, OutboundData> out = new HashMap<>(outboundSocket);
        outboundSocket.clear();
        return out;
    }

    public synchronized Map<Byte, OutboundData> getOutboundDatagram() {
        Map<Byte, OutboundData> out = new HashMap<>(outboundDatagram);
        outboundDatagram.clear();
        return out;
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
}
