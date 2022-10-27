package data.scripts.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.MPModPlugin;
import data.scripts.net.data.packables.metadata.lobby.LobbyIDs;
import data.scripts.net.data.records.StringRecord;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class mpPilotShip  implements BaseCommand {

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!(context == CommandContext.COMBAT_CAMPAIGN || context == CommandContext.COMBAT_SIMULATION || context == CommandContext.COMBAT_MISSION)) {
            Console.showMessage("Command only usable in combat");
            return CommandResult.ERROR;
        }

        Vector2f loc = new Vector2f(Global.getCombatEngine().getViewport().getCenter());

        MPPlugin plugin = MPModPlugin.getPlugin();
        if (plugin == null || plugin.getType() != MPPlugin.PluginType.CLIENT) {
            Console.showMessage("Command only usable on client");
            return CommandResult.ERROR;
        }
        MPClientPlugin clientPlugin = (MPClientPlugin) plugin;

        List<StringRecord> ids = (List<StringRecord>) clientPlugin.getLobbyInput().getLobby().getRecord(LobbyIDs.PLAYER_SHIP_IDS).getValue();
        Set<String> occupied = new HashSet<>();
        for (StringRecord s : ids) {
            occupied.add(s.getValue());
        }

        ShipAPI ship = getClosest(loc, Global.getCombatEngine(), occupied);

        if (ship != null) {
            Global.getCombatEngine().setPlayerShipExternal(ship);

            Console.showMessage("Piloting ship " + ship.getName());
            return CommandResult.SUCCESS;
        } else {
            Console.showMessage("Unable to find ship");
            return CommandResult.ERROR;
        }
    }

    private static ShipAPI getClosest(Vector2f loc, CombatEngineAPI engine, Set<String> occupied) {
        ShipAPI out = null;
        float d = Float.MAX_VALUE;
        for (ShipAPI ship : engine.getShips()) {
            if (occupied.contains(ship.getFleetMemberId())) continue;

            float d2 = Vector2f.sub(ship.getLocation(), loc, new Vector2f()).lengthSquared();
            if (d2 < d) {
                d = d2;
                out = ship;
            }
        }
        return out;
    }
}