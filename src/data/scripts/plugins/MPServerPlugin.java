package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.data.DataGenManager;
import data.scripts.data.LoadedDataStore;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.trans.InputAggregateData;
import data.scripts.net.data.tables.server.ServerPilotCommandManager;
import data.scripts.net.data.tables.server.ServerShipTable;
import data.scripts.net.io.ServerConnectionManager;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MPServerPlugin extends BaseEveryFrameCombatPlugin implements MPPlugin {
    private final ServerConnectionManager serverConnectionManager;

    //inbound
    private final ServerPilotCommandManager serverPilotCommandManager;
    //outbound
    private final ServerShipTable serverShipTable;

    private int nextInstanceID = 1;
    private final Set<Integer> usedIDs = new HashSet<>();

    private final LoadedDataStore dataStore;

    public MPServerPlugin() {
        // inbound init
        serverPilotCommandManager = new ServerPilotCommandManager();
        DataGenManager.registerEntityManager(InputAggregateData.TYPE_ID, serverPilotCommandManager);

        //outbound init
        serverShipTable = new ServerShipTable();

        dataStore = new LoadedDataStore();
        dataStore.generate(Global.getCombatEngine(), this);

        serverConnectionManager = new ServerConnectionManager(this);
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

    public LoadedDataStore getDataStore() {
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