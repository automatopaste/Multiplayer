package data.scripts.net.client;

import data.scripts.net.RequestData;
import data.scripts.net.ResponseData;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.lazywizard.console.Console;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private int tick = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Console.showMessage("Client received message from server: " + msg.toString());
        tick = ((ResponseData) msg).getIntValue();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        RequestData request = new RequestData();
        request.setIntValue(tick + 1);
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
