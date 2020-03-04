package com.proxy.forwarder;

import com.handlers.TimeOutHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 此为透传工具初始化类
 */
public class ForwarderInitializer extends ChannelInitializer<Channel> {

    private static Logger log = LoggerFactory.getLogger(ForwarderInitializer.class);
    public static ForwarderInitializer INSTANCE = new ForwarderInitializer();

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new TimeOutHandler(30, 30, 0));
        if (log.isDebugEnabled()) {
            p.addLast(new LoggingHandler("Forwarder客户端请求流"));
        }
        p.addLast(ForwarderService.INSTANCE);
    }


}
