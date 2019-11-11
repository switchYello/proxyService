package com.httpProxy;

import com.handlers.ExceptionHandler;
import com.handlers.Rc4Handler;
import com.start.PromiseProvide;
import com.utils.ContextSSLFactory;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/*
 * 作为普通http(s)代理服务器时初始化
 * */
public class ProxyServiceInit extends ChannelInitializer<Channel> {

    private final static SslContext context = ContextSSLFactory.getSslContextService();
    private static HttpService httpService = new HttpService(new PromiseProvide());

    @Override
    protected void initChannel(Channel channel) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        ChannelPipeline p = channel.pipeline();
        //连接超时
        p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        p.addLast(new Rc4Handler());
        p.addLast(new LoggingHandler());
        //p.addLast("ssl", new SslHandler(context.newEngine(ByteBufAllocator.DEFAULT)));
        //http解码，解码出ip 端口，或者处理https
        p.addLast("httpcode", new HttpServerCodec());
        p.addLast("objectAggregator", new HttpObjectAggregator(1024 * 1024));
        p.addLast(LoginHandler.INSTANCE);
        //真实处理类
        p.addLast("httpservice", httpService);
        //处理超时事件，和异常
        p.addLast("heartbeat", ExceptionHandler.INSTANCE);
    }
}
