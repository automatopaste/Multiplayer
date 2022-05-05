package data.scripts.net.io;

import data.scripts.net.data.records.ARecord;

import java.util.List;

public class Unpacked {
    private final List<List<ARecord>> unpacked;

    public Unpacked(List<List<ARecord>> unpacked) {
        this.unpacked = unpacked;
    }

    public List<List<ARecord>> getUnpacked() {
        return unpacked;
    }
}
