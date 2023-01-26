package data.scripts.net.data.tables;

import data.scripts.plugins.MPPlugin;

public interface BaseEntityManager {

    void execute();

    void update(float amount, MPPlugin plugin);

    void register();
}
