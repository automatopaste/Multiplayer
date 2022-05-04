package data.scripts.net.client;

import data.scripts.net.RequestData;
import data.scripts.net.data.records.ARecord;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.lazywizard.console.Console;

import java.util.List;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Console.showMessage("Client received packet from server");

        ServerPacketReconstructor reconstructor = (ServerPacketReconstructor) msg;
        for (List<ARecord> unpackedEntity : reconstructor.getUnpacked()) {
            for (ARecord record : unpackedEntity) {
                Console.showMessage(record.toString());
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        RequestData request = new RequestData();
        request.setIntValue(1);
        request.setStringValue("we do a little trolling");

        ChannelFuture future = ctx.writeAndFlush(request);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Console.showMessage("Client handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Console.showMessage("Client handler removed");
    }
}
