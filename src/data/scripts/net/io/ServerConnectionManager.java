package data.scripts.net.io;

import com.fs.starfarer.api.Global;
import data.scripts.net.io.tcp.server.SocketServer;
import data.scripts.net.io.udp.server.DatagramServer;
import data.scripts.plugins.mpServerPlugin;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class ServerConnectionManager implements Runnable {
    private final int maxConnections = Global.getSettings().getInt("mpMaxConnections");

    public final static int PORT = Global.getSettings().getInt("mpLocalPortTCP");
    public static final int TICK_RATE = Global.getSettings().getInt("mpServerTickRate");

    private final DataDuplex dataDuplex;
    private final mpServerPlugin serverPlugin;
    private boolean active;

    private final DatagramServer datagramServer;
    private final Thread datagram;

    private final SocketServer socketServer;
    private final Thread socket;

    private final Map<InetSocketAddress, ServerConnectionWrapper> serverConnectionWrappers;

    private final Map<Integer, InetSocketAddress> clientAddresses;
    private final Map<Integer, InetSocketAddress> clientDatagramAddresses;

    private int tick;
    private final Clock clock;

    public ServerConnectionManager(mpServerPlugin serverPlugin) {
        this.serverPlugin = serverPlugin;
        dataDuplex = new DataDuplex();
        active = true;

        serverConnectionWrappers = new HashMap<>();

        socketServer = new SocketServer(PORT, this);
        socket = new Thread(socketServer, "SOCKET_SERVER_THREAD");

        datagramServer = new DatagramServer(PORT + 1, this);
        datagram = new Thread(datagramServer, "DATAGRAM_SERVER_THREAD");

        tick = 0;
        clock = new Clock(TICK_RATE);

        clientAddresses = new HashMap<>();
        clientDatagramAddresses = new HashMap<>();
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

                socketServer.queueMessages(getSocketMessages());
                datagramServer.queueMessages(getDatagrams());
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

    public List<PacketContainer> getSocketMessages() throws IOException {
        List<PacketContainer> output = new ArrayList<>();

        for (ServerConnectionWrapper connection : serverConnectionWrappers.values()) {
            PacketContainer message = connection.getSocketMessage();
            if (message != null) output.add(message);
        }

        return output;
    }

    public List<PacketContainer> getDatagrams() throws IOException {
        List<PacketContainer> output = new ArrayList<>();

        for (ServerConnectionWrapper connection : serverConnectionWrappers.values()) {
            PacketContainer message = connection.getDatagram();
            if (message != null) output.add(message);
        }

        return output;
    }

    public ServerConnectionWrapper getConnection(InetSocketAddress remoteAddress) {
        for (InetSocketAddress address : serverConnectionWrappers.keySet()) {
            if (Arrays.equals(address.getAddress().getAddress(), remoteAddress.getAddress().getAddress())) {
                return serverConnectionWrappers.get(address);
            }
        }
        return null;
    }

    public ServerConnectionWrapper getNewConnection(InetSocketAddress remoteAddress) {
        if (remoteAddress == null) return null;

        synchronized (serverConnectionWrappers) {
            if (serverConnectionWrappers.size() >= maxConnections) return null;
        }
        int id = serverPlugin.getNewInstanceID();
        ServerConnectionWrapper serverConnectionWrapper = new ServerConnectionWrapper(this, id);

        clientAddresses.put(id, remoteAddress);
        clientAddresses.put(id, new InetSocketAddress(remoteAddress.getHostName(), remoteAddress.getPort() + 1));

        synchronized (serverConnectionWrappers) {
            serverConnectionWrappers.put(remoteAddress, serverConnectionWrapper);
        }

        return serverConnectionWrapper;
    }

    public void removeConnection(InetSocketAddress address) {
        synchronized (serverConnectionWrappers) {
            serverConnectionWrappers.remove(address);
        }
    }

    public void stop() {
        active = false;

        socketServer.stop();
        datagramServer.stop();
        socket.interrupt();
        datagram.interrupt();
    }

    public InetSocketAddress getAddress(int connectionId) {
        synchronized (clientAddresses) {
            return clientAddresses.get(connectionId);
        }
    }

    public InetSocketAddress getDatagramAddress(int connectionId) {
        synchronized (clientDatagramAddresses) {
            return clientDatagramAddresses.get(connectionId);
        }
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

    public mpServerPlugin getServerPlugin() {
        return serverPlugin;
    }
}
