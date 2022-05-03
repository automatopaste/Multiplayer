package data.scripts.net.server;

import data.scripts.net.RequestData;
import data.scripts.net.ResponseData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.lazywizard.console.Console;

public class ProcessingHandler extends ChannelInboundHandlerAdapter {
    private ByteBuf tmp;

    private int tick = 0;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Console.showMessage("Server channel handler added");
        tmp = ctx.alloc().buffer(4);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Console.showMessage("Server channel handler removed");
        tmp.release();
        tmp = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RequestData requestData = (RequestData) msg;
        Console.showMessage("Server received request data: " + requestData.toString());

        tick = requestData.getIntValue();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("lets go", CharsetUtil.UTF_8));
        ctx.fireChannelActive();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ResponseData responseData = new ResponseData();
        responseData.setIntValue(tick + 1);

        ChannelFuture future = ctx.writeAndFlush(responseData);
    }
}
