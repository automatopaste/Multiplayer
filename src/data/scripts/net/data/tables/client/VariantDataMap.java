package data.scripts.net.data.tables.client;

import data.scripts.net.data.packables.entities.ship.VariantData;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class VariantDataMap implements InboundEntityManager {
    private final Map<Short, VariantData> variants;

    public VariantDataMap() {
        variants = new HashMap<>();
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        VariantData data = variants.get(instanceID);

        if (data == null) {
            VariantData variantData = new VariantData(instanceID, null, null);
            variants.put(instanceID, variantData);

            variantData.destExecute(toProcess);

            variantData.init(plugin, );
        } else {
            data.destExecute(toProcess);
        }
    }

    public VariantData find(String shipID) {
        for (VariantData variantData : variants.values()) {
            if (variantData.getFleetMemberID().equals(shipID)) return variantData;
        }
        return null;
    }

    @Override
    public void update(float amount, MPPlugin plugin) {

    }

    public Map<Short, VariantData> getVariants() {
        return variants;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(VariantData.TYPE_ID, this);
    }
}
