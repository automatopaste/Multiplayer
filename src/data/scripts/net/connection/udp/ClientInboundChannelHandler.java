package data.scripts.net.connection.udp;

import data.scripts.net.connection.client.ClientConnectionWrapper;
import data.scripts.net.data.BasePackable;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.lazywizard.console.Console;

import java.util.Map;

public class ClientInboundChannelHandler extends SimpleChannelInboundHandler<Unpacked> {
    private final ClientConnectionWrapper connection;


    public ClientInboundChannelHandler(ClientConnectionWrapper connection) {
        this.connection = connection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Unpacked unpacked) throws Exception {
        int serverTick = unpacked.getTick();
        Console.showMessage("Received unpacked with tick: " + serverTick);

        connection.getDuplex().setCurrTick(serverTick);
        Map<Integer, BasePackable> entities = unpacked.getUnpacked();

        // if getting -1 value tick from server, server is sending preload data
        connection.setLoading(serverTick == -1);
        connection.getDuplex().updateInbound(entities);
    }
}
