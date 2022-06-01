package data.scripts.console.commands;

import com.fs.starfarer.api.Global;
import data.scripts.plugins.mpServerPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class mpRunServer implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (context != CommandContext.COMBAT_SIMULATION) {
            Console.showMessage("Command only usable in simulation");
            return CommandResult.ERROR;
        }
        if (args.trim().isEmpty()) {
            Console.showMessage("Specify port");
            return CommandResult.BAD_SYNTAX;
        }

        String[] ids = args.split(" ");
        if (ids.length != 1) {
            Console.showMessage("Syntax error");
            return CommandResult.BAD_SYNTAX;
        }

        Console.showMessage("Starting server");
        Global.getCombatEngine().addPlugin(new mpServerPlugin());

        return CommandResult.SUCCESS;
    }
}
