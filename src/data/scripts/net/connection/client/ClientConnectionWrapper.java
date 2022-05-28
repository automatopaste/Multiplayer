package data.scripts.net.connection.client;

import data.scripts.net.connection.DataDuplex;

public class ClientConnectionWrapper {
    private final DataDuplex dataDuplex;
    private boolean loading;

    public ClientConnectionWrapper() {
        dataDuplex = new DataDuplex();
        loading = true;
    }

    public DataDuplex getDuplex() {
        return dataDuplex;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }
}
