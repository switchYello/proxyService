package com.proxy.forwarder;

import com.handlers.IdleStateHandlerImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

/**
 * 此为透传工具初始化类
 */
public class ForwarderInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new IdleStateHandlerImpl(30, 30, 0));
        p.addLast(new LoggingHandler("Forwarder客户端请求流"));
        p.addLast(ForwarderService.INSTANCE);
    }


}
