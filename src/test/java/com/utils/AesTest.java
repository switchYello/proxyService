package com.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;


public class AesTest {

    @Test
    public void testAes128Gsm() throws GeneralSecurityException {
        byte[] content = KeyUtil.randomBytes(5 * 1024);
        byte[] origin = content.clone();
        Aes128Gcm aes = new Aes128Gcm();
        byte[] key = KeyUtil.createHkdfKey("abc", KeyUtil.randomBytes(aes.getSaltSize()), aes.getKeySize());
        byte[] randomIv = KeyUtil.randomBytes(aes.getNonceSize());
        ByteBuf encoder = aes.encoder(key, randomIv, Unpooled.wrappedBuffer(content));
        //加密后的数据长度等于 原始长度 + tagLength
        Assert.assertEquals(encoder.readableBytes(), content.length + aes.getTagSize());
        ByteBuf decoder = aes.decoder(key, randomIv, encoder);
        Assert.assertEquals(origin.length, decoder.readableBytes());
        for (int i = 0; i < origin.length; i++) {
            Assert.assertEquals(origin[i], decoder.getByte(i));
        }
    }

    @Test
    public void testAes192Gsm() throws GeneralSecurityException {
        byte[] content = KeyUtil.randomBytes(50);
        byte[] origin = content.clone();
        AbstractAesGcm aes = new Aes192Gcm();
        byte[] key = KeyUtil.createHkdfKey("abc", KeyUtil.randomBytes(aes.getSaltSize()), aes.getKeySize());
        byte[] randomIv = KeyUtil.randomBytes(aes.getNonceSize());
        ByteBuf encoder = aes.encoder(key, randomIv, Unpooled.wrappedBuffer(content));
        //加密后的数据长度等于 原始长度 + tagLength
        Assert.assertEquals(encoder.readableBytes(), content.length + aes.getTagSize());
        ByteBuf decoder = aes.decoder(key, randomIv, encoder);
        Assert.assertEquals(origin.length, decoder.readableBytes());
        for (int i = 0; i < origin.length; i++) {
            Assert.assertEquals(origin[i], decoder.getByte(i));
        }
    }

    @Test
    public void testAes256Gsm() throws GeneralSecurityException {
        byte[] content = KeyUtil.randomBytes(50);
        byte[] origin = content.clone();
        AbstractAesGcm aes = new Aes256Gcm();
        byte[] key = KeyUtil.createHkdfKey("abc", KeyUtil.randomBytes(aes.getSaltSize()), aes.getKeySize());
        byte[] randomIv = KeyUtil.randomBytes(aes.getNonceSize());
        ByteBuf encoder = aes.encoder(key, randomIv, Unpooled.wrappedBuffer(content));
        //加密后的数据长度等于 原始长度 + tagLength
        Assert.assertEquals(encoder.readableBytes(), content.length + aes.getTagSize());
        ByteBuf decoder = aes.decoder(key, randomIv, encoder);
        Assert.assertEquals(origin.length, decoder.readableBytes());
        for (int i = 0; i < origin.length; i++) {
            Assert.assertEquals(origin[i], decoder.getByte(i));
        }
    }

    //这是从网上找的gcm方式代码
    @Test
    public void testGCm() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(192);
            SecretKey secretKey = kg.generateKey();
            System.out.println("密钥：" + Base64.encodeBase64String(secretKey.getEncoded()));
            //自己new一个key也可以,但密钥必须满足个数要求
            SecretKey key = new SecretKeySpec(secretKey.getEncoded(), "AES");

            String txt = "testtxt";
            Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv = cipher.getIV();
            assert iv.length == 12;

            byte[] encryptData = cipher.doFinal(txt.getBytes());
            //加密后的长度等于原始长度 + 16 ?
            assert encryptData.length == txt.getBytes().length + 16;
            //返回密文等于 iv+ 加密后的数据
            byte[] message = new byte[12 + txt.getBytes().length + 16];
            System.arraycopy(iv, 0, message, 0, 12);
            System.arraycopy(encryptData, 0, message, 12, encryptData.length);
            //加密得到 iv + 数据
            System.out.println("加密后：" + Base64.encodeBase64String(message));

            if (message.length < 12 + 16) throw new IllegalArgumentException();

            GCMParameterSpec params = new GCMParameterSpec(128, message, 0, 12);
            Cipher decoder = Cipher.getInstance("AES/GCM/PKCS5Padding");
            decoder.init(Cipher.DECRYPT_MODE, key, params);
            byte[] decryptData = decoder.doFinal(message, 12, message.length - 12);
            System.out.println("解密后：" + new String(decryptData));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //这是从网上找的cfb方式代码
    @Test
    public void testCfb() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            //初始化密钥生成器，AES要求密钥长度为128位、192位、256位
            kg.init(256);
            SecretKey secretKey = kg.generateKey();
            System.out.println("密钥：" + Base64.encodeBase64String(secretKey.getEncoded()));
            SecretKey key = new SecretKeySpec(secretKey.getEncoded(), "AES");
            String txt = "testtxt";
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptData = cipher.doFinal(txt.getBytes());
            System.out.println("加密后：" + Base64.encodeBase64String(encryptData));

            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptData = cipher.doFinal(encryptData);
            System.out.println("解密后：" + new String(decryptData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}