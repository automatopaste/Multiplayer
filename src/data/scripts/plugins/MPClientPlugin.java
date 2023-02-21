package data.scripts.plugins;

import cmu.CMUtils;
import cmu.plugins.debug.DebugGraphContainer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InboundData;
import data.scripts.net.data.OutboundData;
import data.scripts.net.data.datagen.ProjectileSpecDatastore;
import data.scripts.net.data.tables.client.*;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.net.io.ClientConnectionWrapper;
import data.scripts.plugins.gui.MPChatboxPlugin;
import org.lazywizard.console.Console;

import java.util.List;

public class MPClientPlugin extends MPPlugin {

    private final String host;
    private final int port;

    private boolean init = false;

    private final MPChatboxPlugin chatboxPlugin;

    private ClientConnectionWrapper connection;
    private ClientShipTable shipTable;
    private ClientProjectileTable projectileTable;
    private VariantDataMap variantDataMap;
    private LobbyInput lobbyInput;
    private PlayerShip playerShip;
    private TextChatClient textChatClient;
    private Player player;

    private ProjectileSpecDatastore projectileSpecDatastore;

    //debug
    private DebugGraphContainer dataGraph;
//    private DebugGraphContainer dataGraph2;

    public MPClientPlugin(String host, int port) {
        this.host = host;
        this.port = port;

        CombatEngineAPI engine = Global.getCombatEngine();

        chatboxPlugin = new MPChatboxPlugin();
        engine.addPlugin(chatboxPlugin);

        connection = new ClientConnectionWrapper(host, port, this);
    }

    public void init() {
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            Global.getCombatEngine().removeEntity(ship);
        }

        projectileSpecDatastore = new ProjectileSpecDatastore();
        initDatastore(projectileSpecDatastore);

        shipTable = new ClientShipTable();
        initEntityManager(shipTable);

        projectileTable = new ClientProjectileTable();
        initEntityManager(projectileTable);

        variantDataMap = new VariantDataMap();
        initEntityManager(variantDataMap);

        playerShip = new PlayerShip(connection.getConnectionID());
        initEntityManager(playerShip);

        lobbyInput = new LobbyInput();
        initEntityManager(lobbyInput);

        player = new Player(connection.getConnectionID(), this);
        initEntityManager(player);

        textChatClient = new TextChatClient(connection.getConnectionID(), connection.getConnectionID(), chatboxPlugin, lobbyInput);
        initEntityManager(textChatClient);

        dataGraph = new DebugGraphContainer("Inbound Packet Size", 120, 30f);
//        dataGraph2 = new DebugGraphContainer("Inbound Packet Count", 120, 30f);

        init = true;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (!init) {
            return;
        }

        if (connection.getConnectionState() == BaseConnectionWrapper.ConnectionState.CLOSED) {
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Connection interrupted");
            return;
        }

        // get inbound
        InboundData entities = connection.getDuplex().getDeltas();
        dataGraph.increment(entities.size);
//        dataGraph2.increment(connection.getDuplex().getNumSinceTick());
        CMUtils.getGuiDebug().putContainer(MPClientPlugin.class, "dataGraph", dataGraph);
//        CMUtils.getGuiDebug().putContainer(MPClientPlugin.class, "dataGraph2", dataGraph2);

        DataGenManager.distributeInboundDeltas(entities, this, connection.getTick(), connection.getConnectionID());

        // update
        updateEntityManagers(amount);

        // outbound data update
        OutboundData outboundSocket = DataGenManager.collectOutboundDeltasSocket(amount, connection.getConnectionID());
        connection.getDuplex().updateOutboundSocket(outboundSocket);
        OutboundData outboundDatagram = DataGenManager.collectOutboundDeltasDatagram(amount, connection.getConnectionID());
        connection.getDuplex().updateOutboundDatagram(outboundDatagram);
    }

    @Override
    public void stop() {
        connection.stop();
        Global.getCombatEngine().removePlugin(this);
        Console.showMessage("Closed client");
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