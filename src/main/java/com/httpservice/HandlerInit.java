package com.httpservice;

import com.start.PromiseProvide;
import com.utils.ContextSSLFactory;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/*
 * 禁止没在IP地址白名单中的ip访问
 * */

public class HandlerInit extends ChannelInitializer<Channel> {

    private final static SslContext context = ContextSSLFactory.getSslContextService();
    private static HttpService httpService = new HttpService(new PromiseProvide());

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        p.addLast("ssl", new SslHandler(context.newEngine(ByteBufAllocator.DEFAULT)));
        p.addLast("login", new LoginHandler());
        p.addLast("httpservice", httpService);
        p.addLast("exceptionHandler", new ExceptionHandler());
    }


}
