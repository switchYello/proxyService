package com.handlers;

import com.utils.Rc4Md5;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class Rc4Handler extends ByteToMessageCodec<ByteBuf> {

	private Rc4Md5 rc4 = null;
	private boolean firstEncode = true;
	private boolean firstDecode = true;

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
		byte[] origin = ByteBufUtil.getBytes(msg);
		msg.skipBytes(origin.length);
		if (firstEncode) {
			out.writeBytes(rc4.getIv());
			firstEncode = false;
		}
		out.writeBytes(rc4.encoder(origin));
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//		此处如果rc4没初始化则初始化，但只在第一次访问且数据大于16时会初始
// 		编码解码只有第一个包才进行添加iv操作，其他的不需要添加
		if (firstDecode) {
			if (in.readableBytes() < 16) {
				return;
			}
			rc4 = new Rc4Md5(ByteBufUtil.getBytes(in, in.readerIndex(), 16));
			in.skipBytes(16);
			firstDecode = false;
		}
		byte[] origin = ByteBufUtil.getBytes(in);
		in.skipBytes(origin.length);
		out.add(Unpooled.wrappedBuffer(rc4.decoder(origin)));
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		rc4.finishEncoder();
		rc4.finishDecoder();
	}

}
