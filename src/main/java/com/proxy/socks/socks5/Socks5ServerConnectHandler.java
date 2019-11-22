package com.proxy.socks.socks5;

import com.dns.AsnycDns;
import com.handlers.TransferHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class Socks5ServerConnectHandler extends SimpleChannelInboundHandler<Socks5CommandRequest> {

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Socks5CommandRequest msg) throws Exception {
        String host = msg.dstAddr();
        int port = msg.dstPort();
        Bootstrap b = new Bootstrap();
        ChannelFuture promise = b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .resolver(AsnycDns.INSTANCE)
                .remoteAddress(host, port)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new LoggingHandler("socks5网站链接流"))
                .connect();
        promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    ctx.writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, msg.dstAddrType(), msg.dstAddr(), msg.dstPort()));
                    future.channel().pipeline().addLast(new TransferHandler(ctx.channel()));
                    ctx.pipeline().replace(ctx.name(), null, new TransferHandler(future.channel()));
                } else {
                    ctx.writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, msg.dstAddrType(), msg.dstAddr(), msg.dstPort()));
                    ctx.close();
                }
            }
        });
    }


}
