package data.scripts.net.io.tcp.client;

import data.scripts.net.data.InboundData;
import data.scripts.net.io.ClientConnectionWrapper;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.lazywizard.console.Console;

/**
 * Main logic for handling network packet data
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<Unpacked> {
    private final ClientConnectionWrapper connection;

    public ClientChannelHandler(ClientConnectionWrapper connection) {
        this.connection = connection;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        connection.stop();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Console.showMessage("Channel active on client");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Unpacked unpacked) {
        int serverTick = unpacked.getTick();
        //Console.showMessage("Received TCP unpacked with tick: " + serverTick);

        InboundData entities = unpacked.getUnpacked();

        connection.updateInbound(entities, -1);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Console.showMessage("Client channel handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Console.showMessage("Client channel handler removed");
    }
}
