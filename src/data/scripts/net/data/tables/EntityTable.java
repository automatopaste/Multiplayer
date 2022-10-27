package data.scripts.net.data.tables;

import data.scripts.net.data.packables.BasePackable;

import java.util.LinkedList;
import java.util.Queue;

public abstract class EntityTable {
    protected final BasePackable[] table;
    private final Queue<Integer> vacant;

    public EntityTable() {
        table = new BasePackable[getSize()];
        vacant = new LinkedList<>();
        for (int i = 0; i < getSize(); i++) {
            vacant.add(i);
        }
    }

    protected abstract int getSize();

    protected void markVacant(int i) {
        vacant.add(i);
    }

    protected int getVacant() {
        Integer i = vacant.poll();
        if (i == null) throw new NullPointerException("No vacant entity index found");
        return i;
    }
}
