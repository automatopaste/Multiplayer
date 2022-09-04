package data.scripts.plugins;

public interface MPPlugin {
    enum PluginType {
        SERVER,
        CLIENT
    }

    PluginType getType();
}
