package com.utils.algorithm;

import com.handlers.AesGcmHandler;
import com.handlers.Rc4Handler;
import io.netty.channel.ChannelHandler;

/**
 * 根据算法名称 查找加密算法handler
 */
public class EncrypAlgorithmHandlerFactory {

    public static ChannelHandler createEncryptHandler(String encryptMethod, String password) {
        switch (encryptMethod.toLowerCase()) {
            case "rc4-md5":
                return new Rc4Handler(password);
            case "aes-128-gcm":
                return new AesGcmHandler(new Aes128Gcm(), password);
            case "aes-192-gcm":
                return new AesGcmHandler(new Aes192Gcm(), password);
            case "aes-256-gcm":
                return new AesGcmHandler(new Aes256Gcm(), password);
            default:
                throw new RuntimeException("加密方式'" + encryptMethod);
        }
    }

}
