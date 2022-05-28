package data.scripts.net.connection.server;

import data.scripts.net.connection.DataDuplex;

public class ServerConnectionWrapper {
    private final DataDuplex dataDuplex;
    private boolean requestLoad;
    private final int id;

    public ServerConnectionWrapper(int id) {
        dataDuplex = new DataDuplex();
        requestLoad = true;
        this.id = id;
    }

    public synchronized DataDuplex getDuplex() {
        return dataDuplex;
    }

    public synchronized boolean isRequestLoad() {
        return requestLoad;
    }

    public synchronized void setRequestLoad(boolean requestLoad) {
        this.requestLoad = requestLoad;
    }

    public int getId() {
        return id;
    }
}
