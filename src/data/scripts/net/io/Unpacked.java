package data.scripts.net.io;

import data.scripts.net.data.packables.APackable;

import java.util.List;
import java.util.Map;

public class Unpacked {
    private final Map<Integer, APackable> unpacked;
    private final List<Integer> deleted;
    private final int tick;

    public Unpacked(Map<Integer, APackable> unpacked, List<Integer> deleted, int tick) {
        this.unpacked = unpacked;
        this.deleted = deleted;
        this.tick = tick;
    }

    public Map<Integer, APackable> getUnpacked() {
        return unpacked;
    }

    public List<Integer> getDeleted() {
        return deleted;
    }

    public int getTick() {
        return tick;
    }
}
