package data.scripts.plugins.state;

import data.scripts.net.data.packables.APackable;
import data.scripts.plugins.mpServerPlugin;

import java.util.ArrayList;
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
    public void updateEntities() {
        List<Integer> toRemove = new ArrayList<>();

        for (Integer key : entities.keySet()) {
            APackable entity = entities.get(key);
            entity.destinationUpdate();

            if (entity.shouldDeleteOnDestination()) toRemove.add(key);
        }

        for (Integer i : toRemove) {
            entities.remove(i);
        }
    }
}
