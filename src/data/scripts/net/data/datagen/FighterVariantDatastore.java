package data.scripts.net.data.datagen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import data.scripts.plugins.MPPlugin;

import java.util.HashMap;
import java.util.Map;

public class FighterVariantDatastore implements BaseDatagen {

    private final Map<String, String> variants = new HashMap<>();

    @Override
    public void generate(MPPlugin plugin) {
        for (FighterWingSpecAPI spec : Global.getSettings().getAllFighterWingSpecs()) {
            String variant = spec.getVariantId();
            String hull = spec.getVariant().getHullSpec().getHullId();

            if (variant != null && hull != null) {
                variants.put(hull, variant);
            } else {
                throw new NullPointerException("fighter wing hullspec id or variant id was null");
            }
        }
    }

    public Map<String, String> getVariants() {
        return variants;
    }
}
