package com.proxy.ss;

import com.utils.ChannelUtil;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 交换输入输出
 */
public class SSTransferHandler extends ChannelInboundHandlerAdapter {


	private static Logger log = LoggerFactory.getLogger(SSTransferHandler.class);
	private Channel outChannel;
	public SSTransferHandler(Channel outChannel) {
		this.outChannel = outChannel;
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) {
		if (outChannel.isActive()) {
			outChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						ctx.read();
					} else {
						log.info("写入失败原因如下",future.cause());
					}
				}
			});
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ChannelUtil.closeOnFlush(outChannel);
		super.channelInactive(ctx);
	}
}
