package util;

import io.netty.channel.Channel;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum RoomDetail {
    INSTANCE;
    public Map<Integer, Map<PublicKey,Channel>> roomIdChannelsMap = new ConcurrentHashMap<Integer, Map<PublicKey, Channel>>();
    public Map<Channel,Integer> channelRoomIdMap = new ConcurrentHashMap<Channel, Integer>();
}
