package com.utils;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 刷新并关闭流
 */
public class ChannelUtil {

    public static void closeOnFlush(Channel ch) {
        if (ch != null && ch.isActive()) {
            //ch.close();
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
