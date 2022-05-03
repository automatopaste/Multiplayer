package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.net.client.NettyClient;

import java.util.List;

public class mpClientPlugin extends BaseEveryFrameCombatPlugin {
    private NettyClient client;

    private final int port;
    private final String host;

    public mpClientPlugin(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void init(CombatEngineAPI engine) {
        client = new NettyClient(host, port);
        new Thread(client, "mpClient").start();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {

    }
}