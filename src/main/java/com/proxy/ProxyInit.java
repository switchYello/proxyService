package com.proxy;

import com.handlers.AesGcmHandler;
import com.proxy.ss.SsInitializer;
import com.utils.Aes128Gcm;
import com.utils.ContextSSLFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

/*
 * 切换初始化程序
 * */
@ChannelHandler.Sharable
public class ProxyInit extends ChannelInitializer<Channel> {

    private final static SslContext context = ContextSSLFactory.getSslContextService();

    @Override
    public final void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        //连接超时
        p.addLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS));
        //p.addLast(new Rc4Handler());
        p.addLast(new AesGcmHandler(new Aes128Gcm()));
        //p.addLast("ssl", new SslHandler(context.newEngine(ByteBufAllocator.DEFAULT)));
        p.addLast(new LoggingHandler("客户端请求流"));
        //p.addLast(new SocksProxyInit());
        p.addLast(new SsInitializer());
        //p.addLast(new ProxySelectHandler());
        //p.addLast(new DiscadeHandler());
    }

}
