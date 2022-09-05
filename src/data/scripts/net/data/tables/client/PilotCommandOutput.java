package data.scripts.net.data.tables.client;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.trans.PilotCommandData;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;

import java.util.HashMap;
import java.util.Map;

public class PilotCommandOutput implements OutboundEntityManager {
    private final PilotCommandData input;
    private final int instanceID;

    public PilotCommandOutput(int instanceID, PilotCommandData input) {
        this.input = input;
        this.instanceID = instanceID;
    }

    @Override
    public Map<Integer, BasePackable> getOutbound() {
        Map<Integer, BasePackable> out = new HashMap<>();
        out.put(instanceID, input);
        return out;
    }

    @Override
    public void update() {

    }

    @Override
    public void register() {
        DataGenManager.registerOutboundEntityManager(PilotCommandData.TYPE_ID, this);
    }
}
