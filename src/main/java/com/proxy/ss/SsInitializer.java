package com.proxy.ss;

import com.handlers.IdleStateHandlerImpl;
import com.start.Environment;
import com.utils.Conf;
import com.utils.Encrypt;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

public class SsInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        Conf conf = Environment.gitConfFromChannel(ch);
        //连接超时
        p.addLast(new IdleStateHandlerImpl(30, 30, 0));
        //加密解密方式
        p.addLast(new LoggingHandler("ss客户端请求流 密文"));
        p.addLast(Encrypt.get(conf.getEncrypt()));
        p.addLast(new LoggingHandler("ss客户端请求流明文"));
        p.addLast(new SsInitHandler());
        p.addLast(new SsServiceHandler());
    }
}
