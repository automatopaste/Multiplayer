package data.scripts.net.data;

import data.scripts.net.data.records.DataRecord;

import java.util.Map;

public class InstanceData {
    public int size;
    public final Map<Byte, DataRecord<?>> records;

    public InstanceData(int size, Map<Byte, DataRecord<?>> records) {
        this.size = size;
        this.records = records;
    }
}
