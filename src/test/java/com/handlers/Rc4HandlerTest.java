package com.handlers;

import com.utils.KeyUtil;
import io.netty.buffer.*;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/*
以下摘自ss官网，稍微修改
*
加解密流程
1.生成密钥  key_gene(originPassword,IV) == > key
代码例子见 com.utils.KeyUtil.md5IvKey(byte[] originPassword, byte[] iv)
其中的originPassword为用户输入密钥，IV:如果是解密操作会出现在第一次接收数据的前x位，如果是加密操作则生成随机iv并放在流的最前面
2.加密Stream_encrypt(key,message) => ciphertext
使用1中生成的密钥加密明文文本,见com.utils.Rc4Md5.encoder
3.解密 Stream_decrypt(key, ciphertext) => message
使用1中生成的密钥解密密文 com.utils.Rc4Md5.decoder
！！上述操作在单个连接传输过程中，密钥是唯一的
#
TCP解密后报文格式，首个包有IV，后面的包就只有密文了
[IV][encrypted payload]
加密也是同理，首个包要将生成的IV放在流的最前面
#
UDP 和tcp一样
[IV][encrypted payload]
#
#
上面的IV，每个连接都重新随机生成（不是每个数据包是每个连接）
*
* */
public class Rc4HandlerTest {

    /*不参与测试，热机用的*/
    @Before
    public void a1hotVm() throws InterruptedException {
        byte[] bytes = KeyUtil.randomBytes(5 * 1024 * 1024);
        EmbeddedChannel channel = new EmbeddedChannel(new Rc4Handler());
        testRc4HandlerSpeed(bytes, channel);
    }

    @Test
    public void testRc4Speed() throws InterruptedException {

        byte[] bytes = KeyUtil.randomBytes(50 * 1024 * 1024);
        EmbeddedChannel channel = new EmbeddedChannel(new Rc4Handler());
        long longs = testRc4HandlerSpeed(bytes, channel);
        /*
         * 1M=1000KB=1000*1000B
         * 1s=1000毫秒=1000*1000微秒=1000*1000*1000纳秒
         * */
        System.out.println("rc4解析速度约等于:" + bytes.length * 1000.0 / longs + "MB/s");
    }

    private long testRc4HandlerSpeed(byte[] bytes, EmbeddedChannel channel) {

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
//       断言解密后的数据和原数据相同
        Assert.assertArrayEquals(bytes, ByteBufUtil.getBytes(byteBufs));
        Assert.assertTrue(byteBufs.release());
        Assert.assertFalse(channel.finish());
        return System.nanoTime() - startTime;
    }


}