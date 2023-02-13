package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.net.data.packables.entities.projectiles.ProjectileData;
import data.scripts.net.data.packables.entities.ships.ShieldData;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.packables.entities.ships.VariantData;
import data.scripts.net.data.packables.metadata.*;
import data.scripts.net.data.records.*;
import data.scripts.net.data.records.collections.ListenArrayRecord;
import data.scripts.net.data.records.collections.SyncingListRecord;
import data.scripts.net.data.DataGenManager;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.ai.MPDefaultAutofireAIPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MPModPlugin extends BaseModPlugin {

    public static String VERSION = "v0.1.1";

    private static MPPlugin PLUGIN;

    @Override
    public void onApplicationLoad() {
        ShipData.TYPE_ID = DataGenManager.registerEntityType(ShipData.class);
        ShieldData.TYPE_ID = DataGenManager.registerEntityType(ShieldData.class);
        VariantData.TYPE_ID = DataGenManager.registerEntityType(VariantData.class);
        ClientData.TYPE_ID = DataGenManager.registerEntityType(ClientData.class);
        PlayerShipData.TYPE_ID = DataGenManager.registerEntityType(PlayerShipData.class);
        ServerConnectionData.TYPE_ID = DataGenManager.registerEntityType(ServerConnectionData.class);
        ClientConnectionData.TYPE_ID = DataGenManager.registerEntityType(ClientConnectionData.class);
        LobbyData.TYPE_ID = DataGenManager.registerEntityType(LobbyData.class);
        ProjectileData.TYPE_ID = DataGenManager.registerEntityType(ProjectileData.class);
        ChatListenData.TYPE_ID = DataGenManager.registerEntityType(ChatListenData.class);

        Float32Record.setTypeId(DataGenManager.registerRecordType(Float32Record.class.getSimpleName(), Float32Record.getDefault()));
        IntRecord.setTypeId(DataGenManager.registerRecordType(IntRecord.class.getSimpleName(), IntRecord.getDefault()));
        StringRecord.setTypeId(DataGenManager.registerRecordType(StringRecord.class.getSimpleName(), StringRecord.getDefault()));
        Vector2f32Record.setTypeId(DataGenManager.registerRecordType(Vector2f32Record.class.getSimpleName(), Vector2f32Record.getDefault()));
        Vector3f32Record.setTypeId(DataGenManager.registerRecordType(Vector3f32Record.class.getSimpleName(), Vector3f32Record.getDefault()));
        SyncingListRecord.setTypeId(DataGenManager.registerRecordType(SyncingListRecord.class.getSimpleName(), new SyncingListRecord<>(new ArrayList<>(), (byte) -1)));
        Float16Record.setTypeId(DataGenManager.registerRecordType(Float16Record.class.getSimpleName(), Float16Record.getDefault()));
        ByteRecord.setTypeId(DataGenManager.registerRecordType(ByteRecord.class.getSimpleName(), ByteRecord.getDefault()));
        Vector2f16Record.setTypeId(DataGenManager.registerRecordType(Vector2f16Record.class.getSimpleName(), Vector2f16Record.getDefault()));
        ShortRecord.setTypeId(DataGenManager.registerRecordType(ShortRecord.class.getSimpleName(), ShortRecord.getDefault()));
        ListenArrayRecord.setTypeId(DataGenManager.registerRecordType(ListenArrayRecord.class.getSimpleName(), new ListenArrayRecord<>(new ArrayList<>(), (byte) -1)));
    }

    @Override
    public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
        if (getPlugin() != null && getPlugin().getType() == MPPlugin.PluginType.CLIENT) {
            MPDefaultAutofireAIPlugin plugin = new MPDefaultAutofireAIPlugin(weapon);

            ShipAPI ship = weapon.getShip();
            MPClientPlugin clientPlugin = (MPClientPlugin) getPlugin();
            Map<String, MPDefaultAutofireAIPlugin> plugins = clientPlugin.getShipTable().getTempAutofirePlugins().get(ship.getId());
            if (plugins == null) plugins = new HashMap<>();
            plugins.put(weapon.getSlot().getId(), plugin);
            clientPlugin.getShipTable().getTempAutofirePlugins().put(ship.getId(), plugins);

            return new PluginPick<>((AutofireAIPlugin) plugin, CampaignPlugin.PickPriority.HIGHEST);
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