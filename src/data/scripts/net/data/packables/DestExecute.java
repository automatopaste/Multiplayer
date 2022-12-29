package data.scripts.net.data.packables;

import data.scripts.net.data.records.BaseRecord;

public interface DestExecute<T> {
    void execute(BaseRecord<T> record, BasePackable packable);
}
