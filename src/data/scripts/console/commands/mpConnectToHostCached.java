package data.scripts.console.commands;

import com.fs.starfarer.api.Global;
import data.scripts.MPModPlugin;
import data.scripts.plugins.MPClientPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class mpConnectToHostCached implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!(context == CommandContext.COMBAT_CAMPAIGN || context == CommandContext.COMBAT_SIMULATION || context == CommandContext.COMBAT_MISSION)) {
            Console.showMessage("Command only usable in combat");
            return CommandResult.ERROR;
        }

        String a = Global.getSettings().getString("mpHost");
        String[] b = a.split(":");
        String host = b[0];
        int port = Integer.parseInt(b[1]);

        Console.showMessage("Starting client on port " + port);
        MPModPlugin.setPlugin(new MPClientPlugin(host, port));
        Console.showMessage("Client started successfully");

        return CommandResult.SUCCESS;
    }
}
