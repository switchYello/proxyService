package com.start;


import com.handlers.ExceptionHandler;
import com.handlers.TransferHandler;
import io.netty.channel.*;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

/**
 * @author xiaoming
 */
public class PromiseProvide {

    private Context c = Context.getNow();

    public Promise<Channel> createPromise(final InetSocketAddress address, final ChannelHandlerContext ctx) {
        final Promise<Channel> promise = ctx.executor().newPromise();
        c.createBootStrap(ctx.channel().eventLoop())
                .remoteAddress(address)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new TransferHandler(ctx.channel()));
                        p.addLast(ExceptionHandler.INSTANCE);
                    }
                })
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) {
                        if (channelFuture.isSuccess()) {
                            promise.setSuccess(channelFuture.channel());
                        } else {
                            ctx.fireExceptionCaught(channelFuture.cause());
                        }
                    }
                });
        return promise;
    }
}
