package com.httpservice;

import com.start.PromiseProvide;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class HttpService extends SimpleChannelInboundHandler<InetSocketAddress> {

    private static Logger log = LoggerFactory.getLogger(HttpService.class);
    private PromiseProvide promiseProvide;

    public HttpService(PromiseProvide promiseProvide) {
        this.promiseProvide = promiseProvide;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InetSocketAddress address) {
        ChannelPipeline p = ctx.pipeline();
        long start = System.currentTimeMillis();
        Promise<Channel> promise = promiseProvide.createPromise(address, ctx);
        //log.info("创建{}连接到连接完成共花费:{},是否成功:{}", address.getHostName(), System.currentTimeMillis() - start, channelFuture.isSuccess());
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