package data.scripts.net.data.tables;

import data.scripts.net.data.BasePackable;

import java.util.Map;

public interface OutboundEntityManager {
    Map<Integer, BasePackable> getOutbound();

    void update();

    void register();
}
