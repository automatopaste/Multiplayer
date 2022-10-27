package data.scripts.plugins;

import cmu.CMUtils;
import cmu.plugins.GUIDebug;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.server.PlayerMap;
import data.scripts.net.data.tables.server.ShipTable;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.data.util.VariantDataGenerator;
import data.scripts.net.io.ServerConnectionManager;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map;

public class MPServerPlugin extends MPPlugin {

    //inbound
    private final ServerConnectionManager serverConnectionManager;
    private final PlayerMap playerMap;

    //outbound
    private final ShipTable shipTable;

    private final VariantDataGenerator dataStore;

    public MPServerPlugin() {
        dataStore = new VariantDataGenerator();
        dataStore.generate(Global.getCombatEngine(), this);

        serverConnectionManager = new ServerConnectionManager(this);
        initEntityManager(serverConnectionManager);

        // inbound init
        playerMap = new PlayerMap(this);
        initEntityManager(playerMap);

        //outbound init
        shipTable = new ShipTable();
        initEntityManager(shipTable);

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
        Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> inbound = serverConnectionManager.getDuplex().getDeltas();
        DataGenManager.distributeInboundDeltas(inbound, this);

        // simulation update
        updateEntityManagers(amount);

        // outbound data update
        Map<Integer, Map<Integer, BasePackable>> outboundSocket = DataGenManager.collectOutboundDeltasSocket();
        serverConnectionManager.getDuplex().updateOutboundSocket(outboundSocket);

        Map<Integer, Map<Integer, BasePackable>> outboundDatagram = DataGenManager.collectOutboundDeltasDatagram();
        serverConnectionManager.getDuplex().updateOutboundDatagram(outboundDatagram);

        debug();
    }

    private void debug() {
        GUIDebug guiDebug = CMUtils.getGuiDebug();

        guiDebug.putText(MPServerPlugin.class, "clients", serverConnectionManager.getServerConnectionWrappers().size() + " remote clients connected");
        guiDebug.putText(MPServerPlugin.class, "shipCount", "tracking " + shipTable.getRegistered().size() + " ships in local table");
        guiDebug.putText(MPServerPlugin.class, "tick", "current server tick " + serverConnectionManager.getTick() + " @ " + ServerConnectionManager.TICK_RATE + "Hz");
    }

    public VariantDataGenerator getDataStore() {
        return dataStore;
    }

    @Override
    public PluginType getType() {
        return PluginType.SERVER;
    }

    public ShipTable getServerShipTable() {
        return shipTable;
    }

    public PlayerMap getPlayerMap() {
        return playerMap;
    }
}