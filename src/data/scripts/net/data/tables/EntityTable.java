package data.scripts.net.data.tables;

import data.scripts.net.data.packables.EntityData;

import java.util.LinkedList;
import java.util.Queue;

/**
 * No real difference in speed vs using hashmaps, doesnt solve any problems and probably should be removed
 */
public abstract class EntityTable<T extends EntityData> {
    protected final T[] table;
    private final Queue<Short> vacant;

    public EntityTable(T[] array) {
        table = array;
        vacant = new LinkedList<>();
        for (short i = 0; i < array.length; i++) {
            vacant.add(i);
        }
    }

    protected void markVacant(short i) {
        vacant.add(i);
    }

    protected int getVacant() {
        Short i = vacant.poll();
        if (i == null) throw new NullPointerException("No vacant entity index found");
        return i;
    }

    public T[] getTable() {
        return table;
    }
}
