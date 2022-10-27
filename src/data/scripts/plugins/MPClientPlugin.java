package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.client.*;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.data.util.VariantDataGenerator;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.net.io.ClientConnectionWrapper;
import data.scripts.net.io.Clock;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map;

public class MPClientPlugin extends MPPlugin {

    //inbound
    private final ClientConnectionWrapper connection;
    private final ClientShipTable shipTable;
    private final VariantDataMap variantDataMap;
    private final LobbyInput lobbyInput;

    //outbound
    private final PlayerOutput playerOutput;
    private final PlayerShipOutput playerShipOutput;

    private final VariantDataGenerator dataStore;

    public MPClientPlugin(String host, int port) {
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            Global.getCombatEngine().removeEntity(ship);
        }

        dataStore = new VariantDataGenerator();

        connection = new ClientConnectionWrapper(host, port, this);

        Clock msgClock = new Clock(3);
        while (connection.getConnectionID() == BaseConnectionWrapper.DEFAULT_CONNECTION_ID) {
            if (msgClock.mark()) {
                System.out.println("waiting for connection...");
            }
        }

        initEntityManager(connection);

        // inbound init
        shipTable = new ClientShipTable();
        initEntityManager(shipTable);

        variantDataMap = new VariantDataMap();
        initEntityManager(variantDataMap);

        lobbyInput = new LobbyInput();
        initEntityManager(lobbyInput);

        // outbound init
        int connectionID = connection.getConnectionID();

        playerShipOutput = new PlayerShipOutput(connectionID);
        initEntityManager(playerShipOutput);

        playerOutput = new PlayerOutput(connectionID, this);
        initEntityManager(playerOutput);
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
        Map<Integer, Map<Integer, BasePackable>> outboundSocket = DataGenManager.collectOutboundDeltasSocket();
        connection.getDuplex().updateOutboundSocket(outboundSocket);

        Map<Integer, Map<Integer, BasePackable>> outboundDatagram = DataGenManager.collectOutboundDeltasDatagram();
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

    public PlayerOutput getPlayerOutput() {
        return playerOutput;
    }

    public LobbyInput getLobbyInput() {
        return lobbyInput;
    }

    @Override
    public PluginType getType() {
        return PluginType.CLIENT;
    }
}