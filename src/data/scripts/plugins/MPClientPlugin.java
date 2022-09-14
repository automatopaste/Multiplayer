package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.SourcePackable;
import data.scripts.net.data.packables.metadata.pilot.PilotSource;
import data.scripts.net.data.tables.client.ClientShipTable;
import data.scripts.net.data.tables.client.PilotCommandOutput;
import data.scripts.net.data.tables.client.VariantDataMap;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.data.util.VariantDataGenerator;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.net.io.ClientConnectionWrapper;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map;

public class MPClientPlugin extends MPPlugin {

    //inbound
    private final ClientConnectionWrapper connection;
    private final ClientShipTable shipTable;
    private final VariantDataMap variantDataMap;

    //outbound
    private final PilotCommandOutput inputManager;

    private final VariantDataGenerator dataStore;

    public MPClientPlugin(String host, int port) {
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            Global.getCombatEngine().removeEntity(ship);
        }

        dataStore = new VariantDataGenerator();

        connection = new ClientConnectionWrapper(host, port, this);
        initEntityManager(connection);

        // inbound init
        shipTable = new ClientShipTable();
        initEntityManager(shipTable);

        variantDataMap = new VariantDataMap();
        initEntityManager(variantDataMap);

        // outbound init
        int id = -10; // placeholder id
        inputManager = new PilotCommandOutput(id, new PilotSource(id));
        initEntityManager(inputManager);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (connection.getConnectionState() == BaseConnectionWrapper.ConnectionState.CLOSED) {
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Server interrupted");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            connection.stop();
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Closed client");
        }

        // get inbound
        Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> entities = connection.getDuplex().getDeltas();
        DataGenManager.distributeInboundDeltas(entities, this);

        updateEntityManagers(amount);

        // outbound data update
        Map<Integer, Map<Integer, SourcePackable>> outboundSocket = DataGenManager.collectOutboundDeltasSocket();
        connection.getDuplex().updateOutboundSocket(outboundSocket);

        Map<Integer, Map<Integer, SourcePackable>> outboundDatagram = DataGenManager.collectOutboundDeltasDatagram();
        connection.getDuplex().updateOutboundDatagram(outboundDatagram);
    }

    public ClientConnectionWrapper getConnection() {
        return connection;
    }

    public VariantDataGenerator getDataStore() {
        return dataStore;
    }

    public ClientShipTable getShipTable() {
        return shipTable;
    }

    public VariantDataMap getVariantDataMap() {
        return variantDataMap;
    }

    @Override
    public PluginType getType() {
        return PluginType.CLIENT;
    }
}