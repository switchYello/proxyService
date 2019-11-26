package com.start;


import com.proxy.forwarder.ForwarderInitializer;
import com.proxy.ss.SsInitializer;
import com.utils.Conf;
import com.utils.SuccessFutureListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workGroup = new NioEventLoopGroup(1);

    public static void main(String[] args) {
        Main main = new Main();
        try {
            for (Conf conf : Environment.getConfs()) {
                if (!conf.getEnable()) {
                    continue;
                }
                switch (conf.getMode()) {
                    case "ss":
                        main.startSsMode(conf);
                        break;
                    case "forward":
                        main.startForwardMode(conf);
                        break;
                    default:
                        throw new RuntimeException("unknow mode " + conf.getMode());
                }
            }
        } catch (Exception e) {
            log.info("main方法报错", e);
            main.close();
        }

    }

    //启动ss服务器端
    private void startSsMode(final Conf conf) throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.SO_LINGER, 1)
                .childOption(ChannelOption.AUTO_READ, false)
                .childAttr(Conf.conf_key, conf)
                .childHandler(new SsInitializer());
        ChannelFuture f = b.bind("0.0.0.0", conf.getLocalPort());
        f.addListener(new SuccessFutureListener<Void>() {
            @Override
            public void operationComplete0(Void v) {
                log.info("start ss:{}", conf);
            }
        });
        f.channel().closeFuture().addListener(new SuccessFutureListener<Void>() {
            @Override
            public void operationComplete0(Void future) {
                log.info("ss server close");
            }
        });
    }

    private void startForwardMode(final Conf conf) throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.SO_LINGER, 1)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childOption(ChannelOption.AUTO_READ, false)
                .childAttr(Conf.conf_key, conf)
                .childHandler(ForwarderInitializer.INSTANCE);
        ChannelFuture f = b.bind("0.0.0.0", conf.getLocalPort());
        f.addListener(new SuccessFutureListener<Void>() {
            @Override
            public void operationComplete0(Void v) {
                log.info("start forward:{}", conf);
            }
        });
        f.channel().closeFuture().addListener(new SuccessFutureListener<Void>() {
            @Override
            public void operationComplete0(Void future) {
                log.info("forward server close");
            }
        });
    }

    public void close() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully();
        }
        log.info("server close !");
    }


}
