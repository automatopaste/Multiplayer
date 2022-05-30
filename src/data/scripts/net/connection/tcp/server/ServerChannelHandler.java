package data.scripts.net.connection.tcp.server;

import data.scripts.net.connection.ServerConnectionWrapper;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.lazywizard.console.Console;

import java.io.IOException;

public class ServerChannelHandler extends SimpleChannelInboundHandler<Unpacked> {
    private final ServerConnectionWrapper serverConnectionWrapper;

    public ServerChannelHandler(ServerConnectionWrapper serverConnectionWrapper) {
        this.serverConnectionWrapper = serverConnectionWrapper;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Console.showMessage("Server channel handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Console.showMessage("Server channel handler removed");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Unpacked unpacked) {
        int clientTick = unpacked.getTick();
        Console.showMessage("Received TCP client tick notice: " + clientTick);

        // DISCARD WHILE DEBUG
//        Map<Integer, BasePackable> entities = unpacked.getUnpacked();
//
//        serverConnectionWrapper.updateInbound(entities);
    }

    /**
     * Called once when TCP connection is active
     * @param ctx context
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws IOException, InterruptedException {
        Console.showMessage("Channel active on server");
    }

    /**
     * Called once read is complete. Is used to wait and send next packet.
     * @param ctx context
     */
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws IOException, InterruptedException {

    }
}
