package com.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Rc4Md5 implements CipherInfo {

    private static String name = "RC4";
    private Cipher encoder;
    private Cipher decoder;

    @Override
    public int getKeySize() {
        return 16;
    }

    @Override
    public int getSaltSize() {
        throw new RuntimeException("算法不支持salt");
    }

    /*这个在不同算法中也叫iv，有的叫nonce*/
    @Override
    public int getNonceSize() {
        return 16;
    }

    @Override
    public int getTagSize() {
        throw new RuntimeException("算法不支持tag");
    }

    /*
     * 根据密码和内容加密，
     * 密码是处理好的，满足条件的
     * */
    public ByteBuf encoder(byte[] password, ByteBuf content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, ShortBufferException {
        if (encoder == null) {
            encoder = Cipher.getInstance(name);
            encoder.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(password, name));
        }
        //得到的密文大小
        int outputSize = encoder.getOutputSize(content.readableBytes());
        //存储密文
        ByteBuf out = content.alloc().ioBuffer(outputSize);
        ByteBuffer outData = out.nioBuffer(0, outputSize);
        int update = encoder.update(content.nioBuffer(), outData);
        content.skipBytes(update);
        out.writerIndex(update);
        return out;
    }

    public ByteBuf decoder(byte[] password, ByteBuf content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, ShortBufferException {
        if (decoder == null) {
            decoder = Cipher.getInstance(name);
            decoder.init(Cipher.DECRYPT_MODE, new SecretKeySpec(password, name));
        }
        int outputSize = decoder.getOutputSize(content.readableBytes());
        ByteBuf out = content.alloc().ioBuffer(outputSize);
        ByteBuffer outData = out.nioBuffer(0, outputSize);
        int update = decoder.update(content.nioBuffer(), outData);
        content.skipBytes(update);
        out.writerIndex(update);
        return out;
    }

    public void finishEncoder() throws BadPaddingException, IllegalBlockSizeException {
        if (encoder != null) {
            encoder.doFinal();
        }
    }

    public void finishDecoder() throws BadPaddingException, IllegalBlockSizeException {
        if (decoder != null) {
            decoder.doFinal();
        }
    }

}
