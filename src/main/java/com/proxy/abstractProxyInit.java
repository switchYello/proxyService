package com.proxy;

import com.handlers.Rc4Handler;
import com.utils.ContextSSLFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public abstract class abstractProxyInit extends ChannelInitializer<Channel> {

    private final static SslContext context = ContextSSLFactory.getSslContextService();

    @Override
    public final void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        //连接超时
        p.addLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS));
        p.addLast(new Rc4Handler());
        //p.addLast("ssl", new SslHandler(context.newEngine(ByteBufAllocator.DEFAULT)));
        p.addLast(new LoggingHandler());
        initChannel(p);

    }

    protected abstract void initChannel(ChannelPipeline p);

}
