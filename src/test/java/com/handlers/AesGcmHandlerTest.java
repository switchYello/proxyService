package com.handlers;

import com.utils.Aes128Gcm;
import com.utils.Aes192Gcm;
import com.utils.Aes256Gcm;
import com.utils.KeyUtil;
import io.netty.buffer.*;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

/**
 * hcy 2019/11/17
 * aes gsm加密解密为
 * 原文 data
 * 密码 p 长度见各方法的keySize，真正使用时 p = hkdf(p,sale,info) 使用算法Hkdf生成更安全的密码
 * iv 参与加密偏移量，长度为12位，加密解密使用相同的iv才能成功,ss使用iv为从[0 0 0 0... 0 ]每次使用后递增1的方式下次为[1 0 0 ... 0]
 * 密文 e 根据上面原文、密码、iv生成的密文，密文长度 = 原文长度 + tag长度，每种算法tag长度不一致
 * e = encode(data,p,iv)   其中  e长度 = data长度 + tag长度
 * 解码
 * data = decode(p,iv，e), 解密方法使用加密相同的 p 和iv解密密文，得到原文
 * #
 * hkdf算法是，防止原始密码位数太低，导致加密安全性太低时，使用此算法扩大密码长度
 * hkdf(password,salt,info) - >hkdf_password
 * 使用原始密码，随机salt，关联信息info，生成位数更多，更随机的hkdf_password,要生成相同的密钥要保证三个参数均相同
 */

/*
 * ss用到的协议如下，以aes128gcm举例，不同算法使用上的区别就是盐和密钥长度不一样而已
 * tcp传输协议
 * 第一个tcp包
 * salt，length，lengthTag，data，dataTag
 * 16      2        16      可变    16
 * 第二个以及后面的tcp包为
 * length ，lengthTag， data ，dataTag
 * 2          16       可变    16
 * <p>
 * 我们拿到第一个tcp包时
 * 1.首先读取前16位获取  salt
 * 使用hkdf算法,用原始密钥，salt，固定字符串“ss-subkey”计算出加密使用的key =  hkdf(password,salt,"ss-subkey")
 * 2.向后读取18位 encrypted，这18位数据为2位encrypted_payload_length + 16位encrypted_payload_length_tag
 * 3.使用解密函数解密 dataLength = decode(key,iv,encrypted),
 * ...参数key为第1步生成的key
 * ...参数iv是12位数组，从[0 0 ... 00 0]开始按照大端的方式每次使用后都递增一次，第二次是[1 0  .... 0],第三次是[3 0 ... 0 0]这样递增
 * ...参数encrypted_payload是读取到的18位密文
 * 。。返回值dataLength是解密后的2位数组如[0,10],当成short来看，表示后面数据的真实长度
 * 4.根据第三步的获取 dataLength + 16 向后读取密文，encrypted
 * 5.data = decode(key,iv,encrypted) 解密
 * ..参数key为第一步生成的
 * ..参数iv上面用过一次，现在变成12位byte数组[1 0 0 ...0 0 0]
 * ..参数encrypted为读取到的dataLength + 16长度的密文
 * 解密出来则为真实数据
 * 6.以后该连接传输的数据均按照下面格式进行，也就是说除第一次带salt外其他都不带salt
 * [encrypted payload length][length tag][encrypted payload][payload tag]
 *
 */


public class AesGcmHandlerTest {


    @Test
    public void testAes256GcmSpeed() {
        testAesGcmSpeed(new EmbeddedChannel(new AesGcmHandler(new Aes256Gcm())), KeyUtil.randomBytes(1024 * 1024), null);
        testAesGcmSpeed(new EmbeddedChannel(new AesGcmHandler(new Aes256Gcm())), KeyUtil.randomBytes(5 * 1024 * 1024), "Aes256GcmSpeed");
    }

    @Test
    public void testAes192GcmSpeed() {
        testAesGcmSpeed(new EmbeddedChannel(new AesGcmHandler(new Aes192Gcm())), KeyUtil.randomBytes(1024 * 1024), null);
        testAesGcmSpeed(new EmbeddedChannel(new AesGcmHandler(new Aes192Gcm())), KeyUtil.randomBytes(5 * 1024 * 1024), "Aes192GcmSpeed");
    }
    
    @Test
    public void testAes128GcmSpeed() {
        testAesGcmSpeed(new EmbeddedChannel(new AesGcmHandler(new Aes128Gcm())), KeyUtil.randomBytes(1024 * 1024), null);
        testAesGcmSpeed(new EmbeddedChannel(new AesGcmHandler(new Aes128Gcm())), KeyUtil.randomBytes(5 * 1024 * 1024), "Aes128GcmSpeed");
    }

    private void testAesGcmSpeed(EmbeddedChannel channel, byte[] bytes, String name) {
        long startTime = System.nanoTime();
        //写入out通道，对数据进行加密
        Assert.assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(bytes)));
        //将数据读出，分片成多个
        //将加密的数据重新写入inBound,进行解密
        Object o;
        while ((o = channel.readOutbound()) != null) {
            channel.writeInbound(o);
        }
        //从inBound将解密结果读取聚合起来
        CompositeByteBuf byteBufs = new CompositeByteBuf(ByteBufAllocator.DEFAULT, false, 32);
        Object o2;
        while ((o2 = channel.readInbound()) != null) {
            ByteBuf b = (ByteBuf) o2;
            byteBufs.addComponent(true, b);
        }
        //断言解密后的数据和原数据相同
        Assert.assertArrayEquals(bytes, ByteBufUtil.getBytes(byteBufs));
        Assert.assertTrue(byteBufs.release());
        Assert.assertFalse(channel.finish());
        if (name != null) {
            System.out.println(name + "encoder decoder speed:" + bytes.length * 1000.0 / (System.nanoTime() - startTime) + "MB/s");
        }
    }

}