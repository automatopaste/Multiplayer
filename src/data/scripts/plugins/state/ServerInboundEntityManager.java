package data.scripts.plugins.state;

import data.scripts.net.data.BasePackable;
import data.scripts.plugins.mpServerPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerInboundEntityManager implements InboundEntityManager {
    private final Map<Integer, BasePackable> entities;

    public ServerInboundEntityManager(mpServerPlugin serverPlugin) {
        entities = new HashMap<>();
    }

    @Override
    public void processDeltas(Map<Integer, BasePackable> toProcess) {
        for (Integer key : toProcess.keySet()) {
            BasePackable entity = entities.get(key);
            if (entity == null) {
                BasePackable newEntity = toProcess.get(key);
                newEntity.destinationInit();
                entities.put(key, newEntity);
            } else {
                entity.updateFromDelta(entity);
            }
        }
    }

    @Override
    public void updateEntities() {
        List<Integer> toRemove = new ArrayList<>();

        for (Integer key : entities.keySet()) {
            BasePackable entity = entities.get(key);
            entity.destinationUpdate();

            if (entity.shouldDeleteOnDestination()) toRemove.add(key);
        }

        for (Integer i : toRemove) {
            entities.remove(i);
        }
    }
}
