package com.proxy.ss;

import com.handlers.IdleStateHandlerImpl;
import com.start.Environment;
import com.utils.Conf;
import com.utils.Encrypt;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsInitializer extends ChannelInitializer<Channel> {
    private static Logger log = LoggerFactory.getLogger(SsInitHandler.class);
    private static boolean isDebug = log.isDebugEnabled();

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        Conf conf = Environment.gitConfFromChannel(ch);
        //连接超时
        p.addLast(new IdleStateHandlerImpl(30, 30, 0));
        //加密解密方式
        if (isDebug) {
            p.addLast(new LoggingHandler("ss客户端请求流 密文"));
        }
        p.addLast(Encrypt.get(conf.getEncrypt()));
        if (isDebug) {
            p.addLast(new LoggingHandler("ss客户端请求流明文"));
        }
        p.addLast(new SsInitHandler());
        p.addLast(new SsServiceHandler());
    }
}
