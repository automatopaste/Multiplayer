package data.scripts.plugins;

import cmu.CMUtils;
import cmu.plugins.GUIDebug;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InboundData;
import data.scripts.net.data.OutboundData;
import data.scripts.net.data.datagen.FighterVariantDatastore;
import data.scripts.net.data.datagen.ProjectileSpecDatastore;
import data.scripts.net.data.datagen.ShipVariantDatastore;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.tables.server.combat.connection.TextChatHost;
import data.scripts.net.data.tables.server.combat.entities.ProjectileTable;
import data.scripts.net.data.tables.server.combat.entities.ships.ShipTable;
import data.scripts.net.data.tables.server.combat.players.PlayerLobby;
import data.scripts.net.data.tables.server.combat.players.PlayerShips;
import data.scripts.net.io.ServerConnectionManager;
import data.scripts.plugins.gui.MPChatboxPlugin;
import org.lazywizard.console.Console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MPServerPlugin extends MPPlugin {

    //inbound
    private final ServerConnectionManager serverConnectionManager;
    private final PlayerLobby playerLobby;
    private final PlayerShips playerShips;

    //outbound
    private final ShipTable shipTable;
    private final ProjectileTable projectileTable;

    private final ShipVariantDatastore variantDatastore;
    private final ProjectileSpecDatastore projectileSpecDatastore;
    private final FighterVariantDatastore fighterVariantDatastore;

    private final TextChatHost textChatHost;

    public MPServerPlugin(int port) {
        CombatEngineAPI engine = Global.getCombatEngine();

        MPChatboxPlugin chatboxPlugin = new MPChatboxPlugin();
        engine.addPlugin(chatboxPlugin);

        variantDatastore = new ShipVariantDatastore();
        initDatastore(variantDatastore);
        projectileSpecDatastore = new ProjectileSpecDatastore();
        initDatastore(projectileSpecDatastore);
        fighterVariantDatastore = new FighterVariantDatastore();
        initDatastore(fighterVariantDatastore);

        serverConnectionManager = new ServerConnectionManager(this, port);

        shipTable = new ShipTable(this);
        initEntityManager(shipTable);

        playerShips = new PlayerShips(shipTable);
        initEntityManager(playerShips);

        playerLobby = new PlayerLobby(this, playerShips);
        initEntityManager(playerLobby);

        projectileTable = new ProjectileTable(projectileSpecDatastore, shipTable);
        initEntityManager(projectileTable);

        textChatHost = new TextChatHost(chatboxPlugin, playerLobby);
        initEntityManager(textChatHost);

        Thread serverThread = new Thread(serverConnectionManager, "MP_SERVER_THREAD");
        serverThread.start();

        MPLogger.info(MPServerPlugin.class, "INITIALISATION COMPLETE");
        MPLogger.info(MPServerPlugin.class, "WELCOME STARFARER");
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        // inbound data update
        Map<Byte, InboundData> inbound = serverConnectionManager.getDuplex().getDeltas();
        for (byte connectionID : inbound.keySet()) {
            InboundData data = inbound.get(connectionID);
            DataGenManager.distributeInboundDeltas(data, this, serverConnectionManager.getTick(), connectionID);
        }

        CombatEngineAPI engine = Global.getCombatEngine();

        // sorry
        List<CombatEntityAPI> asteroids = new ArrayList<>(engine.getAsteroids());
        for (CombatEntityAPI asteroid : asteroids) {
            engine.removeEntity(asteroid);
        }

        // simulation update
        updateEntityManagers(amount);

        // outbound data update
        List<Byte> connectionIDs = new ArrayList<>(serverConnectionManager.getServerConnectionWrappers().keySet());

        Map<Byte, OutboundData> outboundSocket = DataGenManager.collectOutboundDeltas(amount, connectionIDs, OutboundEntityManager.PacketType.SOCKET);
        Map<Byte, OutboundData> outboundDatagram = DataGenManager.collectOutboundDeltas(amount, connectionIDs, OutboundEntityManager.PacketType.DATAGRAM);

        for (byte connectionID : connectionIDs) {
            serverConnectionManager.getDuplex().updateOutboundSocket(connectionID, outboundSocket.get(connectionID));
            serverConnectionManager.getDuplex().updateOutboundDatagram(connectionID, outboundDatagram.get(connectionID));
        }

        debug();
    }

    @Override
    public void stop() {
        serverConnectionManager.stop();
        Global.getCombatEngine().removePlugin(this);
        Console.showMessage("Closed server");
    }

    private void debug() {
        GUIDebug guiDebug = CMUtils.getGuiDebug();

        guiDebug.putText(MPServerPlugin.class, "clients", serverConnectionManager.getServerConnectionWrappers().size() + " remote clients connected");
        guiDebug.putText(MPServerPlugin.class, "shipCount", "tracking " + shipTable.getRegistered().size() + " ships in local table");
        guiDebug.putText(MPServerPlugin.class, "tick", "current server tick " + serverConnectionManager.getTick() + " @ " + ServerConnectionManager.TICK_RATE + "Hz");
    }

    public ShipVariantDatastore getVariantStore() {
        return variantDatastore;
    }

    public FighterVariantDatastore getFighterVariantDatastore() {
        return fighterVariantDatastore;
    }

    @Override
    public PluginType getType() {
        return PluginType.SERVER;
    }
}