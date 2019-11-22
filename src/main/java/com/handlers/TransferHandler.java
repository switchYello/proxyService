package com.handlers;

import com.utils.ChannelUtil;
import com.utils.SuccessFutureListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 交换输入输出
 */
public class TransferHandler extends ChannelInboundHandlerAdapter {

    //读取数据写入这个channel里
    private Channel outChannel;
    //是否是自动读取，如果不是自动读取，则需要写完后手动read
    private boolean autoRead = true;

    public TransferHandler(Channel outChannel) {
        this(outChannel, true);
    }

    public TransferHandler(Channel outChannel, boolean autoRead) {
        this.outChannel = outChannel;
        this.autoRead = autoRead;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (!autoRead) {
            ctx.read();
        }
        super.channelActive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (outChannel.isActive()) {
            ChannelFuture writeFuture = outChannel.writeAndFlush(msg);
            //如果不是自动read，则手动read
            if (!autoRead) {
                writeFuture.addListener(new SuccessFutureListener<Void>() {
                    @Override
                    public void operationComplete0(Void v) {
                        ctx.read();
                    }
                });
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelUtil.closeOnFlush(outChannel);
        super.channelInactive(ctx);
    }
}
