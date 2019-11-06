package com.start;


import com.httpservice.HandlerInit;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 表示整个程序上下文
 */
public class Context {
    private static Logger log = LoggerFactory.getLogger(Context.class);
    private static final String DEFAULT_ENVIRONMENT_PARAM = "param.properties";
    private Environment environment;
    private static Context contextHolder;
    private static boolean init;

    public Context() {
        this(DEFAULT_ENVIRONMENT_PARAM);
    }

    public Context(String param) {
        if (init) {
            throw new RuntimeException("alread existence Context");
        }
        log.info("param fileName:{}", param);
        this.environment = new Environment(param);
        contextHolder = this;
        init = true;
    }

    public static Context getNow() {
        if (!init) {
            throw new RuntimeException("not find contxt");
        }
        return contextHolder;
    }

    public static Environment getEnvironment() {
        if (!init) {
            throw new RuntimeException("not find contxt");
        }
        return contextHolder.environment;
    }


    public void start() {
        start(1);
    }

    public void start(int threadCount) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(threadCount);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                    .childHandler(new HandlerInit());
            ChannelFuture f = b.bind(environment.getStartPort()).sync();
            log.info("start at " + environment.getStartPort());
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
