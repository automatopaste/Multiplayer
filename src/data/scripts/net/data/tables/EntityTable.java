package data.scripts.net.data.tables;

import data.scripts.net.data.packables.EntityData;

import java.util.LinkedList;
import java.util.Queue;

public class EntityTable<T extends EntityData> {
    private final T[] table;
    public short limit = 0; // highest number of elements

    private final Queue<Short> vacant;

    public EntityTable(T[] array) {
        table = array;
        vacant = new LinkedList<>();
    }

    public short add(T t) {
        short id;

        if (vacant.isEmpty()) {
            id = limit;
            limit++;
        } else {
            id = vacant.poll();
        }

        if (id < 0 || id > table.length - 1) {
            throw new NullPointerException("No vacant entity index found");
        }

        table[id] = t;

        return id;
    }

    public void remove(short id) {
        vacant.add(id);
        table[id] = null;
    }

    public void set(short id, T t) {
        limit = (short) Math.max(id + 1, limit);
        table[id] = t;
    }

    public long hash() {
        return 0;
    }

    public T[] array() {
        return table;
    }
}
