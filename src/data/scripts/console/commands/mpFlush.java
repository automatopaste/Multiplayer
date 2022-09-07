package data.scripts.console.commands;

import com.fs.starfarer.api.Global;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class mpFlush implements BaseCommand {
    public static final String FLUSH_KEY = "JESSE_WE_NEED_TO_COOK";
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!(context == CommandContext.COMBAT_CAMPAIGN || context == CommandContext.COMBAT_SIMULATION || context == CommandContext.COMBAT_MISSION)) {
            Console.showMessage("Command only usable in combat");
            return CommandResult.ERROR;
        }

        Console.showMessage("Flushing data");
        Global.getCombatEngine().getCustomData().put(FLUSH_KEY, new Object());

        return CommandResult.SUCCESS;
    }
}
