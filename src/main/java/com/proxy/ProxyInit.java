package com.proxy;

import com.proxy.ss.SsInitializer;
import com.start.Environment;
import com.utils.Conf;
import com.utils.Encrypt;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

/*
 * 切换初始化程序
 * */
@ChannelHandler.Sharable
public class ProxyInit extends ChannelInitializer<Channel> {

    //private final static SslContext context = ContextSSLFactory.getSslContextService();
    @Override
    public final void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        Conf conf = Environment.gotConfFromChannel(ch);
        //连接超时
        p.addLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS){
            @Override
            protected IdleStateEvent newIdleStateEvent(IdleState state, boolean first) {
                return super.newIdleStateEvent(state, first);
            }
        });
        //加密解密方式
        p.addLast(new LoggingHandler("ss客户端请求流 密文"));
        p.addLast(Encrypt.get(conf.getEncrypt()));
        //p.addLast("ssl", new SslHandler(context.newEngine(ByteBufAllocator.DEFAULT)));
        p.addLast(new LoggingHandler("ss客户端请求流明文"));
        //p.addLast(new SocksProxyInit());
        //p.addLast(new HttpProxyInit());
        p.addLast(new SsInitializer());

    }

}
