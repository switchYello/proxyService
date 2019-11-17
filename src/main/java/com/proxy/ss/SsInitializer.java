package com.proxy.ss;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

public class SsInitializer extends ChannelInitializer<Channel> {
	@Override
	protected void initChannel(Channel ch) {
		ChannelPipeline p = ch.pipeline();
		p.addLast(new SsInitHandler());
		p.addLast(new SsServiceHandler());
	}
}
