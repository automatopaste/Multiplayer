package data.scripts.net.io;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.metadata.ConnectionStatusData;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ServerConnectionWrapper extends BaseConnectionWrapper {
    private final ServerConnectionManager connectionManager;
    private final InetSocketAddress remoteAddress;

    public ServerConnectionWrapper(ServerConnectionManager connectionManager, int connectionId, InetSocketAddress remoteAddress) {
        this.connectionManager = connectionManager;
        this.remoteAddress = remoteAddress;
        this.connectionId = connectionId;

        statusData = new ConnectionStatusData(connectionId);
        statusData.setConnection(this);
    }

    @Override
    public PacketContainer getSocketMessage() throws IOException {
        if (statusData == null) return null;

        List<BasePackable> data;
        switch (connectionState) {
            case INITIALISATION_READY:
                return null;
            case INITIALISING:
                connectionState = ConnectionState.LOADING_READY;
                statusData.updateState();

                return new PacketContainer(
                        Collections.singletonList((BasePackable) statusData),
                        connectionManager.getTick(),
                        true,
                        remoteAddress
                );
            case LOADING:
                Console.showMessage("Sending client " + connectionId + " data over socket");

                data = new ArrayList<>();
                data.add(statusData);

                data.addAll(connectionManager.getServerPlugin().getDataStore().getGenerated());

                connectionState = ConnectionState.SPAWNING_READY;
                statusData.updateState();

                return new PacketContainer(
                        data,
                        connectionManager.getTick(),
                        true,
                        remoteAddress
                );
            case SPAWNING:
                data = new ArrayList<>();
                data.add(statusData);

                data.addAll(connectionManager.getServerPlugin().getServerShipTable().getOutbound().values());

                connectionState = ConnectionState.SIMULATION_READY;
                statusData.updateState();

                return new PacketContainer(
                        data,
                        connectionManager.getTick(),
                        true,
                        remoteAddress
                );
            case SIMULATION_READY:
            case SIMULATING:
            case CLOSED:
            default:
                return new PacketContainer(
                        Collections.singletonList((BasePackable) statusData),
                        connectionManager.getTick(),
                        true,
                        remoteAddress
                );
        }
    }

    @Override
    public PacketContainer getDatagram() throws IOException {
        if (statusData == null) return null;

        switch (connectionState) {
            case INITIALISATION_READY:
            case INITIALISING:
            case LOADING_READY:
            case LOADING:
            case SPAWNING_READY:
            case SPAWNING:
            case SIMULATION_READY:
                return null;
            case SIMULATING:
                List<BasePackable> data = new ArrayList<>();
                data.add(statusData);

                for (Map<Integer, BasePackable> type : connectionManager.getDuplex().getOutbound().values()) {
                    data.addAll(type.values());
                }

                return new PacketContainer(
                        data, connectionManager.getTick(), false, remoteAddress
                );
            case CLOSED: // *shuts briefcase*
            default:
                return null;
        }
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void updateInbound(Map<Integer, Map<Integer, BasePackable>> entities) {
        connectionManager.getDuplex().updateInbound(entities);
    }

    public void updateConnectionStatusData(ConnectionStatusData data) {
        int state = data.getState().getRecord();
        if (state < connectionState.ordinal()) {
            return;
        }

        statusData.updateFromDelta(data);
        connectionState = BaseConnectionWrapper.ordinalToConnectionState(state);
    }

    public void close() {
        connectionManager.removeConnection(connectionId);
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
