package data.scripts.net.connection.server;

import data.scripts.net.connection.DataDuplex;

public class ServerConnectionManager {
    private final DataDuplex dataDuplex;
    private boolean requestLoad;
    private final int id;

    public ServerConnectionManager(int id) {
        dataDuplex = new DataDuplex();
        requestLoad = true;
        this.id = id;
    }

    public DataDuplex getDuplex() {
        return dataDuplex;
    }

    public boolean isRequestLoad() {
        return requestLoad;
    }

    public void setRequestLoad(boolean requestLoad) {
        this.requestLoad = requestLoad;
    }

    public int getId() {
        return id;
    }
}
