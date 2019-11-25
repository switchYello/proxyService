package com.proxy.httpProxy;

import com.handlers.IdleStateHandlerImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/*
 * 作为普通http(s)代理服务器时初始化
 * */
public class HttpProxyInit extends ChannelInitializer<Channel> {

    private static HttpService httpService = new HttpService();

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new IdleStateHandlerImpl(30, 30, 0));
        p.addLast("httpcode", new HttpServerCodec());
        p.addLast("objectAggregator", new HttpObjectAggregator(1024 * 1024));
        p.addLast(LoginHandler.INSTANCE);
        //真实处理类
        p.addLast("httpservice", httpService);
    }
}
