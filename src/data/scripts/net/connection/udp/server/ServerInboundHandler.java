package data.scripts.net.connection.udp.server;

import data.scripts.net.connection.ServerConnectionWrapper;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.lazywizard.console.Console;

public class ServerInboundHandler extends SimpleChannelInboundHandler<Unpacked> {
    private final ServerConnectionWrapper connection;

    public ServerInboundHandler(ServerConnectionWrapper connection) {
        this.connection = connection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Unpacked in) throws Exception {
        int serverTick = in.getTick();
        Console.showMessage("Received UDP unpacked with tick: " + serverTick);

        // DISCARD WHILE DEBUG
//        Map<Integer, BasePackable> entities = in.getUnpacked();
//
//        // if getting -1 value tick from server, server is sending preload data
//        connection.getConnectionManager().getDuplex().updateInbound(entities);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println(cause.getMessage());
        ctx.close();
    }
}
