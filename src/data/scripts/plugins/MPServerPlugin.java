package data.scripts.plugins;

import cmu.CMUtils;
import cmu.plugins.GUIDebug;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InboundData;
import data.scripts.net.data.OutboundData;
import data.scripts.net.data.pregen.ProjectileSpecDatastore;
import data.scripts.net.data.pregen.VariantDataGenerator;
import data.scripts.net.data.tables.server.PlayerLobby;
import data.scripts.net.data.tables.server.PlayerShips;
import data.scripts.net.data.tables.server.ProjectileTable;
import data.scripts.net.data.tables.server.ShipTable;
import data.scripts.net.io.ServerConnectionManager;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MPServerPlugin extends MPPlugin {

    //inbound
    private final ServerConnectionManager serverConnectionManager;
    private final PlayerLobby playerLobby;
    private final PlayerShips playerShips;

    //outbound
    private final ShipTable shipTable;
    private final ProjectileTable projectileTable;

    private final VariantDataGenerator variantDatastore;
    private final ProjectileSpecDatastore projectileSpecDatastore;

    public MPServerPlugin(int port) {
        variantDatastore = new VariantDataGenerator();
        initDatastore(variantDatastore);

        projectileSpecDatastore = new ProjectileSpecDatastore();
        initDatastore(projectileSpecDatastore);

        serverConnectionManager = new ServerConnectionManager(this, port);

        playerLobby = new PlayerLobby(this);
        initEntityManager(playerLobby);

        shipTable = new ShipTable();
        initEntityManager(shipTable);

        playerShips = new PlayerShips();
        initEntityManager(playerShips);

        projectileTable = new ProjectileTable(projectileSpecDatastore.getGeneratedIDs(), shipTable);
        initEntityManager(projectileTable);

        Thread serverThread = new Thread(serverConnectionManager, "MP_SERVER_THREAD");
        serverThread.start();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            serverConnectionManager.stop();
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Closed server");
        }

        // inbound data update
        InboundData inbound = serverConnectionManager.getDuplex().getDeltas();
        DataGenManager.distributeInboundDeltas(inbound, this, serverConnectionManager.getTick());

        // simulation update
        updateEntityManagers(amount);

        // outbound data update
        OutboundData outboundSocket = DataGenManager.collectOutboundDeltasSocket(amount);
        serverConnectionManager.getDuplex().updateOutboundSocket(outboundSocket);
        OutboundData outboundDatagram = DataGenManager.collectOutboundDeltasDatagram(amount);
        serverConnectionManager.getDuplex().updateOutboundDatagram(outboundDatagram);

        debug();
    }

    private void debug() {
        GUIDebug guiDebug = CMUtils.getGuiDebug();

        guiDebug.putText(MPServerPlugin.class, "clients", serverConnectionManager.getServerConnectionWrappers().size() + " remote clients connected");
        guiDebug.putText(MPServerPlugin.class, "shipCount", "tracking " + shipTable.getRegistered().size() + " ships in local table");
        guiDebug.putText(MPServerPlugin.class, "tick", "current server tick " + serverConnectionManager.getTick() + " @ " + ServerConnectionManager.TICK_RATE + "Hz");
    }

    public VariantDataGenerator getVariantStore() {
        return variantDatastore;
    }

    @Override
    public PluginType getType() {
        return PluginType.SERVER;
    }

    public ShipTable getServerShipTable() {
        return shipTable;
    }

    public ProjectileTable getProjectileTable() {
        return projectileTable;
    }

    public PlayerLobby getPlayerMap() {
        return playerLobby;
    }

    public PlayerShips getPlayerShipMap() {
        return playerShips;
    }
}