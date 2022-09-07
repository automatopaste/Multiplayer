package data.scripts.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.mpModPlugin;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import org.lwjgl.util.vector.Vector2f;

public class mpPilotShip  implements BaseCommand {

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!(context == CommandContext.COMBAT_CAMPAIGN || context == CommandContext.COMBAT_SIMULATION || context == CommandContext.COMBAT_MISSION)) {
            Console.showMessage("Command only usable in combat");
            return CommandResult.ERROR;
        }

        Vector2f loc = new Vector2f(Global.getCombatEngine().getViewport().getCenter());
        ShipAPI ship = getClosest(loc, Global.getCombatEngine());

        if (ship != null) {
            if (mpModPlugin.getPlugin().getType() == MPPlugin.PluginType.CLIENT) {
                MPClientPlugin plugin = (MPClientPlugin) mpModPlugin.getPlugin();
                plugin.getShipTable().setClientActive(ship);
            }

            Console.showMessage("Piloting ship " + ship.getName());
            return CommandResult.SUCCESS;
        } else {
            Console.showMessage("Unable to find ship");
            return CommandResult.ERROR;
        }
    }

    private static ShipAPI getClosest(Vector2f loc, CombatEngineAPI engine) {
        ShipAPI out = null;
        float d = Float.MAX_VALUE;
        for (ShipAPI ship : engine.getShips()) {
            float d2 = Vector2f.sub(ship.getLocation(), loc, new Vector2f()).lengthSquared();
            if (d2 < d) {
                d = d2;
                out = ship;
            }
        }
        return out;
    }
}