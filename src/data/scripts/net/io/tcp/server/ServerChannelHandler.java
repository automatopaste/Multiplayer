package data.scripts.net.io.tcp.server;

import data.scripts.net.data.records.BaseRecord;
import data.scripts.net.io.ServerConnectionWrapper;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.util.Map;

public class ServerChannelHandler extends SimpleChannelInboundHandler<Unpacked> {
    private final ServerConnectionWrapper serverConnectionWrapper;
    private final SocketServer socketServer;

    public ServerChannelHandler(ServerConnectionWrapper serverConnectionWrapper, SocketServer socketServer) {
        this.serverConnectionWrapper = serverConnectionWrapper;
        this.socketServer = socketServer;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        serverConnectionWrapper.stop();
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
        //Console.showMessage("Received TCP client tick notice: " + clientTick);

        Map<Byte, Map<Short, Map<Byte, BaseRecord<?>>>> entities = unpacked.getUnpacked();

        serverConnectionWrapper.updateInbound(entities);
    }

    /**
     * Called once when TCP connection is active
     * @param ctx context
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws IOException, InterruptedException {
        Console.showMessage("Channel active on server");

        socketServer.getChannelGroup().add(ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * Called once read is complete. Is used to wait and send next packet.
     * @param ctx context
     */
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws IOException, InterruptedException {

    }
}
