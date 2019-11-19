package com.utils;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Rc4Test {
    /*
     * 使用流式rc4，来处理加密解密
     * 为了安全，真实使用的密钥是 md5(md5(passwoed) + iv),比弱密码更安全一点
     * 这就是rc4md5
     * */
    @Test
    public void testRc4() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        //使用随机密码，随机iv生成key
        byte[] key = KeyUtil.md5IvKey(KeyUtil.randomBytes(16), KeyUtil.randomBytes(12));
        Rc4Md5 rc4Md5 = new Rc4Md5();
        for (int i = 0; i < 10; i++) {
            byte[] context = KeyUtil.randomBytes(50);
            byte[] encoder = rc4Md5.encoder(key, context);
            byte[] decoder = rc4Md5.decoder(key, encoder);
            Assert.assertArrayEquals(context, decoder);
        }
        rc4Md5.finishDecoder();
        rc4Md5.finishEncoder();
    }


}