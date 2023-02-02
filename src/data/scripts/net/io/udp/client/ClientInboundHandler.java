package data.scripts.net.io.udp.client;

import cmu.CMUtils;
import cmu.plugins.debug.DebugGraphContainer;
import data.scripts.net.io.ClientConnectionWrapper;
import data.scripts.net.io.ServerConnectionManager;
import data.scripts.net.io.Unpacked;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

public class ClientInboundHandler extends SimpleChannelInboundHandler<Unpacked> {
    private final ClientConnectionWrapper connection;

    private final DebugGraphContainer dataGraph;

    public ClientInboundHandler(ClientConnectionWrapper connection) {
        this.connection = connection;

        dataGraph = new DebugGraphContainer("Inbound Packet Size", ServerConnectionManager.TICK_RATE * 2, 60f);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Unpacked in) throws Exception {
        int serverTick = in.getTick();
        //Console.showMessage("Received UDP unpacked with tick: " + serverTick);

        // DISCARD WHILE DEBUG
        Map<Byte, Map<Short, Map<Byte, Object>>> entities = in.getUnpacked();

        dataGraph.increment(in.getSize());
        CMUtils.getGuiDebug().putContainer(ClientInboundHandler.class, "dataGraph", dataGraph);

        connection.updateInbound(entities, serverTick);
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
