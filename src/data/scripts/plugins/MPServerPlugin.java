package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.entities.ShipData;
import data.scripts.net.data.packables.entities.VariantData;
import data.scripts.net.data.tables.server.ServerPilotCommandMap;
import data.scripts.net.data.tables.server.ServerShipTable;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.data.util.VariantDataGenerator;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.ServerConnectionManager;
import data.scripts.net.io.UnpackAlgorithm;
import data.scripts.net.io.Unpacked;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
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
        serverConnectionManager.register();

        // inbound init
        serverPilotCommandMap = new ServerPilotCommandMap();
        serverPilotCommandMap.register();

        //outbound init
        serverShipTable = new ServerShipTable();
        serverShipTable.register();

        Thread serverThread = new Thread(serverConnectionManager, "MP_SERVER_THREAD");
        serverThread.start();

        ShipData data = new ShipData(69, Global.getCombatEngine().getShips().get(0));
        VariantData variant = new VariantData(420, data.getShip().getVariant(), "ID_LOL_LMAO");
        PacketContainer p;
        try {
            p = new PacketContainer(Arrays.asList(data, variant), 4200, false, new InetSocketAddress("0", 0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Unpacked u = UnpackAlgorithm.unpack(p.get(), p.getDest(), p.getDest());
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
}