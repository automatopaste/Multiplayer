package data.scripts.net.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OutboundData {
    private final Map<Byte, Map<Short, InstanceData>> out;
    private final Map<Byte, Set<Short>> deleted;
    private int size = -1;
    private final byte connectionID;

    public final Object sync = new Object();

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

    public int getSize() {
        return size;
    }

    public Map<Byte, Map<Short, InstanceData>> getOut() {
        return out;
    }

    public Map<Byte, Set<Short>> getDeleted() {
        return deleted;
    }
}
