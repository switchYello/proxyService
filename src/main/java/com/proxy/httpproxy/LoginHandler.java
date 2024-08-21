package com.proxy.httpproxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaoming
 * 检测http代理是否登录，验证账号密码
 */
@Slf4j
@AllArgsConstructor
public class LoginHandler extends ChannelInboundHandlerAdapter {

    private String userName;
    private String passWord;

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
        if (!PasswordChecker.digestLogin(request, userName, passWord)) {
            ReferenceCountUtil.release(msg);
            ctx.writeAndFlush(PasswordChecker.getDigestNotLoginResponse());
        }
        ctx.pipeline().remove(this);
        log.info("当前的引用数:{}", ReferenceCountUtil.refCnt(msg));
        ctx.fireChannelRead(msg);
    }

}