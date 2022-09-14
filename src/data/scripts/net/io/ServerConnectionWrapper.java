package data.scripts.net.io;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.data.SourcePackable;
import data.scripts.net.data.packables.metadata.connection.ConnectionIDs;
import data.scripts.net.data.packables.metadata.connection.ConnectionSource;
import data.scripts.net.data.records.IntRecord;
import data.scripts.plugins.MPPlugin;
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

    public ServerConnectionWrapper(ServerConnectionManager connectionManager, int connectionId, InetSocketAddress remoteAddress, MPPlugin plugin) {
        super(plugin);

        this.connectionManager = connectionManager;
        this.remoteAddress = remoteAddress;
        this.connectionId = connectionId;

        statusData = new ConnectionSource(connectionId, this);
    }

    @Override
    public PacketContainer getSocketMessage() throws IOException {
        if (statusData == null) return null;

        List<SourcePackable> data;
        switch (connectionState) {
            case INITIALISATION_READY:
                return null;
            case INITIALISING:
                connectionState = ConnectionState.LOADING_READY;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), -1));

                return new PacketContainer(
                        Collections.singletonList((SourcePackable) statusData),
                        connectionManager.getTick(),
                        true,
                        remoteAddress,
                        socketBuffer
                );
            case LOADING:
                Console.showMessage("Sending client " + connectionId + " data over socket");

                data = new ArrayList<>();
                data.add(statusData);

                data.addAll(connectionManager.getServerPlugin().getDataStore().getGenerated());

                connectionState = ConnectionState.SPAWNING_READY;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), -1));

                return new PacketContainer(
                        data,
                        connectionManager.getTick(),
                        true,
                        remoteAddress,
                        socketBuffer
                );
            case SPAWNING:
                data = new ArrayList<>();
                data.add(statusData);

                data.addAll(connectionManager.getServerPlugin().getServerShipTable().getOutbound().values());

                connectionState = ConnectionState.SIMULATION_READY;
                statusData.getRecord(ConnectionIDs.STATE).updateFromDelta(new IntRecord(connectionState.ordinal(), -1));

                return new PacketContainer(
                        data,
                        connectionManager.getTick(),
                        true,
                        remoteAddress,
                        socketBuffer
                );
            case SIMULATION_READY:
            case SIMULATING:
            case CLOSED:
            default:
                return null;
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
                List<SourcePackable> data = new ArrayList<>();
                data.add(statusData);

                for (Map<Integer, SourcePackable> type : connectionManager.getDuplex().getOutbound().values()) {
                    data.addAll(type.values());
                }

                return new PacketContainer(
                        data, connectionManager.getTick(), false, remoteAddress, datagramBuffer
                );
            case CLOSED: // *shuts briefcase*
            default:
                return null;
        }
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void updateInbound(Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> entities) {
        connectionManager.getDuplex().updateInbound(entities);
    }

    public void updateConnectionStatus(Map<Integer, BaseRecord<?>> data) {
        int state = (int) data.get(ConnectionIDs.STATE).getValue();
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
