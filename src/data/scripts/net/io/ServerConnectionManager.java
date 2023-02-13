package data.scripts.net.io;

import com.fs.starfarer.api.Global;
import data.scripts.net.io.tcp.server.SocketServer;
import data.scripts.net.io.udp.server.DatagramServer;
import data.scripts.plugins.MPServerPlugin;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerConnectionManager implements Runnable {
    public static final int MP_MAX_CONNECTIONS = Global.getSettings().getInt("mpMaxConnections");

//    public final static int PORT = Global.getSettings().getInt("mpLocalPortTCP");
    public static final int TICK_RATE = Global.getSettings().getInt("mpServerTickRate");

    private final ServerDuplex duplex;
    private final MPServerPlugin serverPlugin;
    private boolean active;

    private final DatagramServer datagramServer;
    private final Thread datagram;

    private final SocketServer socketServer;
    private final Thread socket;

    private final Map<Byte, ServerConnectionWrapper> serverConnectionWrappers;
    private byte inc = 0;

    private int tick;
    private final Clock clock;

    public ServerConnectionManager(MPServerPlugin serverPlugin, int port) {
        this.serverPlugin = serverPlugin;
        duplex = new ServerDuplex();
        active = true;

        serverConnectionWrappers = new HashMap<>();

        socketServer = new SocketServer(port, this);
        socket = new Thread(socketServer, "SOCKET_SERVER_THREAD");

        datagramServer = new DatagramServer(port, this);
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
                tick++;

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

    public List<MessageContainer> getSocketMessages() throws IOException {
        List<MessageContainer> output = new ArrayList<>();

        for (ServerConnectionWrapper connection : serverConnectionWrappers.values()) {
            List<MessageContainer> messages = connection.getSocketMessages();
            if (messages != null) output.addAll(messages);
        }

        return output;
    }

    public List<MessageContainer> getDatagramMessages() throws IOException {
        List<MessageContainer> output = new ArrayList<>();

        for (ServerConnectionWrapper connection : serverConnectionWrappers.values()) {
            List<MessageContainer> messages = connection.getDatagrams();
            if (messages != null) output.addAll(messages);
        }

        return output;
    }

    public ServerConnectionWrapper getConnection(byte connectionID) {
        for (byte id : serverConnectionWrappers.keySet()) {
            ServerConnectionWrapper wrapper = serverConnectionWrappers.get(id);
            if (id == connectionID) {
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

        byte id = inc;
        ServerConnectionWrapper serverConnectionWrapper = new ServerConnectionWrapper(this, id, remoteAddress, serverPlugin);
        inc++;

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

    public synchronized int getTick() {
        return tick;
    }

    public synchronized ServerDuplex getDuplex() {
        return duplex;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public MPServerPlugin getServerPlugin() {
        return serverPlugin;
    }

    public Map<Byte, ServerConnectionWrapper> getServerConnectionWrappers() {
        return serverConnectionWrappers;
    }
}
