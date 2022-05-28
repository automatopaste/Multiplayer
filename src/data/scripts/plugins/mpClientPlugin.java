package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.console.commands.mpFlush;
import data.scripts.data.LoadedDataStore;
import data.scripts.net.connection.client.ClientConnectionManager;
import data.scripts.net.connection.client.ClientEntityManager;
import data.scripts.net.connection.client.ClientInputManager;
import data.scripts.net.connection.client.NettyClient;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.InputAggregateData;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map;

public class mpClientPlugin extends BaseEveryFrameCombatPlugin {
    private final NettyClient client;
    private Thread clientThread;

    private final ClientConnectionManager connection;
    private final ClientEntityManager entityManager;
    private final ClientInputManager inputManager;
    private LoadedDataStore loadedDataStore;
    private boolean loaded = true;

    private final int port;
    private final String host;

    public mpClientPlugin(String host, int port) {
        this.host = host;
        this.port = port;

        connection = new ClientConnectionManager();

        client = new NettyClient(host, port, connection.getDuplex());
        clientThread = new Thread(client, "mpClient");
        clientThread.start();

        entityManager = new ClientEntityManager();

        // placeholder id
        int id = -10;
        inputManager = new ClientInputManager(id, new InputAggregateData(id));

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            Global.getCombatEngine().removeEntity(ship);
        }
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

        // wait until incoming data packets have finished arriving on network thread
        if (connection.isLoading()) {
            return;
        } else if (loaded) {
            Map<Integer, BasePackable> loadedEntities = connection.getDuplex().getDeltas();
            loadedDataStore = new LoadedDataStore(loadedEntities);

            loaded = false;
        }

        CombatEngineAPI engine = Global.getCombatEngine();

        Object thing = engine.getCustomData().get(mpFlush.FLUSH_KEY);
        if (thing != null) {
            engine.getCustomData().remove(mpFlush.FLUSH_KEY);

            connection.getDuplex().flush();
            Console.showMessage("Flushing client outbound data");
        }

        Map<Integer, BasePackable> entities = connection.getDuplex().getDeltas();

        entityManager.processDeltas(entities);
        entityManager.updateEntities();

        Map<Integer, BasePackable> outbound = inputManager.getEntities();
        connection.getDuplex().updateOutbound(outbound);
    }
}