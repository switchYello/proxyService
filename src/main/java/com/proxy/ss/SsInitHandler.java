package com.proxy.ss;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socksx.v5.Socks5AddressDecoder;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.util.Signal;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * hcy 2019/11/17
 * 首次接受到数据时,根据首byte,解析出域名端口,并创建连接
 * 后面访问时,直接转发不处理
 *
 */
public class SsInitHandler extends ReplayingDecoder<SsInitHandler.Status> {

	private final Socks5AddressDecoder addressDecoder;

	enum Status {
		init, success, complate;
	}

	public SsInitHandler() {
		state(Status.init);
		this.addressDecoder = Socks5AddressDecoder.DEFAULT;
	}

	//开始时读取一次数据
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.read();
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		try {
			switch (state()) {
				case init: {
					Socks5AddressType socks5AddressType = Socks5AddressType.valueOf(in.readByte());
					String host = addressDecoder.decodeAddress(socks5AddressType, in);
					int port = in.readUnsignedShort();
					out.add(InetSocketAddress.createUnresolved(host, port));
					checkpoint(Status.success);
				}
				case success: {
					int readableBytes = actualReadableBytes();
					if (readableBytes > 0) {
						out.add(in.readRetainedSlice(readableBytes));
					}
					checkpoint(Status.complate);
					break;
				}
				case complate:
					ctx.pipeline().remove(this);
			}
		} catch (Signal replay) {
			ctx.read();
			throw replay;
		}
	}
}