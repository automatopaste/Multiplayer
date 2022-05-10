package data.scripts.plugins.state;

import data.scripts.net.data.packables.APackable;

import java.util.List;
import java.util.Map;

public interface InboundEntityManager {
    void processDeltas(Map<Integer, APackable> toProcess);

    void updateEntities();

    void delete(List<Integer> toDelete);
}
