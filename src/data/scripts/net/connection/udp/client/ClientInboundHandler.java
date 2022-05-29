package data.scripts.net.connection.udp.client;

import data.scripts.net.connection.ClientConnectionWrapper;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.lazywizard.console.Console;

public class ClientInboundHandler extends SimpleChannelInboundHandler<Unpacked> {
    private final ClientConnectionWrapper connection;

    public ClientInboundHandler(ClientConnectionWrapper connection) {
        this.connection = connection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Unpacked in) throws Exception {
        int serverTick = in.getTick();
        Console.showMessage("Received unpacked with tick: " + serverTick);
//
//        connection.getDuplex().setCurrTick(serverTick);
//        Map<Integer, BasePackable> entities = in.getUnpacked();
//
//        // if getting -1 value tick from server, server is sending preload data
//        connection.setLoading(serverTick == -1);
//        connection.getDuplex().updateInbound(entities);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println(cause.getMessage());
        ctx.close();
    }
}
