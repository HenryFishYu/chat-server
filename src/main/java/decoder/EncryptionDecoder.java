package decoder;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import util.EncryptionUtil;

import java.util.List;

public class EncryptionDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> ByteBufList) throws Exception {
        while(byteBuf.isReadable(12)){
            long time = byteBuf.readLongLE();
            if(Math.abs(time-System.currentTimeMillis())>24*60*60*1000){ // if client timestamp is illegal
                byteBuf.readerIndex(byteBuf.readableBytes());
                return;
            }
            if(!EncryptionUtil.checkShortsAndLongTime(byteBuf.readShort(),byteBuf.readShort(),time)){ // if encryption is illegal
                byteBuf.readerIndex(byteBuf.readableBytes());
                return;
            }
            ByteBuf sendByteBuf = new UnpooledUnsafeDirectByteBuf(ByteBufAllocator.DEFAULT,512,2048);
            sendByteBuf.writeBytes(byteBuf);
            ByteBufList.add(sendByteBuf);
        }
    }
}
