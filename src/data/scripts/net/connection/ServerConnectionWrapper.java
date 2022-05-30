package data.scripts.net.connection;

import data.scripts.net.data.BasePackable;
import data.scripts.net.io.PacketContainer;

import java.io.IOException;
import java.util.Map;

public class ServerConnectionWrapper extends BaseConnectionWrapper {
    private final int port;
    private final ServerConnectionManager connectionManager;

    public ServerConnectionWrapper(int port, ServerConnectionManager connectionManager) {
        this.port = port;
        this.connectionManager = connectionManager;

        statusData = null;
    }

    @Override
    public void update() {

    }

    @Override
    public PacketContainer getSocketMessage() throws IOException {
        return null;
//        List<BasePackable> packables = new ArrayList<>();
//        packables.add(statusData);
//        return new PacketContainer(packables, 10, false, null);

//        switch (connectionState) {
//            case INITIAL:
//            case LOADING:
//                DataDuplex dataDuplex = connectionManager.getDuplex();
//                return dataDuplex.getPacket(connectionManager.getTick(), null);
//        }
//        return null;
    }

    @Override
    public PacketContainer getDatagram() throws IOException {
        return null;
//        List<BasePackable> packables = new ArrayList<>();
//        packables.add(statusData);
//        return new PacketContainer(packables, 20, false, null);

//        if (connectionState == ConnectionState.SIMULATION) {
//            DataDuplex dataDuplex = connectionManager.getDuplex();
//            return dataDuplex.getPacket(connectionManager.getTick(), null);
//        }
//        return null;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void updateInbound(Map<Integer, BasePackable> entities) {
        connectionManager.getDuplex().updateInbound(entities);
    }

    public ServerConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
