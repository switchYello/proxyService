package com.handlers;

import com.utils.KeyUtil;
import io.netty.buffer.*;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Rc4HandlerTest {

    /*不参与测试，热机用的*/
    @Before
    public void a1hotVm() throws InterruptedException {
        int runCount = 10;
        final CountDownLatch endCountDown = new CountDownLatch(runCount);
        for (int i = 0; i < runCount; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] bytes = KeyUtil.randomBytes(1024 * 1024);
                    testRc4HandlerSpeed(bytes);
                    endCountDown.countDown();
                }
            }).start();
        }
        endCountDown.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testRc4Speed() throws InterruptedException {
        int runCount = 10;
        final CountDownLatch startCountDown = new CountDownLatch(1);
        final CountDownLatch endCountDown = new CountDownLatch(runCount);
        final AtomicLong data = new AtomicLong();
        final AtomicLong time = new AtomicLong();
        //开启5个线程同时执行加解密，执行完成统计凭据速度
        for (int i = 0; i < runCount; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] bytes = KeyUtil.randomBytes(5 * 1024 * 1024);
                    try {
                        startCountDown.await();
                    } catch (InterruptedException ignored) {
                    }
                    data.addAndGet(bytes.length);
                    long longs = testRc4HandlerSpeed(bytes);
                    time.addAndGet(longs);
                    endCountDown.countDown();
                }
            }).start();
        }
        //开始程序并等待执行完成
        startCountDown.countDown();
        endCountDown.await();
        /*
         * 1M=1000KB=1000*1000B
         * 1s=1000毫秒=1000*1000微秒=1000*1000*1000纳秒
         * */
        System.out.println("rc4解析速度约等于:" + data.longValue() * 1000.0 / time.longValue() + "MB/s");
    }

    private long testRc4HandlerSpeed(byte[] bytes) {
        EmbeddedChannel channel = new EmbeddedChannel(new Rc4Handler());
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