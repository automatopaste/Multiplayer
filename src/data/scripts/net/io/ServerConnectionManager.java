package data.scripts.net.io;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.BasePackable;
import data.scripts.net.data.packables.metadata.connection.ConnectionIDs;
import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.tables.OutboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.io.tcp.server.SocketServer;
import data.scripts.net.io.udp.server.DatagramServer;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.MPServerPlugin;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerConnectionManager implements Runnable, InboundEntityManager, OutboundEntityManager {
    public static final int MP_MAX_CONNECTIONS = Global.getSettings().getInt("mpMaxConnections");

    public final static int PORT = Global.getSettings().getInt("mpLocalPortTCP");
    public static final int TICK_RATE = Global.getSettings().getInt("mpServerTickRate");

    private final DataDuplex dataDuplex;
    private final MPServerPlugin serverPlugin;
    private boolean active;

    private final DatagramServer datagramServer;
    private final Thread datagram;

    private final SocketServer socketServer;
    private final Thread socket;

    private final Map<Short, ServerConnectionWrapper> serverConnectionWrappers;

    private int tick;
    private final Clock clock;

    public ServerConnectionManager(MPServerPlugin serverPlugin) {
        this.serverPlugin = serverPlugin;
        dataDuplex = new DataDuplex();
        active = true;

        serverConnectionWrappers = new HashMap<>();

        socketServer = new SocketServer(PORT, this);
        socket = new Thread(socketServer, "SOCKET_SERVER_THREAD");

        datagramServer = new DatagramServer(PORT, this);
        datagram = new Thread(datagramServer, "DATAGRAM_SERVER_THREAD");

        tick = 0;
        clock = new Clock(TICK_RATE);

    }

    @Override
    public void run() {
        Console.showMessage("Starting main server...");

        socket.start();
        datagram.start();

        try {
            while (active) {
                clock.sleepUntilTick();
                tickUpdate();

                List<MessageContainer> socketMessages = getSocketMessages();
                if (!socketMessages.isEmpty()) socketServer.addMessages(socketMessages);

                List<MessageContainer> datagramMessages = getDatagramMessages();
                if (!datagramMessages.isEmpty()) datagramServer.addMessages(datagramMessages);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void tickUpdate() {
        tick++;
    }

    public List<MessageContainer> getSocketMessages() throws IOException {
        List<MessageContainer> output = new ArrayList<>();

        for (ServerConnectionWrapper connection : serverConnectionWrappers.values()) {
            MessageContainer message = connection.getSocketMessage();
            if (message != null) output.add(message);
        }

        return output;
    }

    public List<MessageContainer> getDatagramMessages() throws IOException {
        List<MessageContainer> output = new ArrayList<>();

        for (ServerConnectionWrapper connection : serverConnectionWrappers.values()) {
            MessageContainer message = connection.getDatagram();
            if (message != null) output.add(message);
        }

        return output;
    }

    public ServerConnectionWrapper getConnection(int connectionID) {
        for (Short id : serverConnectionWrappers.keySet()) {
            ServerConnectionWrapper wrapper = serverConnectionWrappers.get(id);
            if (wrapper.connectionID == connectionID) {
                return wrapper;
            }
        }
        return null;
    }

    public ServerConnectionWrapper getNewConnection(InetSocketAddress remoteAddress) {
        if (remoteAddress == null) return null;

        synchronized (serverConnectionWrappers) {
            if (serverConnectionWrappers.size() >= MP_MAX_CONNECTIONS) return null;
        }

        short id = ConnectionIDs.getConnectionID(remoteAddress);
        ServerConnectionWrapper serverConnectionWrapper = new ServerConnectionWrapper(this, id, remoteAddress, serverPlugin);

        synchronized (serverConnectionWrappers) {
            serverConnectionWrappers.put(id, serverConnectionWrapper);
        }

        return serverConnectionWrapper;
    }

    public void removeConnection(short id) {
        synchronized (serverConnectionWrappers) {
            serverConnectionWrappers.remove(id);
        }
    }

    public void stop() {
        active = false;

        socketServer.stop();
        datagramServer.stop();
        socket.interrupt();
        datagram.interrupt();

        System.out.println("STOPPING PRIMARY SERVER THREAD");
    }

    @Override
    public void processDelta(short instanceID, Map<Byte, BaseRecord<?>> toProcess, MPPlugin plugin) {
        ServerConnectionWrapper wrapper = serverConnectionWrappers.get(instanceID);

        if (wrapper != null) {
            wrapper.updateConnectionStatus(toProcess);
        }
    }

    @Override
    public void update(float amount) {

    }

    public synchronized int getTick() {
        return tick;
    }

    public synchronized DataDuplex getDuplex() {
        return dataDuplex;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public MPServerPlugin getServerPlugin() {
        return serverPlugin;
    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ConnectionIDs.TYPE_ID, this);
    }

    public Map<Short, ServerConnectionWrapper> getServerConnectionWrappers() {
        return serverConnectionWrappers;
    }

    @Override
    public Map<Short, BasePackable> getOutbound() {
        Map<Short, BasePackable> out = new HashMap<>();

        for (short id : serverConnectionWrappers.keySet()) {
            out.put(id, serverConnectionWrappers.get(id).statusData);
        }

        return out;
    }

    @Override
    public PacketType getOutboundPacketType() {
        return PacketType.SOCKET;
    }
}
