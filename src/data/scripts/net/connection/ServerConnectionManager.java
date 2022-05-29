package data.scripts.net.connection;

import com.fs.starfarer.api.Global;
import data.scripts.net.connection.tcp.server.SocketServer;
import data.scripts.net.connection.udp.server.DatagramServer;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a collection of registered user connections
 */
public class ServerConnectionManager {
    private final int maxConnections = Global.getSettings().getInt("mpMaxConnections");

    private final int port;
    private final DataDuplex dataDuplex;
    private boolean active;

    private final DatagramServer datagramServer;
    private final Thread datagram;

    private final SocketServer socketServer;
    private final Thread socket;

    private final List<ServerConnectionWrapper> serverConnectionWrappers;

    public ServerConnectionManager(int port) {
        this.port = port;
        dataDuplex = new DataDuplex();
        active = true;

        serverConnectionWrappers = new ArrayList<>();

        datagramServer = new DatagramServer(port, this);
        datagram = new Thread(datagramServer, "DATAGRAM_SERVER_THREAD");

        socketServer = new SocketServer(port, this);
        socket = new Thread(socketServer, "SOCKET_SERVER_THREAD");
    }

    public List<PacketContainer> getSocketMessages() throws IOException {
        List<PacketContainer> output = new ArrayList<>();

        for (ServerConnectionWrapper connection : serverConnectionWrappers) {
            PacketContainer message = connection.getSocketMessage();
            if (message != null) output.add(message);
        }

        return output;
    }

    public List<PacketContainer> getDatagrams() throws IOException {
        List<PacketContainer> output = new ArrayList<>();

        for (ServerConnectionWrapper connection : serverConnectionWrappers) {
            PacketContainer message = connection.getDatagram();
            if (message != null) output.add(message);
        }

        return output;
    }

    public ServerConnectionWrapper getNewConnection() {
        synchronized (serverConnectionWrappers) {
            if (serverConnectionWrappers.size() >= maxConnections) return null;
        }
        ServerConnectionWrapper serverConnectionWrapper = new ServerConnectionWrapper(port, this);

        synchronized (serverConnectionWrappers) {
            serverConnectionWrappers.add(serverConnectionWrapper);
        }

        return serverConnectionWrapper;
    }

    public void removeConnection(ServerConnectionWrapper serverConnectionWrapper) {
        synchronized (serverConnectionWrappers) {
            serverConnectionWrappers.remove(serverConnectionWrapper);
        }
    }

    public void stop() {
        socketServer.stop();
        datagramServer.stop();
        socket.interrupt();
        datagram.interrupt();
    }

    public synchronized List<ServerConnectionWrapper> getConnections() {
        return serverConnectionWrappers;
    }

    public synchronized DataDuplex getDuplex() {
        return dataDuplex;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void setActive(boolean active) {
        this.active = active;
    }
}
