package data.scripts.net.data.tables;

import data.scripts.net.data.BaseRecord;
import data.scripts.plugins.MPPlugin;

import java.util.Map;

public interface InboundEntityManager extends BaseEntityManager {

    void processDelta(int id, Map<Integer, BaseRecord<?>> toProcess, MPPlugin plugin);
}
