package data.scripts.net.io;

import data.scripts.net.io.tcp.client.SocketClient;
import data.scripts.net.io.udp.client.DatagramClient;
import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.ConnectionStatusData;
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



        socketClient = new SocketClient(host, port, this);
        socket = new Thread(socketClient, "SOCKET_CLIENT_THREAD");
        socket.start();

        statusData = new ConnectionStatusData(ConnectionStatusData.UNASSIGNED);
        statusData.setConnection(this);

        connectionId = ConnectionStatusData.UNASSIGNED;
        tick = -1;

        datagramClient = new DatagramClient(host, ((InetSocketAddress) socketClient.getChannel().localAddress()).getPort(), this);
        datagram = new Thread(datagramClient, "DATAGRAM_CLIENT_THREAD");
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
        BasePackable data = entities.get(connectionId);
        if (data != null) {
            updateConnectionStatusData(data);
            entities.remove(connectionId);
        } else {
            Integer key = null;
            for (BasePackable packable : entities.values()) {
                if (packable instanceof ConnectionStatusData) {
                    key = packable.getInstanceID();
                    updateConnectionStatusData(packable);
                }
            }
            if (key != null) entities.remove(key);
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

    private void updateConnectionStatusData(BasePackable packable) {
        statusData.updateFromDelta(packable);
        connectionState = ConnectionStatusData.ordinalToConnectionState(statusData.getState().getRecord());
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
