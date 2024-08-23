package com.proxy.httpproxy;

import com.handlers.TimeOutHandler;
import com.start.Environment;
import com.utils.Conf;
import com.utils.Loops;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author huangchaoyu
 * @since 2024/8/21 11:04
 */
@Slf4j
public class HttpProxyDataHandler implements Consumer<Connection> {

    enum Step {
        INIT, TRANS
    }

    @Override
    public void accept(Connection leftConn) {
        Conf conf = Environment.getConfFromChannel(leftConn.channel());
        leftConn.addHandlerLast(new TimeOutHandler(600, 600, 0)); //增加超时handler
        leftConn.addHandlerLast("httpcode", new HttpServerCodec()); // 解析解码http请求，编码http响应，提供聚合能力
        leftConn.addHandlerLast("objectAggregator", new HttpObjectAggregator(1024 * 1024));
        leftConn.addHandlerLast(new LoginHandler(conf.getUserName(), conf.getPassWord())); //登陆密码验证

        AtomicReference<Connection> rightConn = new AtomicReference<>();
        AtomicReference<Step> step = new AtomicReference<>(Step.INIT);
        leftConn.inbound().receiveObject().concatMap(msg -> {
                    //初始加一次引用，注意释放
                    ReferenceCountUtil.retain(msg);
                    switch (step.get()) {
                        case INIT: {
                            FullHttpRequest req = (FullHttpRequest) msg;
                            InetSocketAddress targetAddress = resolveHostPort(req.headers().get("Host"));
                            //连接目标地址
                            return getConn(targetAddress.getHostName(), targetAddress.getPort()).flatMap(sub -> {
                                rightConn.set(sub);
                                //绑定右侧数据到左侧
                                rightConn.get().inbound()
                                        .receive()
                                        .retain()
                                        .concatMap(data -> leftConn.outbound().send(Mono.just(data)), 0)
                                        .checkpoint()
                                        .subscribe(leftConn.disposeSubscriber());
                                //响应左侧,并切换到传输状态
                                return responseLeft(leftConn, sub, req).doFinally(st -> step.set(Step.TRANS));
                            });
                        }
                        case TRANS: {
                            log.debug("message typ:{}", msg);
                            return rightConn.get().outbound().send(Mono.just((ByteBuf) msg));
                        }
                        default:
                            return Mono.error(new IllegalArgumentException("未知状态"));
                    }
                }, 0)
                .checkpoint()
                .then()
                .subscribe(null, e -> {
                    log.error("http proxy fail", e);
                });
    }

    private Mono<Void> responseLeft(Connection leftConn, Connection rightConn, FullHttpRequest req) {
        /*
          https的情况,响应客户端的代理请求，后续都是透传因此移除http相关的handler
         */
        if (HttpMethod.CONNECT.equals(req.method())) {
            ReferenceCountUtil.release(req);
            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(200, "OK"));
            return leftConn.outbound().sendObject(resp)
                    .then(Mono.fromRunnable(() -> {
                        leftConn.removeHandler("httpcode");
                        leftConn.removeHandler("objectAggregator");
                    })).then();
        }
        /*
          http的情况，将原包转发给客户端，后续同样都是转发
         */
        else {
            req.headers().remove("Proxy-Authorization").remove("Proxy-Connection").add("Connection", "keep-alive");
            rightConn.addHandlerLast("httpRequestEncoder", new HttpRequestEncoder());
            return rightConn.outbound().sendObject(req.retain())
                    .then(Mono.fromRunnable(() -> {
                        rightConn.removeHandler("httpRequestEncoder");
                        leftConn.removeHandler("httpcode");
                        leftConn.removeHandler("objectAggregator");
                    })).then();
        }
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
                .runOn(Loops.httpLoopResources)
                .wiretap("HTTP-PROXY-CLIENT", Environment.LEVEL, Environment.FORMAT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Environment.GLOBAL_TIMEOUT)
                .host(host)
                .port(port)
                .connect()
                .single();
    }
}
