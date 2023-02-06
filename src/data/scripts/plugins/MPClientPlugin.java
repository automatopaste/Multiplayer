package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InboundData;
import data.scripts.net.data.OutboundData;
import data.scripts.net.data.pregen.ProjectileSpecDatastore;
import data.scripts.net.data.tables.client.*;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.net.io.ClientConnectionWrapper;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MPClientPlugin extends MPPlugin {

    //inbound
    private final ClientConnectionWrapper connection;
    private final ClientShipTable shipTable;
    private final ClientProjectileTable projectileTable;
    private final VariantDataMap variantDataMap;
    private LobbyInput lobbyInput;
    private final PlayerShip playerShip;

    //outbound
    private Player player;

    private final ProjectileSpecDatastore projectileSpecDatastore;

    public MPClientPlugin(String host, int port) {
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            Global.getCombatEngine().removeEntity(ship);
        }

        projectileSpecDatastore = new ProjectileSpecDatastore();
        projectileSpecDatastore.generate(this);

        connection = new ClientConnectionWrapper(host, port, this);

        // inbound init
        shipTable = new ClientShipTable();
        initEntityManager(shipTable);

        projectileTable = new ClientProjectileTable();
        initEntityManager(projectileTable);

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
        InboundData entities = connection.getDuplex().getDeltas();
        DataGenManager.distributeInboundDeltas(entities, this, connection.getTick());

        // update
        updateEntityManagers(amount);

        // outbound data update
        OutboundData outboundSocket = DataGenManager.collectOutboundDeltasSocket();
        connection.getDuplex().updateOutboundSocket(outboundSocket);
        OutboundData outboundDatagram = DataGenManager.collectOutboundDeltasDatagram();
        connection.getDuplex().updateOutboundDatagram(outboundDatagram);
    }

    public ClientConnectionWrapper getConnection() {
        return connection;
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