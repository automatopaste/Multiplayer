package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.server.NettyServer;
import org.lazywizard.console.Console;

import java.util.List;

public class mpServerPlugin extends BaseEveryFrameCombatPlugin {
    private NettyServer server;
    private Thread serverThread;

    private final int port;

    public mpServerPlugin(int port) {
        this.port = port;
    }

    @Override
    public void init(CombatEngineAPI engine) {
        server = new NettyServer(port);
        serverThread = new Thread(server, "mpServer");
        serverThread.start();
    }

    private float tracker = 0f;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        final float limit = 10f;
        tracker += amount;
        if (tracker > limit) {
            serverThread.interrupt();
            serverThread = null;
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Closed server");
        }
    }
}