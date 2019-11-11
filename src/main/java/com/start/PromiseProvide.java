package com.start;


import com.dns.AsnycDns;
import com.handlers.ExceptionHandler;
import com.handlers.TransferHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author xiaoming
 */
public class PromiseProvide {
    
    public ChannelFuture createPromise(final InetSocketAddress address, final ChannelHandlerContext ctx) {
        Bootstrap b = new Bootstrap();
        return b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .resolver(AsnycDns.INSTANCE)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .remoteAddress(address)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new TransferHandler(ctx.channel()));
                        p.addLast(ExceptionHandler.INSTANCE);
                    }
                })
                .connect();
    }
}
