package data.scripts.net.data.tables;

import data.scripts.plugins.MPPlugin;

import java.util.Map;

public interface InboundEntityManager extends BaseEntityManager {

    void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick);

    void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick);
}
