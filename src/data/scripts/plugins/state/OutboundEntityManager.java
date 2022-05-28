package data.scripts.plugins.state;

import data.scripts.net.data.BasePackable;

import java.util.Map;

public interface OutboundEntityManager {
    Map<Integer, ? extends BasePackable> getEntities();

    void update();
}
