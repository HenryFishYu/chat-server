package encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import util.EncryptionUtil;

public class EncryptionEncoder extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf sourceByteBuf, ByteBuf byteBuf) throws Exception {
        long time = System.currentTimeMillis();
        byteBuf.writeInt(sourceByteBuf.readableBytes()+12);      // 4 bytes package length
        byteBuf.writeLongLE(time);                               // 8 bytes time
        EncryptionUtil.encryptBytesByLongTime(byteBuf,time);     // 4 bytes encryption
        byteBuf.writeBytes(sourceByteBuf);
    }
}
