package encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PublicKeyEncoder extends MessageToByteEncoder<String> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String publicKey, ByteBuf byteBuf) throws Exception {
        byteBuf.writeByte(0);
        byteBuf.writeBytes(publicKey.getBytes());
    }
}
