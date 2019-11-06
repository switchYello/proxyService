package com.utils;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

public class ContextSSLFactory {

    private ContextSSLFactory() {
    }

    public static SslContext getSslContextService() {
        List<String> ciphers = Arrays.asList("TLS_AES_256_GCM_SHA384", "ECDHE-RSA-AES128-SHA", "ECDHE-RSA-AES256-SHA", "AES128-SHA", "DES-CBC3-SHA");
        try {
            //加载证书
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(ResourceManager.gerResourceForFile("sChat.jks"), "4512357896".toCharArray());
            //密钥管理工厂
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, "4512357896".toCharArray());
            //信任管理器(信任列表的客户端才会被接受)
            TrustManagerFactory tf = TrustManagerFactory.getInstance("SunX509");
            tf.init(keyStore);

            return SslContextBuilder
                    .forServer(keyManagerFactory)
                    .trustManager(tf)
                    .ciphers(ciphers)
                    .protocols("TLSv1.3")
                    .clientAuth(ClientAuth.NONE) //必须校验客户端
                    .sslProvider(SslProvider.OPENSSL_REFCNT)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("无法创建SslContext");
    }


}
