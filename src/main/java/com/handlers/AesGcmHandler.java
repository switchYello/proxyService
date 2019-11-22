package com.handlers;

import com.start.Environment;
import com.utils.Aes;
import com.utils.Conf;
import com.utils.KeyUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

import static com.handlers.AesGcmHandler.DecoderStatus.ERR;
import static com.handlers.AesGcmHandler.DecoderStatus.READ_DATA;
import static com.handlers.AesGcmHandler.DecoderStatus.READ_LENGTH;

public class AesGcmHandler extends ByteToMessageCodec<ByteBuf> {

    private Aes aes;
    private String password = "";

    public AesGcmHandler(Aes aes) {
        this.aes = aes;
    }

    //限制每次encode最大大小，不能超过此值，否则需要拆包
    private static int limitDataLength = 0x3FFF;

    //是否是初次编码，初次编码需要发送salt到对面
    private boolean firstEncode = true;
    //编码nonce递增值
    private long encoderIndex = 0;
    //解码nonce递增值
    private long decoderIndex = 0;

    private byte[] encodeKey;
    //解码用到的key，每个连接用一个
    private byte[] decodeKey;

    //解码读取的数据长度，存储于此，在数据不足需要等待数据到来时，保存数据长度
    private int dataLength;
    //解码当前状态
    private DecoderStatus decoderStatus = DecoderStatus.FIRST;


    enum DecoderStatus {
        /*初次读取，前saltLength位是盐*/
        FIRST,
        /*准备读取长度*/
        READ_LENGTH,
        /*已经读取长度了，开始读取数据*/
        READ_DATA,
        /*错误阶段，如解码出来的长度超过上限*/
        ERR
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Conf conf = Environment.gotConfFromChannel(ctx.channel());
        if (conf != null) {
            password = conf.getPassWord();
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {

        if (firstEncode) {
            byte[] encodeSalt = KeyUtil.randomBytes(aes.getSaltSize());
            out.writeBytes(encodeSalt);
            encodeKey = KeyUtil.createHkdfKey(password, encodeSalt, aes.getKeySize());
            firstEncode = false;
        }
        //此处一直到将所有数据加密完才行
        while (msg.readableBytes() > 0) {
            //读取数据，但不能超过上限
            byte[] origin = readByte(msg, Math.min(limitDataLength, msg.readableBytes()));
            int payloadLength = origin.length;
            byte[] encryptedPayloadLength = aes.encoder(encodeKey, getEncodeNonce(), Unpooled.copyShort(payloadLength).array());
            byte[] encryptedPayload = aes.encoder(encodeKey, getEncodeNonce(), origin);
            out.writeBytes(encryptedPayloadLength).writeBytes(encryptedPayload);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (decoderStatus) {
//            如果是第一次访问，则读取前saltLength位作为salt保存起来
            case FIRST: {
                if (in.readableBytes() < aes.getSaltSize()) {
                    return;
                }
                byte[] salt = readByte(in, aes.getSaltSize());
                decodeKey = KeyUtil.createHkdfKey(password, salt, aes.getKeySize());
                checkDecoderStatus(READ_LENGTH);
            }
//            如果是读取数据长度阶段，则读取前 2 + tagLength位，并解密
            case READ_LENGTH: {
                //出第一次外以后非第一次都是以【*数据长度short* 数据长度tag *负载数据* 负载数据tag】这样的形式组成的
                //获取两位的长度数据和加密它的tag数据
                if (in.readableBytes() < 2 + aes.getTagSize()) {
                    return;
                }
                byte[] payloadLenTagAndLen = readByte(in, 2 + aes.getTagSize());
                //解密并获取数据长度
                byte[] decoder = aes.decoder(decodeKey, getDecodeNonce(), payloadLenTagAndLen);
                dataLength = (0xff & decoder[0]) << 8 | (0xff & decoder[1]);
                if (dataLength > limitDataLength) {
                    //这里长度超过限制，说明数据错误的，忽略接下来的所有数据
                    checkDecoderStatus(ERR);
                    break;
                }
                checkDecoderStatus(READ_DATA);
            }
//            如果是此状态，则说明已经读取了数据长度 dataLength变量一定有值
            case READ_DATA: {
                if (in.readableBytes() < dataLength + aes.getTagSize()) {
                    return;
                }
                byte[] payloaData = readByte(in, dataLength + aes.getTagSize());
                byte[] decoder2 = aes.decoder(decodeKey, getDecodeNonce(), payloaData);
                out.add(Unpooled.wrappedBuffer(decoder2));
                checkDecoderStatus(READ_LENGTH);
                break;
            }
            case ERR: {
                in.skipBytes(in.readableBytes());
            }
        }
    }

    //读取指定长度的byte[],并让读索引前进指定长度位
    private byte[] readByte(ByteBuf in, int length) {
        byte[] bytes = ByteBufUtil.getBytes(in, in.readerIndex(), length);
        in.skipBytes(bytes.length);
        return bytes;
    }

    //这里从0递增nonce，也就是解密用的iv，共12位固定的，采用无符号的little-endian
    //这里用一个long类型代替，因为递增量不会超过一个long
    private byte[] getDecodeNonce() {
        ByteBuf b = Unpooled.buffer(12).writeLongLE(decoderIndex);
        decoderIndex++;
        return b.array();
    }

    private byte[] getEncodeNonce() {
        ByteBuf b = Unpooled.buffer(12).writeLongLE(encoderIndex);
        encoderIndex++;
        return b.array();
    }

    /*转换decode状态*/
    private void checkDecoderStatus(DecoderStatus status) {
        decoderStatus = status;
    }

}
