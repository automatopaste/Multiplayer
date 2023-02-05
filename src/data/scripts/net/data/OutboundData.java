package data.scripts.net.data;

import data.scripts.net.data.records.DataRecord;

import java.util.Map;
import java.util.Set;

public class OutboundData {
    public final Map<Byte, Map<Short, Map<Byte, DataRecord<?>>>> out;
    public final Map<Byte, Set<Short>> deleted;

    public OutboundData(Map<Byte, Map<Short, Map<Byte, DataRecord<?>>>> out, Map<Byte, Set<Short>> deleted) {
        this.out = out;
        this.deleted = deleted;
    }
}
