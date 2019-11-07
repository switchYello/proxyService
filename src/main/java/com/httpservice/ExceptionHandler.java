package com.httpservice;

import com.utils.ChannelUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = LoggerFactory.getLogger(ExceptionHandler.class);
    public static ExceptionHandler INSTANCE = new ExceptionHandler();

    private ExceptionHandler() {
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ChannelUtil.closeOnFlush(ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.debug("exceptionCaught", cause);
        ChannelUtil.closeOnFlush(ctx.channel());
    }
}
