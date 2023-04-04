package data.scripts.net.data.tables.client.combat.entities;

import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.packables.entities.ships.VariantData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class VariantDataMap implements InboundEntityManager {
    private final Map<Short, VariantData> variants;

    public VariantDataMap() {
        variants = new HashMap<>();
    }

    @Override
    public void processDelta(byte typeID, short instanceID, Map<Byte, Object> toProcess, MPPlugin plugin, int tick, byte connectionID) {
        VariantData data = variants.get(instanceID);

        if (data == null) {
            VariantData variantData = new VariantData(instanceID, null, null);
            variants.put(instanceID, variantData);

            variantData.destExecute(toProcess, tick);

            variantData.init(plugin, this);
        } else {
            data.destExecute(toProcess, tick);
        }
    }

    @Override
    public void processDeletion(byte typeID, short instanceID, MPPlugin plugin, int tick, byte connectionID) {
        VariantData data = variants.get(instanceID);

        if (data != null) {
            data.delete();

            variants.remove(instanceID);
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
