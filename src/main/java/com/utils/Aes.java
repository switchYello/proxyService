package com.utils;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

/**
 * hcy 2019/11/17
 */
public abstract class Aes implements CipherInfo {

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
    public ByteBuf encoder(byte[] key, byte[] iv, ByteBuf content) throws GeneralSecurityException {
        switch (encodeStatus) {
            case none: {
                encoderChipher = Cipher.getInstance(algorithm);
                encodeStatus = Status.init;
            }
            case init: {
                GCMParameterSpec params = new GCMParameterSpec(128, iv);
                encoderChipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), params);
                encodeStatus = Status.encode;
            }
            case encode: {
                int outputSize = encoderChipher.getOutputSize(content.readableBytes());
                ByteBuf out = content.alloc().ioBuffer(outputSize);
                ByteBuffer outData = out.nioBuffer(0, outputSize);
                //加密后的数据长度等于原始据长度 + tagLength,,返回值为outData的长度
                try {
                    int length = encoderChipher.doFinal(content.nioBuffer(), outData);
                    encodeStatus = Status.init;
                    content.skipBytes(content.readableBytes());
                    out.writerIndex(length);
                    return out;
                } catch (Exception e) {
                    ReferenceCountUtil.release(out);
                    throw e;
                }
            }
            default:
                throw new RuntimeException("aes 加密器状态不正确 encodeStatus = " + encodeStatus);
        }
    }

    /*
     * 解密 加密函数返回的数据 为 12位iv + 加密后的数据
     * @param encoderByte
     */
    public ByteBuf decoder(byte[] key, byte[] iv, ByteBuf encoderByte) throws GeneralSecurityException {
        switch (decodeStatus) {
            case none: {
                decoderChipher = Cipher.getInstance(algorithm);
                decodeStatus = Status.init;
            }
            case init:
                //128, 120, 112, 104, 96
                GCMParameterSpec params = new GCMParameterSpec(128, iv);
                //根据初始化key 和 向量进行初始化chipher
                decoderChipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), params);
                decodeStatus = Status.decode;
            case decode: {
                int outputSize = decoderChipher.getOutputSize(encoderByte.readableBytes());
                ByteBuf out = encoderByte.alloc().ioBuffer(outputSize);
                ByteBuffer outData = out.nioBuffer(0, outputSize);
                try {
                    int length = decoderChipher.doFinal(encoderByte.nioBuffer(), outData);
                    decodeStatus = Status.init;
                    encoderByte.skipBytes(encoderByte.readableBytes());
                    out.writerIndex(length);
                    return out;
                } catch (Exception e) {
                    ReferenceCountUtil.release(out);
                    throw e;
                }
            }
            default:
                throw new RuntimeException("aes 解密器状态不正确 decodeStatus = " + decodeStatus);
        }
    }

}
