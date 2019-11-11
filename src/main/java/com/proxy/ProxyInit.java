package com.proxy;

import com.handlers.ExceptionHandler;
import com.handlers.Rc4Handler;
import com.utils.ContextSSLFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public abstract class ProxyInit extends ChannelInitializer<Channel> {

    private final static SslContext context = ContextSSLFactory.getSslContextService();

    @Override
    public final void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        //连接超时
        p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        p.addLast(new Rc4Handler());
        //p.addLast("ssl", new SslHandler(context.newEngine(ByteBufAllocator.DEFAULT)));
        initChannel(p);
        //处理超时事件，和异常
        p.addLast("heartbeat", ExceptionHandler.INSTANCE);
    }

    protected abstract void initChannel(ChannelPipeline p);

}
