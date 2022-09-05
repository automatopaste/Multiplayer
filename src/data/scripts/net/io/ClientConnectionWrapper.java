package data.scripts.net.io;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.metadata.ConnectionStatusData;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.data.util.DataGenManager;
import data.scripts.net.io.tcp.client.SocketClient;
import data.scripts.net.io.udp.client.DatagramClient;
import data.scripts.plugins.MPPlugin;
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
public class ClientConnectionWrapper extends BaseConnectionWrapper implements InboundEntityManager {
    private final DataDuplex dataDuplex;

    private DatagramClient datagramClient;
    private Thread datagram;

    private final SocketClient socketClient;
    private final Thread socket;
    private final String host;

    private int tick;

    public ClientConnectionWrapper(String host, int port) {
        this.host = host;
        dataDuplex = new DataDuplex();

        socketClient = new SocketClient(host, port, this);
        socket = new Thread(socketClient, "SOCKET_CLIENT_THREAD");
        socket.start();

        tick = -1;
    }

    @Override
    public PacketContainer getSocketMessage() throws IOException {
        if (statusData == null) {
            InetSocketAddress address = socketClient.getLocal();
            if (address == null) return null;

            statusData = new ConnectionStatusData(ConnectionStatusData.getConnectionId(address));
            statusData.setConnection(this);
        }

        switch (connectionState) {
            case INITIALISATION_READY:
                Console.showMessage("Awaiting server acknowledgement");

                connectionState = ConnectionState.INITIALISING;
                statusData.updateState();

                return new PacketContainer(Collections.singletonList((BasePackable) statusData), -1, true, null);
            case LOADING_READY:
                Console.showMessage("Waiting for prerequisite data");

                connectionState = ConnectionState.LOADING;
                statusData.updateState();

                return new PacketContainer(Collections.singletonList((BasePackable) statusData), -1, true, null);
            case SPAWNING_READY:
                Console.showMessage("Spawning entities");

                connectionState = ConnectionState.SPAWNING;
                statusData.updateState();

                return new PacketContainer(Collections.singletonList((BasePackable) statusData), -1, true, null);
            case SIMULATION_READY:
                Console.showMessage("Starting simulation");

                connectionState = ConnectionState.SIMULATING;
                statusData.updateState();
                startDatagramClient();

                return new PacketContainer(Collections.singletonList((BasePackable) statusData), -1, true, null);
            case SIMULATING:
            case CLOSED:
            default:
                return null;
        }
    }

    private void startDatagramClient() {
        datagramClient = new DatagramClient(host, ((InetSocketAddress) socketClient.getChannel().localAddress()).getPort(), this);
        datagram = new Thread(datagramClient, "DATAGRAM_CLIENT_THREAD");
        datagram.start();
    }

    @Override
    public PacketContainer getDatagram() throws IOException {
        if (statusData == null) return null;

        switch (connectionState) {
            case INITIALISATION_READY:
            case INITIALISING:
            case LOADING_READY:
            case LOADING:
            case SIMULATION_READY:
            case SPAWNING_READY:
            case SPAWNING:
                return null;
            case SIMULATING:
                List<BasePackable> data = new ArrayList<>();
                data.add(statusData);

                for (Map<Integer, BasePackable> type : dataDuplex.getOutbound().values()) {
                    data.addAll(type.values());
                }

                return new PacketContainer(data, tick, false, null);
            case CLOSED:
            default:
                return null;
        }
    }

    public void updateInbound(Map<Integer, Map<Integer, BasePackable>> entities, int tick) {
        this.tick = tick;
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

    private void updateConnectionStatusData(ConnectionStatusData data) {
        int state = data.getState().getRecord();
        if (state < connectionState.ordinal()) {
            return;
        }

        statusData.updateFromDelta(data);
        connectionState = BaseConnectionWrapper.ordinalToConnectionState(state);
    }

    public void stop() {
        socketClient.stop();
        if (datagramClient != null) datagramClient.stop();
        socket.interrupt();
        if (datagram != null) datagram.interrupt();
    }

    public int getTick() {
        return tick;
    }

    @Override
    public void processDelta(int id, BasePackable toProcess, MPPlugin plugin) {
        ConnectionStatusData connectionStatusData = (ConnectionStatusData) toProcess;
        updateConnectionStatusData(connectionStatusData);
    }

    @Override
    public void updateEntities() {

    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ConnectionStatusData.TYPE_ID, this);
    }
}
