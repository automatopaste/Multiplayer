package data.scripts.console.commands;

import com.fs.starfarer.api.Global;
import data.scripts.plugins.MPClientPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class mpConnectToHost implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (context != CommandContext.COMBAT_SIMULATION) {
            Console.showMessage("Command only usable in simulation");
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

//        String[] address = ids[0].split(":");
//        String host = address[0];
//        int port = Integer.parseInt(address[1]);

        String a = Global.getSettings().getString("mpHost");
        String[] b = a.split(":");
        String host = b[0];
        int port = Integer.parseInt(b[1]);

        Console.showMessage("Starting client on port " + port);
        Global.getCombatEngine().addPlugin(new MPClientPlugin(host, port));
        Console.showMessage("Client started successfully");

        return CommandResult.SUCCESS;
    }
}
