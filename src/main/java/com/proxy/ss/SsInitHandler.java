package com.proxy.ss;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socksx.v5.Socks5AddressDecoder;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.util.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * hcy 2019/11/17
 * 首次接受到数据时,根据首byte,解析出域名端口,并创建连接
 * 后面访问时,直接转发不处理
 */
public class SsInitHandler extends ReplayingDecoder<SsInitHandler.Status> {

    private static Logger log = LoggerFactory.getLogger(SsInitHandler.class);
    private final Socks5AddressDecoder addressDecoder;

    enum Status {
        init, success, complate, err;
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
                    //根据首字母判断类型,规则和socks5一致
                    Socks5AddressType socks5AddressType = Socks5AddressType.valueOf(in.readByte());
                    String host = addressDecoder.decodeAddress(socks5AddressType, in);
                    int port = in.readUnsignedShort();
                    out.add(InetSocketAddress.createUnresolved(host, port));
                    checkpoint(Status.success);
                }
                //为什么要有这一步而不是直接移除当前handler呢，因为netty有个bug，直接移除会导致后面的字节乱序
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
                    break;
                case err:
                    in.skipBytes(in.readableBytes());
                    break;
            }
        } catch (Signal replay) {
            //数据量不够就主动读取
            ctx.read();
            throw replay;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.debug("报错忽略剩余所有字节", cause);
        state(Status.err);
    }
}
