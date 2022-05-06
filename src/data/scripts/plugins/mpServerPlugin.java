package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.RecordDelta;
import data.scripts.net.terminals.server.ServerDataDuplex;
import data.scripts.net.terminals.server.NettyServer;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map;

public class mpServerPlugin extends BaseEveryFrameCombatPlugin {
    private final int port;
    private final Logger logger;

    private NettyServer server;
    private Thread serverThread;

    private final ServerDataDuplex serverDataDuplex;

    public mpServerPlugin(int port) {
        this.port = port;
        logger = Global.getLogger(mpServerPlugin.class);

        serverDataDuplex = new ServerDataDuplex();
    }

    @Override
    public void init(CombatEngineAPI engine) {
        server = new NettyServer(port, serverDataDuplex);
        serverThread = new Thread(server, "mpServer");

        logger.info("Starting server");

        serverThread.start();
    }

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

        for (Map<Integer, RecordDelta> map : serverDataDuplex.getEntities()) {
            for (RecordDelta record : map.values()) {
                Global.getLogger(mpClientPlugin.class).info(record.toString());
            }
        }
    }
}