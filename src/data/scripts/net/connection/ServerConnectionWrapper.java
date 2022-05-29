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
    }

    @Override
    public PacketContainer getSocketMessage() throws IOException {
        switch (connectionState) {
            case INITIAL:
            case LOADING:
                DataDuplex dataDuplex = connectionManager.getDuplex();
                return dataDuplex.getPacket(dataDuplex.getCurrTick(), null);
        }
        return null;
    }

    @Override
    public PacketContainer getDatagram() throws IOException {
        if (connectionState == ConnectionState.SIMULATION) {
            DataDuplex dataDuplex = connectionManager.getDuplex();
            return dataDuplex.getPacket(dataDuplex.getCurrTick(), null);
        }
        return null;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public void updateInbound(Map<Integer, BasePackable> entities) {
        connectionManager.getDuplex().updateInbound(entities);
    }
}
