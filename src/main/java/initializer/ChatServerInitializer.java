package initializer;

import decoder.EncryptionDecoder;
import encoder.EncryptionEncoder;
import handler.ChatServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;

public class ChatServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline channelPipeline = socketChannel.pipeline();
        channelPipeline
                .addFirst(new EncryptionDecoder())
                .addFirst(new LengthFieldBasedFrameDecoder(2048,0,4,0,4))

                .addLast(new ChatServerHandler())
                .addLast(new EncryptionEncoder());


    }
}
