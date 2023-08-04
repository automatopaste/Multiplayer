package data.scripts.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import org.lwjgl.util.vector.Vector2f;

import java.util.Set;

public class mpPilotShip  implements BaseCommand {

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        Console.showMessage("deprecated");

        for (final ShipAPI ship : Global.getCombatEngine().getShips()) {
            ship.setShipAI(new ShipAIPlugin() {
                @Override
                public void setDoNotFireDelay(float amount) {

                }

                @Override
                public void forceCircumstanceEvaluation() {

                }

                @Override
                public void advance(float amount) {
                    ship.giveCommand(ShipCommand.DECELERATE, null, 0);
                    ship.setHoldFireOneFrame(true);
                }

                @Override
                public boolean needsRefit() {
                    return false;
                }

                @Override
                public ShipwideAIFlags getAIFlags() {
                    ShipwideAIFlags flags = new ShipwideAIFlags();
                    flags.setFlag(ShipwideAIFlags.AIFlags.BACKING_OFF);
                    return flags;
                }

                @Override
                public void cancelCurrentManeuver() {

                }

                @Override
                public ShipAIConfig getConfig() {
                    return new ShipAIConfig();
                }
            });
        }

        return CommandResult.SUCCESS;
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