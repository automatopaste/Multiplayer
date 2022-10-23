package data.scripts.net.io.udp.server;

import data.scripts.net.data.BaseRecord;
import data.scripts.net.io.ServerConnectionManager;
import data.scripts.net.io.ServerConnectionWrapper;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

public class ServerInboundHandler extends SimpleChannelInboundHandler<Unpacked> {
    private final ServerConnectionManager serverConnectionManager;
    private ServerConnectionWrapper connectionWrapper;

    public ServerInboundHandler(ServerConnectionManager serverConnectionManager) {
        this.serverConnectionManager = serverConnectionManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Unpacked in) throws Exception {
        if (connectionWrapper == null) {
            connectionWrapper = serverConnectionManager.getConnection(in.getSender());
        }

        int serverTick = in.getTick();
        //Console.showMessage("Received UDP unpacked with tick: " + serverTick);

        // DISCARD WHILE DEBUG
        Map<Integer, Map<Integer, Map<Integer, BaseRecord<?>>>> entities = in.getUnpacked();

        // if getting -1 value tick from server, server is sending preload data
        connectionWrapper.updateInbound(entities);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ArrayIndexOutOfBoundsException) {
            System.err.println("Malformed packet caught");
            ctx.flush();
        } else {
            System.err.println("Error caught in datagram channel: " + cause.getMessage());
            cause.printStackTrace();
            ctx.close();
        }
    }
}
