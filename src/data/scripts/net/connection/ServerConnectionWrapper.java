package data.scripts.net.connection;

import data.scripts.net.data.BasePackable;
import data.scripts.net.data.packables.ConnectionStatusData;
import data.scripts.net.io.PacketContainer;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerConnectionWrapper extends BaseConnectionWrapper {
    private final ServerConnectionManager connectionManager;

    public ServerConnectionWrapper(ServerConnectionManager connectionManager, int instanceId) {
        this.connectionManager = connectionManager;

        statusData = new ConnectionStatusData(instanceId);
    }

    @Override
    public PacketContainer getSocketMessage() throws IOException {
        switch (connectionState) {
            case INITIALISATION_READY:
            case INITIALISING:
            case LOADING_READY:
                return null;
            case LOADING:
                Console.showMessage("Sending client " + connectionId + " data over socket");

                List<BasePackable> data = new ArrayList<>();
                data.add(statusData);

                data.addAll(connectionManager.getServerPlugin().getDataStore().getGenerated().values());

                // if client requests data again, state will return back to INITIALISING and resend packet
                connectionState = ConnectionState.SIMULATING;

                return new PacketContainer(data, connectionManager.getTick(), true, null);
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
                return null;
            case SIMULATING:
                List<BasePackable> data = new ArrayList<>();
                data.add(statusData);

                data.addAll(connectionManager.getDuplex().getDeltas().values());

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
        Integer key = null;
        for (BasePackable packable : entities.values()) {
            if (packable instanceof ConnectionStatusData) {
                statusData.updateFromDelta(packable);
                key = statusData.getInstanceID();

                // client determines status
                connectionState = ConnectionStatusData.ordinalToConnectionState(statusData.getState().getRecord());
            }
        }
        if (key != null) entities.remove(key);

        connectionManager.getDuplex().updateInbound(entities);
    }
}