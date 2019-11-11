package com.start;

import com.proxy.socks.SocksAbstractProxyInit;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(1);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                    .childHandler(new SocksAbstractProxyInit());
            ChannelFuture f = b.bind(Environment.getStartPort()).sync();
            log.info("start at :{} ", Environment.getStartPort());
            f.channel().closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    private SslContext createSSlContext() {
        try {
            String password = "123456";
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(ClassLoader.getSystemResourceAsStream(""), password.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password.toCharArray());
            SSLContext tls = SSLContext.getInstance("TLS");
            tls.init(kmf.getKeyManagers(), null, null);
        } catch (Exception e) {

        }
        return null;
    }
}
