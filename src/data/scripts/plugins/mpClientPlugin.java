package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.terminals.client.NettyClient;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class mpClientPlugin extends BaseEveryFrameCombatPlugin {
    private NettyClient client;
    private Thread clientThread;

    private final int port;
    private final String host;

    public mpClientPlugin(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void init(CombatEngineAPI engine) {
        client = new NettyClient(host, port);
        clientThread = new Thread(client, "mpClient");
        clientThread.start();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (!clientThread.isAlive() || clientThread.isInterrupted()) {
            clientThread = null;
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Server interrupted");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            client.stop();
            clientThread.interrupt();
            clientThread = null;
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Closed client");
        }
    }
}