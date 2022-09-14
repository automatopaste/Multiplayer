package data.scripts.console.commands;

import data.scripts.MPModPlugin;
import data.scripts.plugins.MPClientPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class mpConnectToHost implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!(context == CommandContext.COMBAT_CAMPAIGN || context == CommandContext.COMBAT_SIMULATION || context == CommandContext.COMBAT_MISSION)) {
            Console.showMessage("Command only usable in combat");
            return CommandResult.ERROR;
        }
        if (args.trim().isEmpty()) {
            Console.showMessage("Specify address");
            return CommandResult.BAD_SYNTAX;
        }

        String[] ids = args.split(" ");
        if (ids.length != 1) {
            Console.showMessage("Syntax error");
            return CommandResult.BAD_SYNTAX;
        }

        String[] address = ids[0].split(":");
        String host = address[0];
        int port = Integer.parseInt(address[1]);

        Console.showMessage("Starting client on port " + port);
        MPModPlugin.setPlugin(new MPClientPlugin(host, port));
        Console.showMessage("Client started successfully");

        return CommandResult.SUCCESS;
    }
}
