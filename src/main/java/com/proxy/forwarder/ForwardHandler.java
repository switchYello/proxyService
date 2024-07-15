package com.proxy.forwarder;

import com.handlers.TimeOutHandler;
import com.start.Environment;
import com.utils.Conf;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import java.util.function.Consumer;

/**
 * @author huangchaoyu
 * @since 2024/7/15 14:01
 */
@Slf4j
public class ForwardHandler implements Consumer<Connection> {

    @Override
    public void accept(Connection conn) {
        //增加超时handler
        conn.addHandlerLast(new TimeOutHandler(30, 30, 0));

        Conf conf = Environment.getConfFromChannel(conn.channel());
        //获取客户端连接
        getConn(conf.getServerHost(), conf.getServerPort()).doOnNext(subConn -> {
                    //从客户端连接获取数据，写入服务端
                    subConn.outbound().send(conn.inbound().receive().retain()).subscribe(conn.disposeSubscriber());
                    conn.outbound().send(subConn.inbound().receive().retain()).subscribe(subConn.disposeSubscriber());
                })
                .checkpoint()
                .then()
                .subscribe(null, throwable -> {
                    conn.dispose();
                    log.error("connection to client {}:{} fail", conf.getServerHost(), conf.getServerPort(), throwable);
                });
    }

    static Mono<? extends Connection> getConn(String host, int port) {
        return TcpClient.newConnection()
                .wiretap("FORWARD-CLIENT", Environment.level, Environment.format)
                .host(host)
                .port(port)
                .connect()
                .single();
    }
}
