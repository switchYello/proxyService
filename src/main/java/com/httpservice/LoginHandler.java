package com.httpservice;

import com.start.Context;
import com.start.Environment;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

//获取登录信息及ip和端口和验证
//short表示host长度 + host + short表示端口

public class LoginHandler extends ReplayingDecoder<Void> {

    private static Logger log = LoggerFactory.getLogger(LoginHandler.class);
    private static Collection<String> write = Collections.synchronizedCollection(new ArrayList<>());
    private Environment environment = Context.getEnvironment();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        RequestEntity requestEntity = new RequestEntity(byteBuf);
        String remote = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName();
        if (requestEntity.requestType == 1) {
            if (write.contains(remote)) {
                list.add(requestEntity.inetSocketAddress);
                ctx.pipeline().remove(this);
            } else {
                checkpoint();
                log.debug("无此IP需要验证 remote:{},link:{}", remote, requestEntity.inetSocketAddress);
                ctx.writeAndFlush(Unpooled.wrappedBuffer(new byte[]{2}));
            }
        } else if (requestEntity.requestType == 2) {
            if (checkToken(requestEntity, remote)) {
                write.add(remote);
                list.add(requestEntity.inetSocketAddress);
                ctx.pipeline().remove(this);
            } else {
                writeReject(ctx);
            }
        } else {
            writeReject(ctx);
        }
    }

    private static class RequestEntity {
        private byte requestType;
        private InetSocketAddress inetSocketAddress;
        private long timeStamp;
        private String token;

        RequestEntity(ByteBuf buf) {
            requestType = buf.readByte();
            if (requestType == 1) {
                short hostPortLength = buf.readShort();
                String host = buf.readBytes(hostPortLength - 2).toString(StandardCharsets.UTF_8);
                short port = buf.readShort();
                inetSocketAddress = InetSocketAddress.createUnresolved(host, port);
            } else if (requestType == 2) {
                //请求类型byte + 时间戳 + token长度short + token + （host port）长度short + host + port
                timeStamp = buf.readLong();
                short tokenLength = buf.readShort();
                String token = buf.readBytes(tokenLength).toString(StandardCharsets.UTF_8);
                short hostPortLength = buf.readShort();
                String host = buf.readBytes(hostPortLength - 2).toString(StandardCharsets.UTF_8);
                short port = buf.readShort();
                inetSocketAddress = InetSocketAddress.createUnresolved(host, port);
                this.token = token;
            }
        }
    }

    //返回一个空http响应
    private static void writeReject(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast(new HttpResponseEncoder());
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set("Content-Type", "text/plain;charset=utf-8");
        response.headers().setInt("Content-Length", response.content().readableBytes());
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    //检查token是否有效
    private boolean checkToken(RequestEntity entity, String remote) {

        if (System.currentTimeMillis() - entity.timeStamp > 5000) {
            log.debug("验证失败拒绝连接:remote{} timeStamp失效", remote);
            return false;
        }
        boolean equals = entity.token.equals(DigestUtils.md5Hex(entity.timeStamp + environment.getRemoteSalt()));
        if (equals) {
            return true;
        } else {
            log.debug("验证失败拒绝连接:remote{} token不正确", remote);
            return false;
        }
    }

}
