package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.packables.APackable;
import data.scripts.net.data.packables.InputAggregateData;
import data.scripts.net.terminals.client.NettyClient;
import data.scripts.plugins.state.ClientEntityManager;
import data.scripts.plugins.state.ClientInputManager;
import data.scripts.plugins.state.DataDuplex;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class mpClientPlugin extends BaseEveryFrameCombatPlugin {
    private NettyClient client;
    private Thread clientThread;

    private DataDuplex clientDataDuplex;
    private ClientEntityManager entityManager;
    private ClientInputManager inputManager;

    private final int port;
    private final String host;

    public mpClientPlugin(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void init(CombatEngineAPI engine) {
        clientDataDuplex = new DataDuplex();

        client = new NettyClient(host, port, clientDataDuplex);
        clientThread = new Thread(client, "mpClient");
        clientThread.start();

        entityManager = new ClientEntityManager();

        // placeholder id
        int id = -10;
        inputManager = new ClientInputManager(id, new InputAggregateData(id));
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

        Map<Integer, APackable> entities = clientDataDuplex.getDeltas();
        List<Integer> toDelete = new ArrayList<>(clientDataDuplex.getRemovedInbound());

        entityManager.delete(toDelete);
        entityManager.processDeltas(entities);

        entityManager.updateEntities();

        Map<Integer, APackable> outbound = inputManager.getEntities();

        // provide empty list because server does not care about any deletions on client
        clientDataDuplex.updateOutbound(outbound, new ArrayList<Integer>());
    }
}