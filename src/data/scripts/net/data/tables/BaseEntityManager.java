package data.scripts.net.data.tables;

import data.scripts.plugins.MPPlugin;

public interface BaseEntityManager {

    short DEFAULT_HOST_INSTANCE = -1;
    byte DEFAULT_HOST_ID = -1;

    void update(float amount, MPPlugin plugin);

    void register();
}
