package com.handlers;

import com.start.Environment;
import com.utils.KeyUtil;
import com.utils.Rc4Md5;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Rc4Handler extends ByteToMessageCodec<ByteBuf> {
    //全局原始密码
    private static byte[] password = Environment.getPassWord().getBytes(StandardCharsets.UTF_8);
    private Rc4Md5 rc4 = null;
    //是否是第一次编码，第一次解码
    private boolean firstEncode = true;
    private boolean firstDecode = true;
    //编码接收到的iv，为了兼容手机端app，解码也使用这个iv，理论上应该是不同的
    private byte[] encodeIv;
    private byte[] decodeIv;
    private byte[] decodeKey;
    private byte[] encodeKey;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        byte[] origin = readByte(msg, msg.readableBytes());
        if (firstEncode) {
            /*
             * encodeIv默认在decode时会设置，如果先调用encode方法，则随机生成encodeIv
             * */
            if (encodeIv == null) {
                encodeIv = KeyUtil.randomBytes(16);
            }
            if (rc4 == null) {
                rc4 = new Rc4Md5();
            }
            out.writeBytes(encodeIv);
//           生成encodeKey时，如果两个iv相同，则直接拷贝key而不是创建新的
            encodeKey = encodeIv == decodeIv ? decodeKey : KeyUtil.md5IvKey(password, encodeIv);
            firstEncode = false;
        }
        out.writeBytes(rc4.encoder(encodeKey, origin));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//		此处如果rc4没初始化则初始化，但只在第一次访问且数据大于16时会初始
// 		编码解码只有第一个包才进行添加iv操作，其他的不需要添加
        if (firstDecode) {
            if (in.readableBytes() < 16) {
                return;
            }
            decodeIv = readByte(in, 16);
            decodeKey = KeyUtil.md5IvKey(password, decodeIv);
            if (encodeIv == null) {
                encodeIv = decodeIv;
            }
            if (rc4 == null) {
                rc4 = new Rc4Md5();
            }
            firstDecode = false;
        }
        byte[] origin = readByte(in, in.readableBytes());
        out.add(Unpooled.wrappedBuffer(rc4.decoder(decodeKey, origin)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        rc4.finishEncoder();
        rc4.finishDecoder();
    }

    private byte[] readByte(ByteBuf in, int length) {
        byte[] bytes = ByteBufUtil.getBytes(in, in.readerIndex(), length);
        in.skipBytes(bytes.length);
        return bytes;
    }


}
