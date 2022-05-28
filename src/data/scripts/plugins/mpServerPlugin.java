package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.console.commands.mpFlush;
import data.scripts.data.LoadedDataStore;
import data.scripts.net.data.BasePackable;
import data.scripts.net.connection.server.NettyServer;
import data.scripts.net.connection.server.ServerCombatEntityManager;
import data.scripts.net.connection.server.ServerConnectionWrapper;
import data.scripts.net.connection.server.ServerInboundEntityManager;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class mpServerPlugin extends BaseEveryFrameCombatPlugin {
    private final int port;
    private final Logger logger;

    private NettyServer server;
    private Thread serverThread;

    private final List<ServerConnectionWrapper> connections = new ArrayList<>();

    private final ServerInboundEntityManager serverEntityManager;
    private final ServerCombatEntityManager serverCombatEntityManager;

    private int nextInstanceID = Integer.MIN_VALUE;
    private final Set<Integer> usedIDs = new HashSet<>();

    public mpServerPlugin(int port) {
        this.port = port;
        logger = Global.getLogger(mpServerPlugin.class);

        serverEntityManager = new ServerInboundEntityManager(this);

        serverCombatEntityManager = new ServerCombatEntityManager(this);

        server = new NettyServer(port, this);
        serverThread = new Thread(server, "mpServer");

        logger.info("Starting server");

        serverThread.start();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();

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

        for (ServerConnectionWrapper connection : connections) {
            if (connection.isRequestLoad()) {
                connection.getDuplex().updateOutbound(LoadedDataStore.generate(engine, this));
                connection.setRequestLoad(false);
            }
        }

        Object thing = engine.getCustomData().get(mpFlush.FLUSH_KEY);
        if (thing != null) {
            engine.getCustomData().remove(mpFlush.FLUSH_KEY);

            for (ServerConnectionWrapper connection : connections) {
                connection.getDuplex().flush();
                Console.showMessage("Flushing server outbound data for client " + connection.getId());
            }
        }

        // outbound data update
        serverCombatEntityManager.update();
        Map<Integer, BasePackable> entities = serverCombatEntityManager.getEntities();

        for (ServerConnectionWrapper connection : connections) {
            serverEntityManager.processDeltas(connection.getDuplex().getDeltas());
        }

        //inbound data update
        serverEntityManager.updateEntities();

        for (ServerConnectionWrapper connection : connections) {
            connection.getDuplex().updateOutbound(entities);
        }
    }

    public ServerConnectionWrapper getNewConnection() {
        ServerConnectionWrapper connectionManager = new ServerConnectionWrapper(getNewInstanceID());
        connections.add(connectionManager);

        return connectionManager;
    }

    public void removeConnection(ServerConnectionWrapper connection) {
        connections.remove(connection);
    }

    public int getNewInstanceID(ShipAPI ship) {
        int id = ship.getFleetMemberId().hashCode();

        while (usedIDs.contains(id)) {
            logger.warn("Attempted to provide new instance with historic ID: is ShipAPI String ID the same?");
            nextInstanceID++;
            id = nextInstanceID;
        }

        usedIDs.add(id);
        return id;
    }

    public int getNewInstanceID() {
        int id;
        nextInstanceID++;
        id = nextInstanceID;

        while (usedIDs.contains(id)) {
            logger.warn("Attempted to provide new instance with historic ID: hashcode collision?");
            nextInstanceID++;
            id = nextInstanceID;
        }

        usedIDs.add(id);
        return id;
    }
}