package com.httpservice;

import com.start.PromiseProvide;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class HttpService extends SimpleChannelInboundHandler<InetSocketAddress> {

    private PromiseProvide promiseProvide;

    public HttpService(PromiseProvide promiseProvide) {
        this.promiseProvide = promiseProvide;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InetSocketAddress address) {
        ChannelPipeline p = ctx.pipeline();
        Promise<Channel> promise = promiseProvide.createPromise(address, ctx);
        promise.addListener(new FutureListener<Channel>() {
            @Override
            public void operationComplete(Future<Channel> channelFuture) {
                if (channelFuture.isSuccess()) {
                    p.remove(HttpService.this);
                    p.addLast(new TransferHandler(channelFuture.getNow()));
                    ctx.writeAndFlush(Unpooled.copyBoolean(true));
                }
            }
        });
    }
}
