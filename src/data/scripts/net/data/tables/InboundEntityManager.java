package data.scripts.net.data.tables;

import data.scripts.net.data.BasePackable;
import data.scripts.plugins.MPPlugin;

public interface InboundEntityManager {
    void processDelta(int id, BasePackable toProcess, MPPlugin plugin);

    void updateEntities();
}
