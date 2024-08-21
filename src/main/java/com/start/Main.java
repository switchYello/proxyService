package com.start;


import com.proxy.forwarder.ForwardHandler;
import com.proxy.httpproxy.HttpProxyDataHandler;
import com.proxy.ss.SsDataHandler;
import com.utils.Conf;
import com.utils.Loops;
import com.utils.Symbols;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpServer;

import java.util.List;

@Slf4j
public class Main {
    public static void main(String[] args) {
        List<Conf> confs = Environment.loadConfs();
        Flux.fromIterable(confs)
                .filter(Conf::getEnable)
                .flatMap(conf -> {
                    switch (conf.getMode()) {
                        case Symbols.SS:
                            log.info("启动SS成功:{}", conf);
                            return startSsMode(conf);
                        case Symbols.FORWARD:
                            log.info("启动FORWARD成功:{}", conf);
                            return startForwardMode(conf);
                        case Symbols.HTTP_PROXY:
                            log.info("启动HTTP_PROXY成功:{}", conf);
                            return startHttpMode(conf);
                        default:
                            return Flux.error(new RuntimeException("unKnow mode " + conf.getMode()));
                    }
                }, confs.size())
                .then()
                .checkpoint()
                .block();
    }

    //启动ss服务器端
    private static Mono<Void> startSsMode(final Conf conf) {
        TcpServer ts = TcpServer.create()
                .runOn(Loops.ssLoopResources)
                .option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childAttr(Conf.CONF_KEY, conf)
                .doOnConnection(new SsDataHandler()) //做handlerInit后
                .wiretap("SS-SERVER", Environment.level, Environment.format)
                .host("0.0.0.0")
                .port(conf.getLocalPort());
        ts.warmup().block();
        return ts.bindNow().onDispose();
    }

    private static Mono<Void> startForwardMode(final Conf conf) {
        TcpServer ts = TcpServer.create()
                .runOn(Loops.forwardLoopResources)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childAttr(Conf.CONF_KEY, conf)
                .doOnConnection(new ForwardHandler())
                .wiretap("FORWARD-SERVER", Environment.level, Environment.format)
                .host("0.0.0.0")
                .port(conf.getLocalPort());
        ts.warmup().block();
        return ts.bindNow().onDispose();
    }

    private static Mono<Void> startHttpMode(final Conf conf) {
        TcpServer ts = TcpServer.create()
                .runOn(Loops.httpLoopResources)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childAttr(Conf.CONF_KEY, conf)
                .doOnConnection(new HttpProxyDataHandler())
                .wiretap("HTTP-PROXY-SERVER", Environment.level, Environment.format)
                .host("0.0.0.0")
                .port(conf.getLocalPort());
        ts.warmup().block();
        return ts.bindNow().onDispose();
    }

}
