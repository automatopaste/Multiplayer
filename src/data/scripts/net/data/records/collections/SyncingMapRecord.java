package data.scripts.net.data.records.collections;

import data.scripts.net.data.records.DataRecord;
import io.netty.buffer.ByteBuf;

import java.util.Map;

public class SyncingMapRecord<K, V> extends DataRecord<Map<K, V>> {

    public SyncingMapRecord(Map<K, V> value) {
        super(value);
    }

    @Override
    public void write(ByteBuf dest) {

    }

    @Override
    public Map<K, V> read(ByteBuf in) {
        return null;
    }

    @Override
    public byte getTypeId() {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    protected boolean checkUpdate(Map<K, V> delta) {
        return false;
    }
}
