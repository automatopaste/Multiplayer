package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import data.scripts.plugins.gui.MPChatboxPlugin;

public class MPLogger {

    public static void info(Class<?> c, Object message) {
        Global.getLogger(c).info(message);

        if (message instanceof String) {
            String s = c.getSimpleName() + ": " + message;
            MPChatboxPlugin.INSTANCE.addEntry(new MPChatboxPlugin.ChatEntry(s, "[ SYS ]", (byte) 0xFF, true));
        }
    }

    public static void error(Class<?> c, Object message) {
        Global.getLogger(c).error(message);

        if (message instanceof String) {
            String s = c.getSimpleName() + ": " + message;
            MPChatboxPlugin.INSTANCE.addEntry(new MPChatboxPlugin.ChatEntry(s, "[ ERR ]", (byte) 0xFF, true));
        }
    }

}
