package com.proxy.socks;

import com.proxy.socks.socks4.Socks4ServerConnectHandler;
import com.proxy.socks.socks5.Socks5ServerConnectHandler;
import com.utils.ChannelUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandType;
import io.netty.handler.codec.socksx.v5.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@ChannelHandler.Sharable
public class SocksServerHandler extends SimpleChannelInboundHandler<SocksMessage> {

    public static SocksServerHandler INSTANCE = new SocksServerHandler();

    private static Logger log = LoggerFactory.getLogger(SocksServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksMessage msg) throws Exception {
        if (msg.decoderResult() != DecoderResult.SUCCESS) {
            ctx.fireExceptionCaught(msg.decoderResult().cause());
        }
        switch (msg.version()) {
            case SOCKS5:
                //获取客户端的所有加密方式，选择一种加密方式回复，并在前面添加处理类
                if (msg instanceof Socks5InitialRequest) {
                    //ctx.pipeline().addBefore(ctx.name(), null, new Socks5PasswordAuthRequestDecoder());
                    //ctx.write(new DefaultSocks5AuthMethodResponse(Socks5AuthMethod.PASSWORD));
                    ctx.pipeline().addBefore(ctx.name(), null, new Socks5CommandRequestDecoder());
                    ctx.writeAndFlush(new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD));
                    //如果支持账号密码认证，则使用此处，但是chrome不支持，暂时不写认证部分吧
                } else if (msg instanceof Socks5PasswordAuthRequest) {
                    ctx.pipeline().addBefore(ctx.name(), null, new Socks5CommandRequestDecoder());
                    ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS));
                }
                //判断连接是否是CONNECT，并将数据传到下一个handler处理
                else if (msg instanceof Socks5CommandRequest) {
                    Socks5CommandRequest socks5CmdRequest = (Socks5CommandRequest) msg;
                    if (socks5CmdRequest.type() == Socks5CommandType.CONNECT) {
                        ctx.pipeline().addAfter(ctx.name(), null, new Socks5ServerConnectHandler());
                        ctx.pipeline().remove(this);
                        ctx.fireChannelRead(socks5CmdRequest);
                    } else {
                        ctx.close();
                    }
                }
                break;
            case SOCKS4a:
                Socks4CommandRequest socksV4CmdRequest = (Socks4CommandRequest) msg;
                if (socksV4CmdRequest.type() == Socks4CommandType.CONNECT) {
                    ctx.pipeline().addAfter(ctx.name(), null, new Socks4ServerConnectHandler());
                    ctx.pipeline().remove(this);
                    ctx.fireChannelRead(socksV4CmdRequest);
                } else {
                    ctx.close();
                }
                break;
            case UNKNOWN:
                ctx.close();
                break;
            default:
                ctx.close();
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.debug("SocksServerHandler", cause);
        ChannelUtil.closeOnFlush(ctx.channel());
    }

}
