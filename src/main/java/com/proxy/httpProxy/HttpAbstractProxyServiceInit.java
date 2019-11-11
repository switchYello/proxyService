package com.proxy.httpProxy;

import com.proxy.abstractProxyInit;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;

/*
 * 作为普通http(s)代理服务器时初始化
 * */
public class HttpAbstractProxyServiceInit extends abstractProxyInit {

    private static HttpService httpService = new HttpService();

    @Override
    protected void initChannel(ChannelPipeline p) {
        p.addLast(new LoggingHandler());
        p.addLast("httpcode", new HttpServerCodec());
        p.addLast("objectAggregator", new HttpObjectAggregator(1024 * 1024));
        p.addLast(LoginHandler.INSTANCE);
        //真实处理类
        p.addLast("httpservice", httpService);
    }
}
