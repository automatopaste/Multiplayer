package data.scripts.net.data;

import java.util.Map;
import java.util.Set;

public class OutboundData {
    public final Map<Byte, Map<Short, InstanceData>> out;
    public final Map<Byte, Set<Short>> deleted;
    public int size = -1;

    public OutboundData(Map<Byte, Map<Short, InstanceData>> out, Map<Byte, Set<Short>> deleted) {
        this.out = out;
        this.deleted = deleted;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
