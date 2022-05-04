package data.scripts.net.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Reconstruct received packets from server
 */
public class ResponseDataDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        ResponseData data = new ResponseData();
//        data.setIntValue(in.readInt());
//        out.add(data);

        if (in.readableBytes() < 4) return;

        in.markReaderIndex();

        // wait for all data to arrive
        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        ServerPacketReconstructor reconstructor = new ServerPacketReconstructor();
        reconstructor.unpack(in, length);

        out.add(reconstructor);
    }
}
