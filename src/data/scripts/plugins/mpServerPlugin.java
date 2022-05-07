package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.packables.APackable;
import data.scripts.net.data.packables.InputAggregateData;
import data.scripts.net.terminals.server.NettyServer;
import data.scripts.plugins.state.ServerDataDuplex;
import data.scripts.plugins.state.ServerInputManager;
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

    private ServerInputManager inputManager;

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
        inputManager = new ServerInputManager();
        engine.addPlugin(inputManager);
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

        Map<Integer, APackable> entities = serverDataDuplex.update();
        for (APackable packable : entities.values()) {
            if (packable instanceof InputAggregateData) {
                inputManager.updateClientInput(packable.getInstanceID(), (InputAggregateData) packable);
            }
        }


    }
}