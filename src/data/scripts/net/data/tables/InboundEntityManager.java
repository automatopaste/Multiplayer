package data.scripts.net.data.tables;

import data.scripts.net.data.records.BaseRecord;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public interface InboundEntityManager extends BaseEntityManager {

    void processDelta(byte typeID, short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin, int tick);
}
