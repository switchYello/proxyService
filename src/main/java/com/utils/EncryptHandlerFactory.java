package com.utils;

import com.handlers.AesGcmHandler;
import com.handlers.Rc4Handler;
import io.netty.channel.ChannelHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据算法名称 查找加密算法handler
 */
public class EncryptHandlerFactory {

    private final static Map<String, HandlerFactory> map = new HashMap<>();

    static {
        map.put("rc4-md5", new Rc4HandlerFactory());
        map.put("aes-128-gcm", new Aes128GcmHandlerFactory());
        map.put("aes-192-gcm", new Aes192GcmHandlerFactory());
        map.put("aes-256-gcm", new Aes256GcmHandlerFactory());
    }

    public static ChannelHandler createChannelHandler(String encryptMethod) {
        HandlerFactory handlerFactory = map.get(encryptMethod);
        if (handlerFactory == null) {
            throw new RuntimeException("加密方式'" + encryptMethod + "'不支持,[" + map.keySet() + "]");
        }
        return handlerFactory.createHandler();
    }

    interface HandlerFactory {
        ChannelHandler createHandler();
    }

    private static class Rc4HandlerFactory implements HandlerFactory {
        @Override
        public ChannelHandler createHandler() {
            return new Rc4Handler();
        }
    }

    private static class Aes128GcmHandlerFactory implements HandlerFactory {
        @Override
        public ChannelHandler createHandler() {
            return new AesGcmHandler(new Aes128Gcm());
        }
    }

    private static class Aes192GcmHandlerFactory implements HandlerFactory {
        @Override
        public ChannelHandler createHandler() {
            return new AesGcmHandler(new Aes192Gcm());
        }
    }

    private static class Aes256GcmHandlerFactory implements HandlerFactory {
        @Override
        public ChannelHandler createHandler() {
            return new AesGcmHandler(new Aes256Gcm());
        }
    }

}
