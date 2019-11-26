package com.proxy.forwarder;

import com.dns.AsnycDns;
import com.handlers.IdleStateHandlerImpl;
import com.handlers.TransferHandler;
import com.start.Environment;
import com.utils.Conf;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class ForwarderService extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(ForwarderService.class);
    public static ForwarderService INSTANCE = new ForwarderService();


    //尝试连接服务器。连接成功后进行读取
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        //这是main方法中设置进每个连接的conf属性，这里直接取出来用
        final Conf conf = Environment.getConfFromChannel(ctx.channel());
        ChannelFuture promise = createPromise(InetSocketAddress.createUnresolved(conf.getServerHost(), conf.getServerPort()), ctx);
        promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    log.debug("Forwarder客户端请求连接到服务器 {}:{}", conf.getServerHost(), conf.getServerPort());
                    ctx.pipeline().replace(ctx.name(), null, new TransferHandler(future.channel()));
                    ctx.channel().config().setAutoRead(true);
                } else {
                    log.debug("Forwarder连接服务器失败:", future.cause());
                    ctx.close();
                }
            }
        });
    }

    private ChannelFuture createPromise(final InetSocketAddress address, final ChannelHandlerContext ctx) {
        Bootstrap b = new Bootstrap();
        return b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .resolver(AsnycDns.INSTANCE)
                .remoteAddress(address)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new IdleStateHandlerImpl(30, 30, 0));
                        p.addLast(new LoggingHandler("Forwarder服务器连接流"));
                        p.addLast(new TransferHandler(ctx.channel()));
                    }
                })
                .connect();
    }

}
