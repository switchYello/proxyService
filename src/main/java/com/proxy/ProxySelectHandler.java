package com.proxy;

import com.proxy.httpProxy.HttpProxyInit;
import com.proxy.socks.SocksProxyInit;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

//自己选http代理还是socks代理
public class ProxySelectHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf bb = (ByteBuf) msg;
        byte select = bb.getByte(0);
        if (select == 4 || select == 5) {
            ctx.pipeline().replace(ctx.name(), null, new SocksProxyInit());
        } else if (select == 'C' || select == 'G' || select == 'P') {
            ctx.pipeline().replace(ctx.name(), null, new HttpProxyInit());
        } else {
            System.out.println("未知请求");
            ctx.close();
        }
        ctx.fireChannelRead(msg);
    }


}
