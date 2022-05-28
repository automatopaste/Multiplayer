package data.scripts.net.connection.client;

import data.scripts.net.connection.DataDuplex;

public class ClientConnectionWrapper {
    private final DataDuplex dataDuplex;
    private boolean loading;

    public ClientConnectionWrapper() {
        dataDuplex = new DataDuplex();
        loading = true;
    }

    public synchronized DataDuplex getDuplex() {
        return dataDuplex;
    }

    public synchronized boolean isLoading() {
        return loading;
    }

    public synchronized void setLoading(boolean loading) {
        this.loading = loading;
    }
}
