package com.proxy.socks;

import com.handlers.ExceptionHandler;
import com.proxy.AbstractProxyInit;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;

public class SocksAbstractProxyInit extends AbstractProxyInit {

    @Override
    protected void initChannel(ChannelPipeline p) {

        //判断socks协议版本，添加相应的hander
        p.addLast(new SocksPortUnificationServerHandler());
        //处理验证逻辑，添加相应handler
        p.addLast(SocksServerHandler.INSTANCE);
        //
        p.addLast(ExceptionHandler.INSTANCE);

    }


}
