package data.scripts.net.io;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.ConnectionStatusData;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ServerConnectionWrapper extends BaseConnectionWrapper {
    private final ServerConnectionManager connectionManager;

    public ServerConnectionWrapper(ServerConnectionManager connectionManager, int connectionId) {
        this.connectionManager = connectionManager;
        this.connectionId = connectionId;

        statusData = new ConnectionStatusData(connectionId);
        statusData.setConnection(this);
    }

    @Override
    public PacketContainer getSocketMessage() throws IOException {
        switch (connectionState) {
            case INITIALISATION_READY:
                return null;
            case INITIALISING:
                connectionState = ConnectionState.LOADING_READY;

                return new PacketContainer(
                        Collections.singletonList((BasePackable) statusData),
                        connectionManager.getTick(),
                        true,
                        connectionManager.getAddress(connectionId)
                );
            case LOADING_READY:
                return null;
            case LOADING:
                Console.showMessage("Sending client " + connectionId + " data over socket");

                List<BasePackable> data = new ArrayList<>();
                data.add(statusData);

                data.addAll(connectionManager.getServerPlugin().getDataStore().getGenerated().values());

                // if client requests data again, state will return back to INITIALISING and resend packet
                connectionState = ConnectionState.SIMULATION_READY;

                return new PacketContainer(
                        data,
                        connectionManager.getTick(),
                        true,
                        connectionManager.getAddress(connectionId)
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
            case SIMULATION_READY:
                return null;
            case SIMULATING:
                List<BasePackable> data = new ArrayList<>();
                data.add(statusData);

                data.addAll(connectionManager.getDuplex().getOutbound());

                return new PacketContainer(
                        data, connectionManager.getTick(), false, connectionManager.getAddress(connectionId)
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

        connectionManager.getDuplex().updateInbound(entities);
    }

    private void updateConnectionStatusData(BasePackable packable) {
        statusData.updateFromDelta(packable);
        connectionState = ConnectionStatusData.ordinalToConnectionState(statusData.getState().getRecord());
    }

    public void close() {
        connectionManager.removeConnection(connectionManager.getAddress(connectionId));
    }
}
