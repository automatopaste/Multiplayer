package data.scripts.net.data;

import java.util.Map;

public interface OutboundEntityManager {
    Map<Integer, ? extends BasePackable> getEntities();

    void update();
}
