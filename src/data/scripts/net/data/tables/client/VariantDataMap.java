package data.scripts.net.data.tables.client;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.entities.VariantData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class VariantDataMap implements InboundEntityManager {
    private final Map<String, VariantData> variantData;

    public VariantDataMap() {
        variantData = new HashMap<>();
    }

    @Override
    public void processDelta(int id, BasePackable toProcess, MPPlugin plugin) {
        VariantData delta = (VariantData) toProcess;
        VariantData data = variantData.get(delta.getShipId().getRecord());

        if (data == null) {
            variantData.put(delta.getShipId().getRecord(), delta);
            delta.destinationInit(plugin);
        } else {
            data.updateFromDelta(delta);
        }
    }

    @Override
    public void updateEntities() {

    }

    public Map<String, VariantData> getVariantData() {
        return variantData;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(VariantData.TYPE_ID, this);
    }
}
