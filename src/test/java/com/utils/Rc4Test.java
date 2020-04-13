package com.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Rc4Test {
    /*
     * 使用流式rc4，来处理加密解密
     * 为了安全，真实使用的密钥是 md5(md5(passwoed) + iv),比弱密码更安全一点
     * 这就是rc4md5
     * */
    @Test
    public void testRc4() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, ShortBufferException {
        //使用随机密码，随机iv生成key
        byte[] key = KeyUtil.md5IvKey(KeyUtil.randomBytes(16), KeyUtil.randomBytes(12));
        Rc4Md5 rc4Md5 = new Rc4Md5();
        for (int i = 0; i < 10; i++) {
            ByteBuf data = Unpooled.wrappedBuffer(KeyUtil.randomBytes(50));
            ByteBuf origin = data.duplicate();

            ByteBuf outData = rc4Md5.encoder(key, data);
            Assert.assertEquals(origin.readableBytes(),outData.readableBytes());

            ByteBuf decoder = rc4Md5.decoder(key, outData);
            Assert.assertEquals(origin.readableBytes(),decoder.readableBytes());

            for(int index = decoder.readerIndex();index<decoder.writerIndex();index++){
                Assert.assertEquals(origin.getByte(index),decoder.getByte(index));
            }
        }
        rc4Md5.finishDecoder();
        rc4Md5.finishEncoder();
    }

    /**
     * 测试java自带的rc4加密解密
     */
    @Test
    public void testRc4Origin() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        //随机生成20位密码
        String password = randomString(20);
        //需要加密的字符串
        String content = randomString(50);
        //用于加密的
        Cipher encoder = Cipher.getInstance("RC4");
        //初始化成加密模式
        encoder.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(password.getBytes(), "RC4"));

        //用于解密的
        Cipher decoder = Cipher.getInstance("RC4");
        //初始化成解密模式
        decoder.init(Cipher.DECRYPT_MODE, new SecretKeySpec(password.getBytes(), "RC4"));

        //密文
        byte[] update = encoder.update(content.getBytes());
        //解密后
        byte[] update1 = decoder.update(update);

        //加密在解密后和原文一致
        Assert.assertEquals(content, new String(update1, StandardCharsets.UTF_8));

        //因为是流加密，只要不关闭，encoder decoder对象可以一直使用
        String content2 = randomString(50);
        //密文
        byte[] en = encoder.update(content2.getBytes());
        //解密后
        byte[] de = decoder.update(en);
        //加密在解密后和原文一致
        Assert.assertEquals(content2, new String(de, StandardCharsets.UTF_8));

    }

    private static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int rand = random.nextInt('z' - 'a' + 1) + 'a';
            sb.append((char) rand);
        }
        return sb.toString();
    }


}