package data.scripts.plugins.state;

import data.scripts.net.data.packables.APackable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ships, Projectiles, Missiles, Fighters
 */
public class ClientEntityManager {
    private Map<Integer, APackable> entities;

    public ClientEntityManager() {
        entities = new HashMap<>();
    }

    public void processDeltas(List<APackable> toProcess) {
        for (APackable packable : toProcess) {

        }
    }
}
