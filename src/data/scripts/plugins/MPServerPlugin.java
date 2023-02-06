package data.scripts.plugins;

import cmu.CMUtils;
import cmu.plugins.GUIDebug;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.DataGenManager;
import data.scripts.net.data.InboundData;
import data.scripts.net.data.OutboundData;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.pregen.ProjectileSpecDatastore;
import data.scripts.net.data.pregen.VariantDataGenerator;
import data.scripts.net.data.records.DataRecord;
import data.scripts.net.data.records.Float32Record;
import data.scripts.net.data.records.collections.SyncingListRecord;
import data.scripts.net.data.tables.server.PlayerLobby;
import data.scripts.net.data.tables.server.PlayerShips;
import data.scripts.net.data.tables.server.ProjectileTable;
import data.scripts.net.data.tables.server.ShipTable;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.net.io.ServerConnectionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;

public class MPServerPlugin extends MPPlugin {

    //inbound
    private final ServerConnectionManager serverConnectionManager;
    private final PlayerLobby playerLobby;
    private final PlayerShips playerShips;

    //outbound
    private final ShipTable shipTable;
    private final ProjectileTable projectileTable;

    private final VariantDataGenerator variantDatastore;
    private final ProjectileSpecDatastore projectileSpecDatastore;

    public MPServerPlugin(int port) {
        variantDatastore = new VariantDataGenerator();
        variantDatastore.generate(this);

        projectileSpecDatastore = new ProjectileSpecDatastore();
        projectileSpecDatastore.generate(this);

        serverConnectionManager = new ServerConnectionManager(this, port);

        // inbound init
        playerShips = new PlayerShips();
        initEntityManager(playerShips);

        playerLobby = new PlayerLobby(this);
        initEntityManager(playerLobby);

        //outbound init
        shipTable = new ShipTable();
        initEntityManager(shipTable);

        projectileTable = new ProjectileTable(projectileSpecDatastore.getGeneratedIDs(), shipTable);
        initEntityManager(projectileTable);

        Thread serverThread = new Thread(serverConnectionManager, "MP_SERVER_THREAD");
        serverThread.start();

        testInit();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        test();

        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            serverConnectionManager.stop();
            Global.getCombatEngine().removePlugin(this);
            Console.showMessage("Closed server");
        }

        // inbound data update
        InboundData inbound = serverConnectionManager.getDuplex().getDeltas();
        DataGenManager.distributeInboundDeltas(inbound, this, serverConnectionManager.getTick());

        // simulation update
        updateEntityManagers(amount);

        // outbound data update
        OutboundData outboundSocket = DataGenManager.collectOutboundDeltasSocket();
        serverConnectionManager.getDuplex().updateOutboundSocket(outboundSocket);
        OutboundData outboundDatagram = DataGenManager.collectOutboundDeltasDatagram();
        serverConnectionManager.getDuplex().updateOutboundDatagram(outboundDatagram);

        debug();
    }

    private void debug() {
        GUIDebug guiDebug = CMUtils.getGuiDebug();

        guiDebug.putText(MPServerPlugin.class, "clients", serverConnectionManager.getServerConnectionWrappers().size() + " remote clients connected");
        guiDebug.putText(MPServerPlugin.class, "shipCount", "tracking " + shipTable.getRegistered().size() + " ships in local table");
        guiDebug.putText(MPServerPlugin.class, "tick", "current server tick " + serverConnectionManager.getTick() + " @ " + ServerConnectionManager.TICK_RATE + "Hz");
    }

    Map<Byte, Map<Short, Map<Byte, DataRecord<?>>>> m;
    float f = 0f;

    private void testInit() {
        SyncingListRecord<Float> syncingListRecord = new SyncingListRecord<>(new ArrayList<Float>(), Float32Record.TYPE_ID);
        syncingListRecord.sourceExecute(new SourceExecute<List<Float>>() {
            @Override
            public List<Float> get() {
                List<Float> data = new ArrayList<>();
                data.add(1f);
                data.add(10f);
                data.add(53423f);
                data.add(2321f);
                return data;
            }
        });
        Map<Byte, DataRecord<?>> t = new HashMap<>();
        t.put((byte) 3, syncingListRecord);
        Map<Short, Map<Byte, DataRecord<?>>> i = new HashMap<>();
        i.put((short) 10, t);
        m = new HashMap<>();
        m.put((byte) 69, i);
    }

    private void test() {
        f += 10f;

        SyncingListRecord<Float> syncingListRecord = (SyncingListRecord<Float>) m.get((byte) 69).get((short) 10).get((byte) 3);
        syncingListRecord.sourceExecute(new SourceExecute<List<Float>>() {
            @Override
            public List<Float> get() {
                List<Float> data = new ArrayList<>();
                data.add(1f);
                data.add(10f);
                data.add(53423f);
                data.add(2321f);
                data.add(f);
                return data;
            }
        });

        ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer();
        BaseConnectionWrapper.writeBuffer(new OutboundData(m, new HashMap<Byte, Set<Short>>()), buf);
        InboundData output;
        try {
            output = BaseConnectionWrapper.readBuffer(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        buf.release();
    }

    public VariantDataGenerator getVariantStore() {
        return variantDatastore;
    }

    @Override
    public PluginType getType() {
        return PluginType.SERVER;
    }

    public ShipTable getServerShipTable() {
        return shipTable;
    }

    public PlayerLobby getPlayerMap() {
        return playerLobby;
    }

    public PlayerShips getPlayerShipMap() {
        return playerShips;
    }
}