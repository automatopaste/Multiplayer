package data.scripts.net.io.udp.server;

import data.scripts.net.data.InboundData;
import data.scripts.net.io.ServerConnectionManager;
import data.scripts.net.io.ServerConnectionWrapper;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerInboundHandler extends SimpleChannelInboundHandler<Unpacked> {
    private final ServerConnectionManager serverConnectionManager;
    private ServerConnectionWrapper connectionWrapper;

    public ServerInboundHandler(ServerConnectionManager serverConnectionManager) {
        this.serverConnectionManager = serverConnectionManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Unpacked in) throws Exception {
        if (connectionWrapper == null) {
            connectionWrapper = serverConnectionManager.getConnection(in.getConnectionID());
        }

        InboundData entities = in.getUnpacked();
        entities.setSize(in.getSize());

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
