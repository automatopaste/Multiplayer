package data.scripts.net.data.tables;

import data.scripts.net.data.SourcePackable;

import java.util.Map;

public interface OutboundEntityManager {
    Map<Integer, SourcePackable> getOutbound();

    void update();

    void register();
}
