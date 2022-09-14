package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.net.data.packables.entities.ship.ShipDest;
import data.scripts.net.data.packables.entities.ship.ShipIDs;
import data.scripts.net.data.packables.entities.ship.ShipSource;
import data.scripts.net.data.packables.entities.variant.VariantDest;
import data.scripts.net.data.packables.entities.variant.VariantIDs;
import data.scripts.net.data.packables.metadata.connection.ConnectionDest;
import data.scripts.net.data.packables.metadata.connection.ConnectionIDs;
import data.scripts.net.data.packables.metadata.pilot.PilotDest;
import data.scripts.net.data.packables.metadata.pilot.PilotIDs;
import data.scripts.net.data.records.*;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.DefaultAutofireAIPlugin;

public class MPModPlugin extends BaseModPlugin {

    private static MPPlugin PLUGIN;

    @Override
    public void onApplicationLoad() {
        ShipIDs.TYPE_ID = DataGenManager.registerEntityType(ShipSource.class, ShipDest.getDefault());
        VariantIDs.TYPE_ID = DataGenManager.registerEntityType(VariantDest.class, VariantDest.getDefault());
        PilotIDs.TYPE_ID = DataGenManager.registerEntityType(PilotDest.class, PilotDest.getDefault());
        ConnectionIDs.TYPE_ID = DataGenManager.registerEntityType(ConnectionDest.class, ConnectionDest.getDefault());

        FloatRecord.setTypeId(DataGenManager.registerRecordType(FloatRecord.class.getSimpleName(), FloatRecord.getDefault(-1)));
        IntRecord.setTypeId(DataGenManager.registerRecordType(IntRecord.class.getSimpleName(), IntRecord.getDefault(-1)));
        StringRecord.setTypeId(DataGenManager.registerRecordType(StringRecord.class.getSimpleName(), StringRecord.getDefault(-1)));
        Vector2fRecord.setTypeId(DataGenManager.registerRecordType(Vector2fRecord.class.getSimpleName(), Vector2fRecord.getDefault(-1)));
        ListRecord.setTypeId(DataGenManager.registerRecordType(ListRecord.class.getSimpleName(), ListRecord.getDefault(-1, -1)));
    }

    @Override
    public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
        if (getPlugin() != null && getPlugin().getType() == MPPlugin.PluginType.CLIENT) {
            return new PluginPick<>((AutofireAIPlugin) new DefaultAutofireAIPlugin(weapon), CampaignPlugin.PickPriority.HIGHEST);
        }
        return null;
    }

    public static void setPlugin(MPPlugin plugin) {
        if (PLUGIN != null) {
            Global.getCombatEngine().removePlugin(PLUGIN);
        }
        Global.getCombatEngine().addPlugin(plugin);
        PLUGIN = plugin;
    }

    public static void destroyPlugin() {
        if (PLUGIN == null) return;
        Global.getCombatEngine().removePlugin(PLUGIN);
    }

    public static MPPlugin getPlugin() {
        return PLUGIN;
    }
}