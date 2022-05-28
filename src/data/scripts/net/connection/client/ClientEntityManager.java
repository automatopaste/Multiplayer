package data.scripts.net.connection.client;

import data.scripts.net.connection.InboundEntityManager;
import data.scripts.net.data.BasePackable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientEntityManager implements InboundEntityManager {
    private final Map<Integer, BasePackable> entities;

    public ClientEntityManager() {
        entities = new HashMap<>();
    }

    /**
     * Take incoming partial entity deltas and update existing entities. If no existing entity matches delta instance ID
     * then assume it is newly created and init it
     * @param toProcess new deltas
     */
    public void processDeltas(Map<Integer, BasePackable> toProcess) {
        for (Integer key : toProcess.keySet()) {
            BasePackable entity = entities.get(key);
            BasePackable newEntity = toProcess.get(key);
            if (entity == null) {
                newEntity.destinationInit();
                entities.put(key, newEntity);
            } else {
                entity.updateFromDelta(newEntity);
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
