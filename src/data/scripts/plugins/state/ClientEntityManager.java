package data.scripts.plugins.state;

import data.scripts.net.data.packables.APackable;

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
            APackable toUpdate = toProcess.get(key);
            if (entity == null) {
                toUpdate.destinationInit();
                entities.put(key, toUpdate);
            } else {
                toUpdate.updateFromDelta(entity);
            }
        }
    }

    @Override
    public void updateEntities() {
        for (APackable entity : entities.values()) {
            entity.destinationUpdate();
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
