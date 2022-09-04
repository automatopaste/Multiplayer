package data.scripts.net.data.tables.client;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.entities.VariantData;
import data.scripts.net.data.tables.InboundEntityManager;
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
        VariantData data = (VariantData) toProcess;
        variantData.put(data.getShipId().getRecord(), data);
    }

    @Override
    public void updateEntities() {

    }

    public Map<String, VariantData> getVariantData() {
        return variantData;
    }
}
