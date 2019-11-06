package com.start;


import com.httpservice.TransferHandler;
import com.utils.ChannelUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

/**
 * @author xiaoming
 */
public class PromiseProvide {

    private static Bootstrap b = new Bootstrap();

    static {
        b.channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }


    public Promise<Channel> createPromise(final InetSocketAddress address, final ChannelHandlerContext ctx) {
        final Promise<Channel> promise = ctx.executor().newPromise();
        b.clone(ctx.channel().eventLoop())
                .remoteAddress(address)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new TransferHandler(ctx.channel()));
                    }
                })
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) {
                        if (channelFuture.isSuccess()) {
                            promise.setSuccess(channelFuture.channel());
                        } else {
                            channelFuture.cancel(false);
                            ChannelUtil.closeOnFlush(ctx.channel());
                        }
                    }
                });
        return promise;
    }
}
