package com.proxy.forwarder;

import com.handlers.ExceptionHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 此为透传工具初始化类
 */
public class ForwarderInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        p.addLast(new LoggingHandler("Forwarder客户端请求流"));
        p.addLast(ForwarderService.INSTANCE);
        p.addLast(ExceptionHandler.INSTANCE);
    }


}
