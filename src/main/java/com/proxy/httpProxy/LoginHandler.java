package com.proxy.httpProxy;

import com.utils.PasswordChecker;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author xiaoming
 * 检测http代理是否登录，验证账号密码
 */
@ChannelHandler.Sharable
public class LoginHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(LoginHandler.class);

    static LoginHandler INSTANCE = new LoginHandler();

    private LoginHandler() {
    }

    //验证通过则移除this，fire消息
    //否则，发送验证指令
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        FullHttpRequest request = (FullHttpRequest) msg;
        if (PasswordChecker.digestLogin(request)) {
            ctx.pipeline().remove(this);
            ctx.fireChannelRead(msg);
        } else {
            ReferenceCountUtil.release(msg);
            ctx.writeAndFlush(PasswordChecker.getDigestNotLoginResponse());
        }
    }

}
