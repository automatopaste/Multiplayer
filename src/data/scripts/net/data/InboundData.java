package data.scripts.net.data;

import java.util.Map;
import java.util.Set;

public class InboundData {
    public final Map<Byte, Map<Short, Map<Byte, Object>>> in;
    public final Map<Byte, Set<Short>> deleted;
    public int size = -1;

    public InboundData(Map<Byte, Map<Short, Map<Byte, Object>>> in, Map<Byte, Set<Short>> deleted) {
        this.in = in;
        this.deleted = deleted;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
