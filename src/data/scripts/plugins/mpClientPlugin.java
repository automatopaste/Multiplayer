package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.RecordDelta;
import data.scripts.net.terminals.client.ClientDataDuplex;
import data.scripts.net.terminals.client.NettyClient;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map;

public class mpClientPlugin extends BaseEveryFrameCombatPlugin {
    private NettyClient client;
    private Thread clientThread;

    private ClientDataDuplex clientDataDuplex;

    private final int port;
    private final String host;

    public mpClientPlugin(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void init(CombatEngineAPI engine) {
        clientDataDuplex = new ClientDataDuplex();
        client = new NettyClient(host, port, clientDataDuplex);
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

        clientDataDuplex.update();

        for (Map<Integer, RecordDelta> map : clientDataDuplex.getEntities()) {
            for (RecordDelta record : map.values()) {
                Global.getLogger(mpClientPlugin.class).info(record.toString());
            }
        }
    }
}