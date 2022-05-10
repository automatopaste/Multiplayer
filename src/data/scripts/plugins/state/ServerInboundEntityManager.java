package data.scripts.plugins.state;

import data.scripts.net.data.packables.APackable;
import data.scripts.plugins.mpServerPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerInboundEntityManager implements InboundEntityManager {
    private final Map<Integer, APackable> entities;

    public ServerInboundEntityManager(mpServerPlugin serverPlugin) {
        entities = new HashMap<>();
    }

    @Override
    public void processDeltas(Map<Integer, APackable> toProcess) {
        for (Integer key : toProcess.keySet()) {
            APackable entity = entities.get(key);
            if (entity == null) {
                APackable newEntity = toProcess.get(key);
                newEntity.destinationInit();
                entities.put(key, newEntity);
            } else {
                entity.updateFromDelta(entity);
            }
        }
    }

    @Override
    public void delete(List<Integer> toDelete) {
        for (Integer i : toDelete) {
            APackable entity = entities.get(i);
            if (entity != null) {
                entity.destinationDelete();
            }
            entities.remove(i);
        }
    }

    @Override
    public void updateEntities() {
        for (APackable entity : entities.values()) {
            entity.destinationUpdate();
        }
    }
}
