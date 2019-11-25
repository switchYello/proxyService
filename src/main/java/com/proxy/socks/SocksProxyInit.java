package com.proxy.socks;

import com.handlers.IdleStateHandlerImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;

public class SocksProxyInit extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new IdleStateHandlerImpl(30, 30, 0));
        //判断socks协议版本，添加相应的hander
        p.addLast(new SocksPortUnificationServerHandler());
        //处理验证逻辑，添加相应handler
        p.addLast(SocksServerHandler.INSTANCE);

    }
}
