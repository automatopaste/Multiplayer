package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.net.data.packables.entities.ship.ShipData;
import data.scripts.net.data.packables.entities.variant.VariantData;
import data.scripts.net.data.packables.metadata.connection.ConnectionData;
import data.scripts.net.data.packables.metadata.lobby.LobbyData;
import data.scripts.net.data.packables.metadata.player.PlayerData;
import data.scripts.net.data.packables.metadata.playership.PlayerShipData;
import data.scripts.net.data.records.*;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultAutofireAIPlugin;

import java.util.ArrayList;

public class MPModPlugin extends BaseModPlugin {

    private static MPPlugin PLUGIN;

    @Override
    public void onApplicationLoad() {
        ShipData.TYPE_ID = DataGenManager.registerEntityType(ShipData.class);
        VariantData.TYPE_ID = DataGenManager.registerEntityType(VariantData.class);
        PlayerData.TYPE_ID = DataGenManager.registerEntityType(PlayerData.class);
        PlayerShipData.TYPE_ID = DataGenManager.registerEntityType(PlayerShipData.class);
        ConnectionData.TYPE_ID = DataGenManager.registerEntityType(ConnectionData.class);
        LobbyData.TYPE_ID = DataGenManager.registerEntityType(LobbyData.class);

        Float32Record.setTypeId(DataGenManager.registerRecordType(Float32Record.class.getSimpleName(), Float32Record.getDefault()));
        IntRecord.setTypeId(DataGenManager.registerRecordType(IntRecord.class.getSimpleName(), IntRecord.getDefault()));
        StringRecord.setTypeId(DataGenManager.registerRecordType(StringRecord.class.getSimpleName(), StringRecord.getDefault()));
        Vector2f32Record.setTypeId(DataGenManager.registerRecordType(Vector2f32Record.class.getSimpleName(), Vector2f32Record.getDefault()));
        Vector3f32Record.setTypeId(DataGenManager.registerRecordType(Vector3f32Record.class.getSimpleName(), Vector3f32Record.getDefault()));
        ListRecord.setTypeId(DataGenManager.registerRecordType(ListRecord.class.getSimpleName(), new ListRecord<>(new ArrayList<>(), (byte) -1)));
        Float16Record.setTypeId(DataGenManager.registerRecordType(Float16Record.class.getSimpleName(), Float16Record.getDefault()));
        ByteRecord.setTypeId(DataGenManager.registerRecordType(ByteRecord.class.getSimpleName(), ByteRecord.getDefault()));
        Vector2f16Record.setTypeId(DataGenManager.registerRecordType(Vector2f16Record.class.getSimpleName(), Vector2f16Record.getDefault()));
        ShortRecord.setTypeId(DataGenManager.registerRecordType(ShortRecord.class.getSimpleName(), ShortRecord.getDefault()));
    }

    @Override
    public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
        if (getPlugin() != null && getPlugin().getType() == MPPlugin.PluginType.CLIENT) {
            return new PluginPick<>((AutofireAIPlugin) new MPDefaultAutofireAIPlugin(weapon), CampaignPlugin.PickPriority.HIGHEST);
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