package data.scripts.net.connection;

import data.scripts.net.connection.tcp.client.SocketClient;
import data.scripts.net.connection.udp.client.DatagramClient;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;

/**
 * Manages switching logic for inputting/sending data
 */
public class ClientConnectionWrapper extends BaseConnectionWrapper{
    private final DataDuplex dataDuplex;

    private final DatagramClient datagramClient;
    private final Thread datagram;

    private final SocketClient socketClient;
    private final Thread socket;

    public ClientConnectionWrapper(String host, int port) {
        dataDuplex = new DataDuplex();

        datagramClient = new DatagramClient(host, port, this);
        datagram = new Thread(datagramClient, "DATAGRAM_CLIENT_THREAD");

        socketClient = new SocketClient(host, port, this);
        socket = new Thread(socketClient, "SOCKET_CLIENT_THREAD");
    }

    @Override
    public PacketContainer getSocketMessage() throws IOException {
        switch (connectionState) {
            case INITIAL:
            case LOADING:
                return dataDuplex.getPacket(dataDuplex.getCurrTick(), null);
        }
        return null;
    }

    @Override
    public PacketContainer getDatagram() throws IOException {
        if (connectionState == ConnectionState.SIMULATION) {
            return dataDuplex.getPacket(dataDuplex.getCurrTick(), null);
        }
        return null;
    }

    public synchronized DataDuplex getDuplex() {
        return dataDuplex;
    }

    public synchronized ConnectionState getConnectionState() {
        return connectionState;
    }

    public synchronized void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void stop() {
        socketClient.stop();
        datagramClient.stop();
        socket.interrupt();
        datagram.interrupt();
    }
}
