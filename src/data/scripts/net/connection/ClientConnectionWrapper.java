package data.scripts.net.connection;

import data.scripts.net.connection.tcp.client.SocketClient;
import data.scripts.net.connection.udp.client.DatagramClient;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.ConnectionStatusData;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages switching logic for inputting/sending data
 */
public class ClientConnectionWrapper extends BaseConnectionWrapper{
    private final DataDuplex dataDuplex;

    private final DatagramClient datagramClient;
    private final Thread datagram;

    private final SocketClient socketClient;
    private final Thread socket;

    private int tick;

    public ClientConnectionWrapper(String host, int port) {
        dataDuplex = new DataDuplex();

        datagramClient = new DatagramClient(host, port, this);
        datagram = new Thread(datagramClient, "DATAGRAM_CLIENT_THREAD");

        socketClient = new SocketClient(host, port, this);
        socket = new Thread(socketClient, "SOCKET_CLIENT_THREAD");

        statusData = new ConnectionStatusData(ConnectionStatusData.UNASSIGNED);
        statusData.setConnection(this);

        connectionId = ConnectionStatusData.UNASSIGNED;
        tick = -1;

        socket.start();
        datagram.start();
    }

    @Override
    public void update() {
        switch (connectionState) {
            case INITIAL:

                break;
            case LOADING:
                break;
            case SIMULATION:
                break;
            case CLOSED:
                break;
        }
    }

    @Override
    public PacketContainer getSocketMessage() throws IOException {
        List<BasePackable> packables = new ArrayList<>();
        packables.add(statusData);
        return new PacketContainer(packables, -10, false, null);

//        switch (connectionState) {
//            case INITIAL:
//                List<BasePackable> packables = new ArrayList<>();
//                packables.add(statusData);
//                return new PacketContainer(packables, -1, false, null);
//            case LOADING:
//                return dataDuplex.getPacket(tick, null);
//        }
//        return null;
    }

    @Override
    public PacketContainer getDatagram() throws IOException {
        List<BasePackable> packables = new ArrayList<>();
        packables.add(statusData);
        return new PacketContainer(packables, -20, false, null);

//        if (connectionState == ConnectionState.SIMULATION) {
//            return dataDuplex.getPacket(tick, null);
//        }
//        return null;
    }

    public void updateInbound(Map<Integer, BasePackable> entities, int tick) {
        this.tick = tick;

        for (BasePackable packable : entities.values()) {
            if (packable instanceof ConnectionStatusData) {
                statusData.updateFromDelta(packable);
            }
        }

        dataDuplex.updateInbound(entities);
    }

    public DataDuplex getDuplex() {
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

    public int getTick() {
        return tick;
    }
}
