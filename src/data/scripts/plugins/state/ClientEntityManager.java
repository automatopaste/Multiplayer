package data.scripts.plugins.state;

import data.scripts.net.data.packables.APackable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientEntityManager implements InboundEntityManager {
    private final Map<Integer, APackable> entities;

    public ClientEntityManager() {
        entities = new HashMap<>();
    }

    /**
     * Take incoming partial entity deltas and update existing entities. If no existing entity matches delta instance ID
     * then assume it is newly created and init it
     * @param toProcess new deltas
     */
    public void processDeltas(Map<Integer, APackable> toProcess) {
        for (Integer key : toProcess.keySet()) {
            APackable entity = entities.get(key);
            APackable newEntity = toProcess.get(key);
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
            APackable entity = entities.get(key);
            entity.destinationUpdate();

            if (entity.shouldDeleteOnDestination()) toRemove.add(key);
        }

        for (Integer i : toRemove) {
            entities.remove(i);
        }
    }

    /**
     * Remove entities that the server has communicated should be discarded
     * @param toDelete list of instance IDs
     */
    public void delete(List<Integer> toDelete) {
        for (Integer i : toDelete) {
            APackable entity = entities.get(i);
            if (entity != null) {
                entity.destinationDelete();
            }
            entities.remove(i);
        }
    }
}
