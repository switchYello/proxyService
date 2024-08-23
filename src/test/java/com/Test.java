package com;

import com.start.Environment;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.util.ReferenceCountUtil;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

public class Test {

    public static void main(String[] args) {
        LoopResources loopResources = LoopResources.create("demo", 1, 1, true);

        // client
        Mono<? extends Connection> connect = TcpClient.newConnection()
                .wiretap("demo", LogLevel.INFO, AdvancedByteBufFormat.HEX_DUMP)
                .runOn(loopResources)
                .option(ChannelOption.TCP_NODELAY, true)
                .host("bing.com")
                .port(443)
                .connect();
        Connection client = connect.block();

        //service
        TcpServer service = TcpServer.create()
                .runOn(loopResources)
                .option(ChannelOption.AUTO_READ, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .doOnConnection(subConn -> {
                    subConn.inbound().receiveObject().concatMap(data -> {

                                ByteBuf copy = ((ByteBuf) data).copy();

                                return client.outbound().sendObject(copy);
                            }, 0)
                            .checkpoint()
                            .subscribe(subConn.disposeSubscriber());

                })
                .wiretap("SERVER", Environment.LEVEL, Environment.FORMAT)
                .host("0.0.0.0")
                .port(1024);

        //block
        service.bindNow().onDispose().block();
    }
}
