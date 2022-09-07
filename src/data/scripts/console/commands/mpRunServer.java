package data.scripts.console.commands;

import com.fs.starfarer.api.Global;
import data.scripts.plugins.MPServerPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class mpRunServer implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!(context == CommandContext.COMBAT_CAMPAIGN || context == CommandContext.COMBAT_SIMULATION || context == CommandContext.COMBAT_MISSION)) {
            Console.showMessage("Command only usable in combat");
            return CommandResult.ERROR;
        }

        Console.showMessage("Starting server");
        Global.getCombatEngine().addPlugin(new MPServerPlugin());

        return CommandResult.SUCCESS;
    }
}
