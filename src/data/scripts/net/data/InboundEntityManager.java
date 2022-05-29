package data.scripts.net.data;

import java.util.Map;

public interface InboundEntityManager {
    void processDeltas(Map<Integer, BasePackable> toProcess);

    void updateEntities();
}
