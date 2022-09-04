package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.metadata.ConnectionStatusData;
import data.scripts.net.data.packables.trans.PilotCommandData;
import data.scripts.net.data.tables.server.ServerPilotCommandMap;
import data.scripts.net.data.tables.server.ServerShipTable;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.data.util.VariantDataGenerator;
import data.scripts.net.io.ServerConnectionManager;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map;

public class MPServerPlugin extends BaseEveryFrameCombatPlugin implements MPPlugin {

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

        // inbound init
        serverPilotCommandMap = new ServerPilotCommandMap();
        DataGenManager.registerEntityManager(PilotCommandData.TYPE_ID, serverPilotCommandMap);
        DataGenManager.registerEntityManager(ConnectionStatusData.TYPE_ID, serverConnectionManager);

        //outbound init
        serverShipTable = new ServerShipTable();

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
        Map<Integer, BasePackable> inbound = serverConnectionManager.getDuplex().getDeltas();
        DataGenManager.distributeInboundDeltas(inbound, this);

        // simulation update
        serverShipTable.update();

        // outbound data update
        Map<Integer, BasePackable> entities = serverShipTable.getOutbound();
        serverConnectionManager.getDuplex().updateOutbound(entities);
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
}