package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.terminals.server.ServerPacketManager;
import data.scripts.net.terminals.server.NettyServer;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class mpServerPlugin extends BaseEveryFrameCombatPlugin {
    private final int port;
    private final Logger logger;

    private NettyServer server;
    private Thread serverThread;

    private final ServerPacketManager serverPacketManager;

    public mpServerPlugin(int port) {
        this.port = port;
        logger = Global.getLogger(mpServerPlugin.class);

        serverPacketManager = new ServerPacketManager();
    }

    @Override
    public void init(CombatEngineAPI engine) {
        server = new NettyServer(port, serverPacketManager);
        serverThread = new Thread(server, "mpServer");

        logger.info("Starting server");

        serverThread.start();
    }

    private float tracker = 0f;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (!serverThread.isAlive() || serverThread.isInterrupted()) {
            serverThread = null;
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Server interrupted");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            serverThread.interrupt();
            serverThread = null;
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Closed server");
        }
    }
}