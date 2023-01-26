package data.scripts.net.data.tables;

import data.scripts.plugins.MPPlugin;

public interface BaseEntityManager {

    void execute(MPPlugin plugin);

    void update(float amount, MPPlugin plugin);

    void register();
}
