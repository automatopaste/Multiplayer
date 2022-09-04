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
        List<BasePackable> data = new ArrayList<>();
        switch (connectionState) {
            case INITIALISATION_READY:
                return null;
            case INITIALISING:
                connectionState = ConnectionState.LOADING_READY;

                return new PacketContainer(
                        Collections.singletonList((BasePackable) statusData),
                        connectionManager.getTick(),
                        true,
                        remoteAddress
                );
            case LOADING_READY:
                return null;
            case LOADING:
                Console.showMessage("Sending client " + connectionId + " data over socket");

                data = new ArrayList<>();
                data.add(statusData);

                data.addAll(connectionManager.getServerPlugin().getDataStore().getGenerated());

                // if client requests data again, state will return back to INITIALISING and resend packet
                connectionState = ConnectionState.SPAWNING_READY;

                return new PacketContainer(
                        data,
                        connectionManager.getTick(),
                        true,
                        remoteAddress
                );
            case SPAWNING_READY:
                return null;
            case SPAWNING:
                data = new ArrayList<>();
                data.add(statusData);

                data.addAll(connectionManager.getServerPlugin().getServerShipTable().getOutbound().values());

                connectionState = ConnectionState.SIMULATION_READY;

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
            case SPAWNING_READY:
            case SPAWNING:
            case SIMULATION_READY:
                return null;
            case SIMULATING:
                List<BasePackable> data = new ArrayList<>();
                data.add(statusData);

                data.addAll(connectionManager.getDuplex().getOutbound());

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

    public void updateInbound(Map<Integer, BasePackable> entities) {
        // grab connection data
//        BasePackable data = entities.get(connectionId);
//        if (data != null) {
//            updateConnectionStatusData((ConnectionStatusData) data);
//            entities.remove(connectionId);
//        } else {
//            Integer key = null;
//            for (BasePackable packable : entities.values()) {
//                if (packable instanceof ConnectionStatusData) {
//                    key = packable.getInstanceID();
//                    updateConnectionStatusData((ConnectionStatusData) packable);
//                }
//            }
//            if (key != null) entities.remove(key);
//        }

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
        connectionManager.removeConnection(remoteAddress);
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
