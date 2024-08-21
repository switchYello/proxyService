package com.proxy.httpproxy;

import com.start.Environment;
import com.utils.Loops;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import java.net.InetSocketAddress;

/**
 * @author xiaoming
 * 检测http代理是否登录，验证账号密码
 */
@Slf4j
@ChannelHandler.Sharable
public class LoginHandler extends ChannelInboundHandlerAdapter {

    public static LoginHandler INSTANCE = new LoginHandler();

    //验证通过则移除this,fire消息
    //否则，发送验证指令
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof FullHttpRequest)) {
            log.info("无法识别的数据{}", msg);
            ReferenceCountUtil.release(msg);
        }
        //密码验证
        FullHttpRequest request = (FullHttpRequest) msg;
        if (!PasswordChecker.digestLogin(request)) {
            ReferenceCountUtil.release(msg);
            ctx.writeAndFlush(PasswordChecker.getDigestNotLoginResponse());
        }
        ctx.pipeline().remove(this);
        ctx.fireChannelRead(msg);
    }


    //从请求头host字段解析出目标网站的host和端口
    private InetSocketAddress resolveHostPort(String headerHost) {
        String[] split = headerHost.split(":");
        String host = split[0];
        int port = 80;
        if (split.length > 1) {
            port = Integer.parseInt(split[1]);
        }
        return InetSocketAddress.createUnresolved(host, port);
    }

    static Mono<? extends Connection> getConn(String host, int port) {
        return TcpClient.newConnection()
                .runOn(Loops.ssLoopResources)
                .wiretap("HTTp-PROXY-CLIENT", Environment.level, Environment.format)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000)
                .host(host)
                .port(port)
                .connect()
                .single();
    }

}