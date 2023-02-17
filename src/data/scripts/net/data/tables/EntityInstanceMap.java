package data.scripts.net.data.tables;

import data.scripts.net.data.packables.EntityData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntityInstanceMap<T extends EntityData> {

    public final Map<Short, T> registered;
    public final Set<Short> deleted;

    public EntityInstanceMap() {
        registered = new HashMap<>();
        deleted = new HashSet<>();
    }

    public EntityInstanceMap(Map<Short, T> registered, Set<Short> deleted) {
        this.registered = registered;
        this.deleted = deleted;
    }

    public void delete(short index) {
        registered.remove(index);
        deleted.add(index);
    }

    public Set<Short> getDeleted() {
        Set<Short> out = new HashSet<>(deleted);
        deleted.clear();
        return out;
    }
}
