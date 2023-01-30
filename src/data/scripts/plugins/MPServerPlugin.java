package data.scripts.plugins;

import cmu.CMUtils;
import cmu.plugins.GUIDebug;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.records.Float32Record;
import data.scripts.net.data.records.collections.ListRecord;
import data.scripts.net.data.tables.server.HostShipTable;
import data.scripts.net.data.tables.server.PlayerMap;
import data.scripts.net.data.tables.server.PlayerShipMap;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.data.util.VariantDataGenerator;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.net.io.ServerConnectionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.lazywizard.console.Console;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MPServerPlugin extends MPPlugin {

    //inbound
    private final ServerConnectionManager serverConnectionManager;
    private final PlayerMap playerMap;
    private final PlayerShipMap playerShipMap;

    //outbound
    private final HostShipTable hostShipTable;

    private final VariantDataGenerator dataStore;

    public MPServerPlugin(int port) {
        dataStore = new VariantDataGenerator();
        dataStore.generate(Global.getCombatEngine());

        serverConnectionManager = new ServerConnectionManager(this, port);

        // inbound init
        playerShipMap = new PlayerShipMap();
        initEntityManager(playerShipMap);

        playerMap = new PlayerMap(this);
        initEntityManager(playerMap);

        //outbound init
        hostShipTable = new HostShipTable();
        initEntityManager(hostShipTable);

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

        ListRecord<Float> listRecord = new ListRecord<>(new ArrayList<Float>(), Float32Record.TYPE_ID);
        listRecord.sourceExecute(new SourceExecute<List<Float>>() {
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
        Map<Byte, BaseRecord<?>> t = new HashMap<>();
        t.put((byte) 3, listRecord);
        Map<Short, Map<Byte, BaseRecord<?>>> i = new HashMap<>();
        i.put((short) 10, t);
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> m = new HashMap<>();
        m.put((byte) 69, i);
        ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer();
        BaseConnectionWrapper.writeBuffer(m, buf);
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> output;
        try {
            output = BaseConnectionWrapper.readBuffer(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        buf.release();

        // inbound data update
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> inbound = serverConnectionManager.getDuplex().getDeltas();
        DataGenManager.distributeInboundDeltas(inbound, this);

        // simulation update
        updateEntityManagers(amount);

        // outbound data update
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outboundSocket = DataGenManager.collectOutboundDeltasSocket();
        serverConnectionManager.getDuplex().updateOutboundSocket(outboundSocket);
        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> outboundDatagram = DataGenManager.collectOutboundDeltasDatagram();
        serverConnectionManager.getDuplex().updateOutboundDatagram(outboundDatagram);

        debug();
    }

    private void debug() {
        GUIDebug guiDebug = CMUtils.getGuiDebug();

        guiDebug.putText(MPServerPlugin.class, "clients", serverConnectionManager.getServerConnectionWrappers().size() + " remote clients connected");
        guiDebug.putText(MPServerPlugin.class, "shipCount", "tracking " + hostShipTable.getRegistered().size() + " ships in local table");
        guiDebug.putText(MPServerPlugin.class, "tick", "current server tick " + serverConnectionManager.getTick() + " @ " + ServerConnectionManager.TICK_RATE + "Hz");
    }

    public VariantDataGenerator getVariantStore() {
        return dataStore;
    }

    @Override
    public PluginType getType() {
        return PluginType.SERVER;
    }

    public HostShipTable getServerShipTable() {
        return hostShipTable;
    }

    public PlayerMap getPlayerMap() {
        return playerMap;
    }

    public PlayerShipMap getPlayerShipMap() {
        return playerShipMap;
    }
}