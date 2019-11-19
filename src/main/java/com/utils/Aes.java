package com.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.security.GeneralSecurityException;
import java.security.Key;

/**
 * hcy 2019/11/17
 */
public abstract class Aes implements AesInfo {

    private static String algorithm = "AES/GCM/PKCS5Padding";
    private Cipher encoderChipher;
    private Cipher decoderChipher;

    private Status encodeStatus = Status.none;
    private Status decodeStatus = Status.none;

    /*
     *Aes运行状态
     */
    enum Status {
        /*未new,处于null状态*/
        none,
        /*new出来了，但未初始化，或使用完成了，等待下次重用*/
        init,
        /*编码状态*/
        encode,
        /*解码状态*/
        decode;
    }

    //加密得到的结果等于 iv + 加密后的数据
    public byte[] encoder(Key key, byte[] iv, byte[] content) throws GeneralSecurityException {
        switch (encodeStatus) {
            case none: {
                encoderChipher = Cipher.getInstance(algorithm);
                encodeStatus = Status.init;
            }
            case init: {
                GCMParameterSpec params = new GCMParameterSpec(128, iv);
                encoderChipher.init(Cipher.ENCRYPT_MODE, key, params);
                encodeStatus = Status.encode;
            }
            case encode: {
                //加密后的数据长度等于原始据长度 + tagLength
                byte[] encryptData = encoderChipher.doFinal(content);
                encodeStatus = Status.init;
                return encryptData;
            }
            default:
                throw new RuntimeException("aes 加密器状态不正确 encodeStatus = " + encodeStatus);
        }
    }

    /*
     * 解密 加密函数返回的数据 为 12位iv + 加密后的数据
     * @param encoderByte
     */
    public byte[] decoder(Key key, byte[] iv, byte[] encoderByte) throws GeneralSecurityException {
        switch (decodeStatus) {
            case none: {
                decoderChipher = Cipher.getInstance(algorithm);
                decodeStatus = Status.init;
            }
            case init:
                //128, 120, 112, 104, 96
                GCMParameterSpec params = new GCMParameterSpec(128, iv);
                //根据初始化key 和 向量进行初始化chipher
                decoderChipher.init(Cipher.DECRYPT_MODE, key, params);
                decodeStatus = Status.decode;
            case decode: {
                byte[] bytes = decoderChipher.doFinal(encoderByte);
                decodeStatus = Status.init;
                return bytes;
            }
            default:
                throw new RuntimeException("aes 解密器状态不正确 decodeStatus = " + decodeStatus);
        }
    }

}
