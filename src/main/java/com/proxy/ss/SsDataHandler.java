package com.proxy.ss;

import com.handlers.TimeOutHandler;
import com.start.Environment;
import com.utils.Assert;
import com.utils.Conf;
import com.utils.Loops;
import com.utils.algorithm.EncrypAlgorithmHandlerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author huangchaoyu
 * @since 2024/7/15 14:01
 */
@Slf4j
public class SsDataHandler implements Consumer<Connection> {

    enum Step {
        INIT, TRANS
    }

    @Override
    public void accept(Connection conn) {
        //增加handler
        Conf conf = Environment.getConfFromChannel(conn.channel());
        conn.addHandlerLast(new TimeOutHandler(30, 30, 0));
        conn.addHandlerLast(EncrypAlgorithmHandlerFactory.createEncryptHandler(conf.getEncrypt(), conf.getPassWord()));
        conn.addHandlerLast(new SsProtocolHandler());

        AtomicReference<SsDataHandler.Step> step = new AtomicReference<>(SsDataHandler.Step.INIT);
        AtomicReference<Connection> subConnRef = new AtomicReference<>();
        conn.inbound()
                .receiveObject()
                .concatMap((Function<Object, Publisher<?>>) msg -> {
                    switch (step.get()) {
                        /**
                         * 第一条消息，是目标地址,此时创建客户端流
                         * 创建成功后，绑定读取子流写入父流中
                         */
                        case INIT: {
                            Assert.isTrue(msg instanceof InetSocketAddress, "类型不正确");
                            step.set(Step.TRANS);
                            InetSocketAddress sa = (InetSocketAddress) msg;
                            return getConn(sa.getHostName(), sa.getPort())
                                    .doOnNext(subConn -> {
                                        subConnRef.set(subConn);
                                        subConn.inbound()
                                                .receive()
                                                .retain()
                                                .concatMap(data -> conn.outbound().sendObject(data))
                                                .checkpoint()
                                                .subscribe(conn.disposeSubscriber());
                                    })
                                    .then()
                                    ;
                        }
                        /**
                         * 第二条消息后，都是数据
                         * concatMap 操作符保证了读取第二条消息时，连接一定创建成功状态
                         */
                        case TRANS: {
                            Assert.isTrue(msg instanceof ByteBuf, "类型不正确");
                            Assert.notNull(subConnRef.get(), "subConn is null");
                            ReferenceCountUtil.retain(msg);
                            return subConnRef.get().outbound().send(Mono.just((ByteBuf) msg));
//                            return subConnRef.get().outbound().sendObject(msg);
                        }
                        default: {
                            return Mono.error(new IllegalArgumentException("未知的数据类型"));
                        }
                    }
                }, 0)
                .checkpoint()
                .then()
                .subscribe(null, e -> {
                    log.error("主链接报错", e);
//                    conn.dispose();
                });
    }

    static Mono<? extends Connection> getConn(String host, int port) {
        return TcpClient.newConnection()
                .runOn(Loops.ssLoopResources)
                .wiretap("SS-CLIENT", Environment.LEVEL, Environment.FORMAT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Environment.GLOBAL_TIMEOUT)
                .host(host)
                .port(port)
                .connect()
                .single();
    }
}
