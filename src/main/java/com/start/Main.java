package com.start;

import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        Context context = new Context("param.properties");
        context.start();

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
