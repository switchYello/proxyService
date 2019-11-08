package com.httpservice;

import com.utils.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 交换输入输出
 */
public class TransferHandler extends ChannelInboundHandlerAdapter {

    private Channel outChannel;

    public TransferHandler(Channel outChannel) {
        this.outChannel = outChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        outChannel.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        outChannel.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelUtil.closeOnFlush(outChannel);
        super.channelInactive(ctx);
    }
}