package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.tables.server.ServerPilotCommandMap;
import data.scripts.net.data.tables.server.ServerShipTable;
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
    private final ServerPilotCommandMap serverPilotCommandMap;

    //outbound
    private final ServerShipTable serverShipTable;

    private final VariantDataGenerator dataStore;

    public MPServerPlugin() {
        dataStore = new VariantDataGenerator();
        dataStore.generate(Global.getCombatEngine(), this);

        serverConnectionManager = new ServerConnectionManager(this);
        serverConnectionManager.register();

        // inbound init
        serverPilotCommandMap = new ServerPilotCommandMap();
        serverPilotCommandMap.register();

        //outbound init
        serverShipTable = new ServerShipTable();
        serverShipTable.register();

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
        Map<Integer, Map<Integer, BasePackable>> inbound = serverConnectionManager.getDuplex().getDeltas();
        DataGenManager.distributeInboundDeltas(inbound, this);

        // simulation update
        serverShipTable.update();

        // outbound data update
        Map<Integer, Map<Integer, BasePackable>> outbound = DataGenManager.collectOutboundDeltas();
        serverConnectionManager.getDuplex().updateOutbound(outbound);
    }

    public VariantDataGenerator getDataStore() {
        return dataStore;
    }

    @Override
    public PluginType getType() {
        return PluginType.SERVER;
    }

    public ServerShipTable getServerShipTable() {
        return serverShipTable;
    }

    public ServerPilotCommandMap getServerPilotCommandMap() {
        return serverPilotCommandMap;
    }
}