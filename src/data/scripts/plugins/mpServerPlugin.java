package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.PacketManager;
import data.scripts.net.server.NettyServer;
import org.apache.log4j.Logger;

import java.util.List;

public class mpServerPlugin extends BaseEveryFrameCombatPlugin {
    private final int port;
    private final Logger logger;

    private NettyServer server;
    private Thread serverThread;

    private final PacketManager packetManager;

    public mpServerPlugin(int port) {
        this.port = port;
        logger = Global.getLogger(mpServerPlugin.class);

        packetManager = new PacketManager();
    }

    @Override
    public void init(CombatEngineAPI engine) {
        server = new NettyServer(port, packetManager);
        serverThread = new Thread(server, "mpServer");

        logger.info("Starting server");

        serverThread.start();
    }

    private float tracker = 0f;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
//        final float limit = 10f;
//        tracker += amount;
//        if (tracker > limit) {
//            serverThread.interrupt();
//            serverThread = null;
//            Global.getCombatEngine().removePlugin(this);
//            Console.showMessage("Closed server");
//        }
    }
}