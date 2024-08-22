package com;

import com.start.Environment;
import com.utils.Loops;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * @author huangchaoyu
 * @since 2024/8/22 15:27
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        Mono<? extends Connection> connect = TcpClient.newConnection()
                .runOn(Loops.ssLoopResources)
                .wiretap("SS-CLIENT", LogLevel.INFO, AdvancedByteBufFormat.HEX_DUMP)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Environment.GLOBAL_TIMEOUT)
                .host("127.0.0.1")
                .port(50000)
                .connect();
        Connection conn = connect.block();

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeCharSequence("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", StandardCharsets.UTF_8);


        conn.outbound()
                .sendObject(buffer)
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        throw new RuntimeException("xxx");
                    }

                    @Override
                    public void onNext(Void unused) {
                        conn.dispose();
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("com");
                    }
                })

        ;

        Thread.sleep(1000000);
    }
}
