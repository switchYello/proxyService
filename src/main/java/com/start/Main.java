package com.start;


import com.proxy.forwarder.ForwardHandler;
import com.proxy.ss.SSHandler;
import com.utils.Conf;
import com.utils.Symbols;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpServer;

@Slf4j
public class Main {
    private static LoopResources loopResources = LoopResources.create("FORWARD-SERVER");

    public static void main(String[] args) {
        try {
            for (Conf conf : Environment.loadConfs()) {
                if (!conf.getEnable()) {
                    continue;
                }
                switch (conf.getMode()) {
                    case Symbols.SS:
                        startSsMode(conf);
                        log.info("启动SS成功:{}", conf);
                        break;
                    case Symbols.FORWARD:
                        startForwardMode(conf);
                        log.info("启动FORWARD成功:{}", conf);
                        break;
                    default:
                        throw new RuntimeException("unKnow mode " + conf.getMode());
                }
            }
        } catch (Exception e) {
            log.info("main方法报错", e);
        }
    }

    //启动ss服务器端
    private static void startSsMode(final Conf conf) {
        TcpServer ts = TcpServer.create()
                .runOn(loopResources)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childAttr(Conf.CONF_KEY, conf)
                .doOnConnection(new ForwardHandler())
                .wiretap("SS-SERVER", Environment.level, Environment.format)
                .host("0.0.0.0")
                .port(conf.getLocalPort());
        ts.warmup().block();
        ts.bindNow().onDispose().publishOn(Schedulers.boundedElastic()).block();
    }

    private static void startForwardMode(final Conf conf) {
        TcpServer ts = TcpServer.create()
                .runOn(loopResources)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childAttr(Conf.CONF_KEY, conf)
                .doOnConnection(new SSHandler())
                .wiretap("FORWARD-SERVER", Environment.level, Environment.format)
                .host("0.0.0.0")
                .port(conf.getLocalPort());
        ts.warmup().block();
        ts.bindNow().onDispose().publishOn(Schedulers.boundedElastic()).block();
    }

}
