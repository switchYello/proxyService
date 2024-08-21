package com.proxy.httpproxy;

import com.handlers.TimeOutHandler;
import com.start.Environment;
import com.utils.Loops;
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

    Connection rightConn;
    InetSocketAddress targetAddress;
    Step step = Step.INIT;


    private void changeStep(Step step) {
        this.step = step;
    }

    @Override
    public void accept(Connection leftConn) {
        leftConn.addHandlerLast(new TimeOutHandler(600, 600, 0)); //增加超时handler
        leftConn.addHandlerLast("httpcode", new HttpServerCodec()); // 解析解码http请求，编码http响应，提供聚合能力
        leftConn.addHandlerLast("objectAggregator", new HttpObjectAggregator(1024 * 1024));
        leftConn.addHandlerLast(LoginHandler.INSTANCE); //登陆密码验证

        leftConn.inbound().receive().concatMap(msg -> {
                    switch (step) {
                        case INIT: {
                            FullHttpRequest req = (FullHttpRequest) msg;
                            targetAddress = resolveHostPort(req.headers().get("Host"));
                            //连接目标地址
                            return getConn(targetAddress.getHostName(), targetAddress.getPort()).doOnNext(sub -> {
                                this.rightConn = sub;
                        /*
                          https的情况,响应客户端的代理请求，后续都是透传因此移除http相关的handler
                         */
                                if (HttpMethod.CONNECT.equals(req.method())) {
                                    FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(200, "OK"));
                                    leftConn.outbound().sendObject(resp).then(Mono.fromRunnable(() -> {
                                        leftConn.removeHandler("httpcode");
                                        leftConn.removeHandler("objectAggregator");
                                    }));
                                }
                        /*
                          http的情况，将原包转发给客户端，后续同样都是转发
                         */
                                else {
                                    req.headers().remove("Proxy-Authorization").remove("Proxy-Connection").add("Connection", "keep-alive");
                                    rightConn.addHandlerLast("httpRequestEncoder", new HttpRequestEncoder());
                                    rightConn.outbound().sendObject(req).then(Mono.fromRunnable(() -> {
                                        rightConn.removeHandler("httpRequestEncoder");
                                        leftConn.removeHandler("httpcode");
                                        leftConn.removeHandler("objectAggregator");
                                    }));
                                }

                                //绑定右侧数据到左侧
                                rightConn.inbound()
                                        .receive()
                                        .retain()
                                        .concatMap(data -> leftConn.outbound().sendObject(data), 0)
                                        .checkpoint()
                                        .subscribe(leftConn.disposeSubscriber());
                                changeStep(Step.TRANS);
                            });
                        }
                        case TRANS: {
                            ReferenceCountUtil.retain(msg);
                            return rightConn.outbound().sendObject(msg);
                        }
                        default:
                            return Mono.error(new IllegalArgumentException("未知状态"));
                    }
                }, 0)
                .checkpoint()
                .then()
                .subscribe(null, e -> {
                    log.error("http proxy connection to client fail", e);
                    leftConn.dispose();
                });
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
        return TcpClient.newConnection().runOn(Loops.ssLoopResources).wiretap("HTTp-PROXY-CLIENT", Environment.level, Environment.format).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000).host(host).port(port).connect().single();
    }
}
