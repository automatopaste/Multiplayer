package data.scripts.console.commands;

import data.scripts.plugins.mpClientPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class mpFlush implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (context != CommandContext.COMBAT_SIMULATION) {
            Console.showMessage("Command only usable in simulation");
            return CommandResult.ERROR;
        }

        Console.showMessage("Flushing data");
        mpClientPlugin.flush();
        Console.showMessage("Flushed data successfully");

        return CommandResult.SUCCESS;
    }
}
