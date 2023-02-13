package data.scripts.net.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OutboundData {
    public final Map<Byte, Map<Short, InstanceData>> out;
    public final Map<Byte, Set<Short>> deleted;
    public int size = -1;
    public byte connectionID;

    public OutboundData(byte connectionID) {
        this(new HashMap<Byte, Map<Short, InstanceData>>(), new HashMap<Byte, Set<Short>>(), connectionID);
    }

    public OutboundData(Map<Byte, Map<Short, InstanceData>> out, Map<Byte, Set<Short>> deleted, byte connectionID) {
        this.out = out;
        this.deleted = deleted;
        this.connectionID = connectionID;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
