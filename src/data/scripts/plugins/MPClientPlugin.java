package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.client.*;
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
    private LobbyInput lobbyInput;
    private PlayerShip playerShip;

    //outbound
    private Player player;

    private final VariantDataGenerator dataStore;

    public MPClientPlugin(String host, int port) {
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            Global.getCombatEngine().removeEntity(ship);
        }

        dataStore = new VariantDataGenerator();

        connection = new ClientConnectionWrapper(host, port, this);

        // inbound init
        shipTable = new ClientShipTable();
        initEntityManager(shipTable);

        variantDataMap = new VariantDataMap();
        initEntityManager(variantDataMap);

        // outbound init
        playerShip = new PlayerShip(connection.getConnectionID());
        initEntityManager(playerShip);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (connection.getConnectionID() == ClientConnectionWrapper.DEFAULT_CONNECTION_ID) {
            return;
        }

        if (connection.getConnectionState() == BaseConnectionWrapper.ConnectionState.CLOSED) {
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Connection interrupted");
            return;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            connection.stop();
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Closed client");
            return;
        }

        if (lobbyInput == null) {
            lobbyInput = new LobbyInput(connection.getConnectionID());
            initEntityManager(lobbyInput);
        }
        if (player == null) {
            player = new Player(connection.getConnectionID(), this);
            initEntityManager(player);
        }

        // get inbound
        Map<Byte, Map<Short, Map<Byte, Object>>> entities = connection.getDuplex().getDeltas();
        DataGenManager.distributeInboundDeltas(entities, this, connection.getTick());

        // update
        updateEntityManagers(amount);

        // outbound data update
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outboundSocket = DataGenManager.collectOutboundDeltasSocket();
        connection.getDuplex().updateOutboundSocket(outboundSocket);
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outboundDatagram = DataGenManager.collectOutboundDeltasDatagram();
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

    public Player getPlayerOutput() {
        return player;
    }

    public PlayerShip getPlayerShip() {
        return playerShip;
    }

    public LobbyInput getLobbyInput() {
        return lobbyInput;
    }

    @Override
    public PluginType getType() {
        return PluginType.CLIENT;
    }
}