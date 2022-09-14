package data.scripts.net.io;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.SourcePackable;
import data.scripts.net.data.packables.metadata.connection.ConnectionIDs;
import data.scripts.net.data.packables.metadata.connection.ConnectionSource;
import data.scripts.net.data.records.IntRecord;
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

    public ClientConnectionWrapper(String host, int port, MPPlugin plugin) {
        super(plugin);

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

            statusData = new ConnectionSource(ConnectionIDs.getConnectionId(address), this);
        }

        switch (connectionState) {
            case INITIALISATION_READY:
                Console.showMessage("Awaiting server acknowledgement");

                connectionState = ConnectionState.INITIALISING;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), -1));

                return new PacketContainer(Collections.singletonList((SourcePackable) statusData), -1, true, null, socketBuffer);
            case LOADING_READY:
                Console.showMessage("Waiting for prerequisite data");

                connectionState = ConnectionState.LOADING;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), -1));

                return new PacketContainer(Collections.singletonList((SourcePackable) statusData), -1, true, null, socketBuffer);
            case SPAWNING_READY:
                Console.showMessage("Spawning entities");

                connectionState = ConnectionState.SPAWNING;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), -1));

                return new PacketContainer(Collections.singletonList((SourcePackable) statusData), -1, true, null, socketBuffer);
            case SIMULATION_READY:
                Console.showMessage("Starting simulation");

                connectionState = ConnectionState.SIMULATING;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), -1));

                startDatagramClient();

                return new PacketContainer(Collections.singletonList((SourcePackable) statusData), -1, true, null, socketBuffer);
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
                List<SourcePackable> data = new ArrayList<>();
                data.add(statusData);

                for (Map<Integer, SourcePackable> type : dataDuplex.getOutbound().values()) {
                    data.addAll(type.values());
                }

                return new PacketContainer(data, tick, false, null, datagramBuffer);
            case CLOSED:
            default:
                return null;
        }
    }

    public void updateInbound(Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> entities, int tick) {
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
    public void processDelta(int id, Map<Integer, BaseRecord<?>> toProcess, MPPlugin plugin) {
        int state = (int) toProcess.get(ConnectionIDs.STATE).getValue();
        if (state < connectionState.ordinal()) {
            return;
        }

        statusData.updateFromDelta(toProcess);
        connectionState = BaseConnectionWrapper.ordinalToConnectionState(state);
    }

    @Override
    public void updateEntities(float amount) {

    }

    @Override
    public void register() {
        DataGenManager.registerInboundEntityManager(ConnectionIDs.TYPE_ID, this);
    }
}
