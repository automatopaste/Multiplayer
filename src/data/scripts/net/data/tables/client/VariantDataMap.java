package data.scripts.net.data.tables.client;

import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.packables.entities.variant.VariantDest;
import data.scripts.net.data.packables.entities.variant.VariantIDs;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class VariantDataMap implements InboundEntityManager {
    private final Map<String, VariantDest> variantData;

    public VariantDataMap() {
        variantData = new HashMap<>();
    }

    @Override
    public void processDelta(int instanceID, Map<Integer, BaseRecord<?>> toProcess, MPPlugin plugin) {
        String shipID = (String) toProcess.get(VariantIDs.SHIP_ID).getValue();
        VariantDest data = variantData.get(shipID);

        if (data == null) {
            VariantDest variantDest = new VariantDest(instanceID, toProcess);
            variantData.put(shipID, variantDest);
            variantDest.init(plugin);
        } else {
            data.updateFromDelta(toProcess);
        }
    }

    @Override
    public void update(float amount) {

    }

    public Map<String, VariantDest> getVariantData() {
        return variantData;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(VariantIDs.TYPE_ID, this);
    }
}
