package data.scripts.net.io;

import cmu.CMUtils;
import com.fs.starfarer.api.Global;
import data.scripts.net.data.InboundData;
import data.scripts.net.data.InstanceData;
import data.scripts.net.data.OutboundData;
import data.scripts.net.data.packables.metadata.ConnectionData;
import data.scripts.net.io.tcp.client.SocketClient;
import data.scripts.net.io.udp.client.DatagramClient;
import data.scripts.plugins.MPPlugin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages switching logic for inputting/sending data
 */
public class ClientConnectionWrapper extends BaseConnectionWrapper {
    private final DataDuplex dataDuplex;

    private DatagramClient datagramClient;
    private Thread datagram;

    private final SocketClient socketClient;
    private final Thread socket;
    private final String host;
    private final int port;

    private int tick;

    public ClientConnectionWrapper(String host, int port, MPPlugin plugin) {
        super(plugin);

        this.host = host;
        this.port = port;
        dataDuplex = new DataDuplex();

        tick = -1;

        socketClient = new SocketClient(host, port, this);
        socket = new Thread(socketClient, "SOCKET_CLIENT_THREAD");
        socket.start();
    }

    @Override
    public List<MessageContainer> getSocketMessages() throws IOException {
        if (connectionData == null) {
            InetSocketAddress address = socketClient.getLocal();
            if (address == null) return null;

            connectionID = ConnectionData.getConnectionID(address);
            connectionData = new ConnectionData(connectionID, this);
            clientPort = socketClient.getLocalPort();
            return null;
        }

        connectionState = BaseConnectionWrapper.ordinalToConnectionState(connectionData.getConnectionState());

        OutboundData outbound = dataDuplex.getOutboundSocket();

        switch (connectionState) {
            case INITIALISATION_READY:
            //case INITIALISING:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "initialising connection...");
                Global.getLogger(ClientConnectionWrapper.class).info("initialising connection");

                connectionState = ConnectionState.INITIALISING;

                break;
            case LOADING_READY:
            //case LOADING:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "Receiving data over socket...");
                Global.getLogger(ClientConnectionWrapper.class).info("receiving data");

                connectionState = ConnectionState.LOADING;

                break;
            case SPAWNING_READY:
            //case SPAWNING:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "Spawning entities...");
                Global.getLogger(ClientConnectionWrapper.class).info("spawning entities");

                connectionState = ConnectionState.SPAWNING;

                break;
            case SIMULATION_READY:
                CMUtils.getGuiDebug().putText(ClientConnectionWrapper.class, "debug", "Starting simulation...");
                Global.getLogger(ClientConnectionWrapper.class).info("starting simulation");

                connectionState = ConnectionState.SIMULATING;

                break;
            case SIMULATING:
                if (datagramClient == null && connectionData.getClientPort() != 0) {
                    datagramClient = new DatagramClient(host, port, clientPort, this);
                    datagram = new Thread(datagramClient, "DATAGRAM_CLIENT_THREAD");
                    datagram.start();
                }

                break;
            default:
                break;
        }

        Map<Short, InstanceData> instance = new HashMap<>();
        instance.put(connectionID, connectionData.sourceExecute(0f));
        outbound.out.put(ConnectionData.TYPE_ID, instance);

        return writeBuffer(outbound, tick, null, connectionID);
    }

    @Override
    public List<MessageContainer> getDatagrams() throws IOException {
        if (connectionData == null || connectionState != ConnectionState.SIMULATING) return null;

        OutboundData outbound = dataDuplex.getOutboundDatagram();

        switch (connectionState) {
            case INITIALISATION_READY:
            case INITIALISING:
            case LOADING_READY:
            case LOADING:
            case SIMULATION_READY:
            case SPAWNING_READY:
            case SPAWNING:
            case SIMULATING:
            case CLOSED:
            default:
                break;
        }

        return writeBuffer(outbound, tick, null, connectionID);
    }

    public void updateInbound(InboundData entities, int tick) {
        Map<Short, Map<Byte, Object>> instance = entities.in.get(ConnectionData.TYPE_ID);
        if (instance != null) {
            Map<Byte, Object> data = instance.get(connectionID);
            if (data != null) {
                connectionData.destExecute(data, tick);
            }
        }
        entities.in.remove(ConnectionData.TYPE_ID);

        if (tick != -1) this.tick = tick;
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

    @Override
    public void stop() {
        socketClient.stop();
        if (datagramClient != null) datagramClient.stop();
        socket.interrupt();
        if (datagram != null) datagram.interrupt();

        setConnectionState(BaseConnectionWrapper.ConnectionState.CLOSED);
    }

    public int getTick() {
        return tick;
    }
}
