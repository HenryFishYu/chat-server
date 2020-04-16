package handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.SimpleChannelInboundHandler;
import util.EncryptionUtil;
import util.RoomDetail;

import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        int messageType = byteBuf.readByte();
        switch (messageType){
            case 0: // new client register
                int roomId = byteBuf.readInt();
                byte[] publicKeyBytes = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(publicKeyBytes);
                PublicKey publicKey = EncryptionUtil.getPublicKeyFromBytes(publicKeyBytes);
                RoomDetail.INSTANCE.channelRoomIdMap.put(ctx.channel(),roomId);
                if(RoomDetail.INSTANCE.roomIdChannelsMap.get(roomId)!=null){
                    for(Map.Entry<PublicKey,Channel> entry : RoomDetail.INSTANCE.roomIdChannelsMap.get(roomId).entrySet()){ // dispatch all the public key to each other
                        ByteBuf toRegisteredClientsByteBuf = new UnpooledUnsafeDirectByteBuf(ByteBufAllocator.DEFAULT,512,2048);
                        ByteBuf toNewClientsByteBuf = new UnpooledUnsafeDirectByteBuf(ByteBufAllocator.DEFAULT,512,2048);
                        toRegisteredClientsByteBuf.writeByte(1);         // Message type 1 : Server send publicKey, Client receive publicKey
                        toRegisteredClientsByteBuf.writeBytes(publicKey.getEncoded());
                        toNewClientsByteBuf.writeByte(1);
                        toNewClientsByteBuf.writeBytes(entry.getKey().getEncoded());
                        ctx.channel().writeAndFlush(toNewClientsByteBuf);
                        entry.getValue().writeAndFlush(toRegisteredClientsByteBuf);
                    }
                    RoomDetail.INSTANCE.roomIdChannelsMap.get(roomId).put(publicKey,ctx.channel());
                    break;
                }
                if(RoomDetail.INSTANCE.roomIdChannelsMap.get(roomId)==null){
                    RoomDetail.INSTANCE.roomIdChannelsMap.put(roomId,new ConcurrentHashMap<PublicKey, Channel>());
                    RoomDetail.INSTANCE.roomIdChannelsMap.get(roomId).put(publicKey,ctx.channel());
                    break;
                }
                break;
            case 16:
                int chatRoomId = RoomDetail.INSTANCE.channelRoomIdMap.get(ctx.channel());
                byte[] destinationPublicKeyBytes = new byte[162];
                byteBuf.readBytes(destinationPublicKeyBytes);
                byteBuf.resetReaderIndex();
                ByteBuf sendByteBuf = new UnpooledUnsafeDirectByteBuf(ByteBufAllocator.DEFAULT,512,2048);
                sendByteBuf.writeBytes(byteBuf);
                RoomDetail.INSTANCE.roomIdChannelsMap.get(chatRoomId).get(EncryptionUtil.getPublicKeyFromBytes(destinationPublicKeyBytes)).writeAndFlush(sendByteBuf);
                break;
            default:
                System.out.println("Illegal message!");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {


    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        int roomId = RoomDetail.INSTANCE.channelRoomIdMap.get(ctx.channel());
        PublicKey leftPublicKey = null;
        for(Map.Entry<PublicKey,Channel> entry : RoomDetail.INSTANCE.roomIdChannelsMap.get(roomId).entrySet()) {
            if(entry.getValue().equals(ctx.channel())){
                leftPublicKey = entry.getKey();
                RoomDetail.INSTANCE.channelRoomIdMap.remove(ctx.channel());
                RoomDetail.INSTANCE.roomIdChannelsMap.get(roomId).remove(entry.getKey());
                System.out.println(entry.getKey());
                System.out.println("has left room "+roomId);
                break;
            }
        }
        for(Map.Entry<PublicKey,Channel> entry : RoomDetail.INSTANCE.roomIdChannelsMap.get(roomId).entrySet()) {
            ByteBuf leaveMessageByteBuf = new UnpooledUnsafeDirectByteBuf(ByteBufAllocator.DEFAULT,512,2048);
            leaveMessageByteBuf.writeByte(2); // Message Type 2 : User Leave
            leaveMessageByteBuf.writeBytes(leftPublicKey.getEncoded());
            entry.getValue().writeAndFlush(leaveMessageByteBuf);
        }
        RoomDetail.INSTANCE.channelRoomIdMap.remove(ctx.channel());

    }
}
