package data.scripts.net.connection;

import data.scripts.net.connection.tcp.client.SocketClient;
import data.scripts.net.connection.udp.client.DatagramClient;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.ConnectionStatusData;
import data.scripts.net.io.PacketContainer;
import data.scripts.plugins.mpClientPlugin;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
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
    private final String host;
    private final int port;
    private final mpClientPlugin clientPlugin;

    private int tick;

    public ClientConnectionWrapper(String host, int port, mpClientPlugin clientPlugin) {
        this.host = host;
        this.port = port;
        this.clientPlugin = clientPlugin;
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
    public PacketContainer getSocketMessage() throws IOException {
        switch (connectionState) {
            case INITIALISATION_READY:
                Console.showMessage("Awaiting server acknowledgement");

                connectionState = ConnectionState.INITIALISING;

                return new PacketContainer(Collections.singletonList((BasePackable) statusData), -1, true, null);
            case INITIALISING:
                return null;
            case LOADING_READY:
                connectionState = ConnectionState.LOADING;

                return new PacketContainer(Collections.singletonList((BasePackable) statusData), -1, true, null);
            case LOADING:
                return null;
            case SIMULATION_READY:
                clientPlugin.getDataStore().absorbVariants(dataDuplex.getDeltas());

                connectionState = ConnectionState.SIMULATING;

                return new PacketContainer(Collections.singletonList((BasePackable) statusData), -1, true, null);
            case SIMULATING:
            case CLOSED:
            default:
                return null;
        }
    }

    @Override
    public PacketContainer getDatagram() throws IOException {
        switch (connectionState) {
            case INITIALISATION_READY:
            case INITIALISING:
            case LOADING_READY:
            case LOADING:
            case SIMULATION_READY:
                return null;
            case SIMULATING:
                List<BasePackable> data = new ArrayList<>();
                data.add(statusData);

                data.addAll(dataDuplex.getOutbound());

                return new PacketContainer(data, tick, false, new InetSocketAddress(host, port));
            case CLOSED:
            default:
                return null;
        }
    }

    public void updateInbound(Map<Integer, BasePackable> entities, int tick) {
        this.tick = tick;

        // grab connection data
        Integer key = null;
        for (BasePackable packable : entities.values()) {
            if (packable instanceof ConnectionStatusData) {
                statusData.updateFromDelta(packable);
                key = statusData.getInstanceID();

                connectionState = ConnectionStatusData.ordinalToConnectionState(statusData.getState().getRecord());
            }
        }
        if (key != null) entities.remove(key);

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
