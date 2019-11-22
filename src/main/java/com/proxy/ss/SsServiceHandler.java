package com.proxy.ss;

import com.dns.AsnycDns;
import com.handlers.ExceptionHandler;
import com.handlers.TransferHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class SsServiceHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(SsServiceHandler.class);
    //初始化为voidPromise，因为第一次read一定会将他初始化，如果没初始化就是异常的
    private ChannelFuture promise;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        promise = ctx.voidPromise();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof SocketAddress) {
            Bootstrap b = new Bootstrap();
            promise = b.group(ctx.channel().eventLoop())
                    .channel(NioSocketChannel.class)
                    .resolver(AsnycDns.INSTANCE)
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .option(ChannelOption.AUTO_READ, false)
                    .remoteAddress((SocketAddress) msg)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) {
                            ChannelPipeline p = channel.pipeline();
                            p.addLast(new LoggingHandler("与网站连接"));
                            p.addLast(new TransferHandler(ctx.channel(), false));
                            p.addLast(ExceptionHandler.INSTANCE);
                        }
                    }).connect().addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) {
//							连接外网成功第一时间读取数据
                            if (future.isSuccess()) {
                                ctx.read();
                                future.channel().read();
                            } else {
                                log.info("连接外网失败", future.cause());
                                ctx.close();
                            }
                        }
                    });
        } else if (msg instanceof ByteBuf) {
            promise.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.pipeline().replace(ctx.name(), null, new TransferHandler(future.channel(), false));
                        ctx.fireChannelRead(msg);
                    }
                }
            });
        } else {
            throw new RuntimeException("未知的数据类型");
        }
    }
}
