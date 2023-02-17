package data.scripts.net.io;

import data.scripts.net.data.InboundData;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.OutboundData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientDuplex {

    private InboundData inbound = new InboundData();
    private OutboundData outboundSocket = new OutboundData((byte) -1);
    private OutboundData outboundDatagram = new OutboundData((byte) -1);
    private volatile int latency;

    public synchronized InboundData getDeltas() {
        InboundData in = inbound;
        inbound = new InboundData();
        return in;
    }

    public synchronized void updateInbound(InboundData data) {
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

        if (data.latency != -1) latency = data.latency;
    }

    public int getLatency() {
        return latency;
    }

    public synchronized void updateOutboundSocket(OutboundData bufferData) {
        ServerDuplex.updateEntities(outboundSocket.out, bufferData.out);
        ServerDuplex.updateDeleted(outboundSocket.deleted, bufferData.deleted);
    }

    public void updateOutboundDatagram(OutboundData bufferData) {
        ServerDuplex.updateEntities(outboundDatagram.out, bufferData.out);
        ServerDuplex.updateDeleted(outboundDatagram.deleted, bufferData.deleted);
    }

    public OutboundData getOutboundSocket() {
        OutboundData out = outboundSocket;
        outboundSocket = new OutboundData(new HashMap<Byte, Map<Short, InstanceData>>(), new HashMap<Byte, Set<Short>>(), (byte) -1);
        return out;
    }

    public OutboundData getOutboundDatagram() {
        OutboundData out = outboundDatagram;
        outboundDatagram = new OutboundData(new HashMap<Byte, Map<Short, InstanceData>>(), new HashMap<Byte, Set<Short>>(), (byte) -1);
        return out;
    }

    public int getNumSinceTick() {
        return 1;
    }
}
