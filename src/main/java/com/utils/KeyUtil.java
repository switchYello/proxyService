package com.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HashMap;
import java.util.Map;

/*
 * 生成各种各样的key
 * */
public class KeyUtil {

    private static final Logger log = LoggerFactory.getLogger(KeyUtil.class);
    private static SecureRandom rand = new SecureRandom();
    private static Map<String, Map<Integer, byte[]>> exPasswoedCache = new HashMap<>();
    /*
        名称	                别名	                密钥大小 盐大小     nonce大小	 tag大小
        AEAD_CHACHA20_POLY1305	chacha20-ietf-poly1305	32	      32        12	        16
        AEAD_AES_256_GCM	    AES-256-gcm	            32	      32	    12	        16
        AEAD_AES_192_GCM	    AES-192-gcm	            24	      24	    12	        16
        AEAD_AES_128_GCM	    AES-128-gcm	            16	      16	    12	        16
    */

    private final static byte[] info = "ss-subkey".getBytes();

    /*
     * 创建hkdf协议使用的key
     * aes gcm使用这种方法创建的key
     *
     * 注意如果给定的password长度太小，需要使用exPassword方法扩大到算法需要的长度
     * */
    public static byte[] createHkdfKey(String password, byte[] salt, int keySize) throws GeneralSecurityException {
        byte[] prk = hkdfExtract(salt, exPassword(password, keySize));
        return hkdfExpand(prk, info, keySize);
    }

    //hkdf 提取  ork = extract(salt,ikm)
    private static byte[] hkdfExtract(byte[] salt, byte[] passsword) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(salt, "HmacSHA1");
        mac.init(keySpec);
        return mac.doFinal(passsword);
    }

    //扩展 key = Expand(prk,info,length)
    private static byte[] hkdfExpand(byte[] prk, byte[] info, int length) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(prk, "HmacSHA1");
        mac.init(keySpec);
        byte[] result = new byte[length];
        int pos = 0;
        byte[] digest = new byte[0];
        byte t = 1;
        while (pos < result.length) {
            mac.update(digest);
            mac.update(info);
            mac.update(t);
            digest = mac.doFinal();
            System.arraycopy(digest, 0, result, pos, Math.min(digest.length, length - pos));
            pos += digest.length;
            t++;
        }
        return result;
    }


    /*
     * 根据简单密钥和iv构建复杂密钥
     *  md5(md5(password) + iv)
     * rc4-md5使用这种方式生成密码
     */
    public static byte[] md5IvKey(byte[] originPassword, byte[] iv) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        byte[] md5Password = md5.digest(originPassword);
        md5.reset();
        md5.update(md5Password);
        md5.update(iv);
        return md5.digest();
    }

    /*随机生成指定大小的byte数组*/
    public static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        rand.nextBytes(bytes);
        return bytes;
    }

    /*
     * 通过算法，将password扩大到指定长度
     * */
    public static byte[] exPassword(String passwordStr, int length) {
        Map<Integer, byte[]> intMap = exPasswoedCache.get(passwordStr);
        if (intMap == null) {
            intMap = new HashMap<>();
            exPasswoedCache.put(passwordStr, intMap);
        }
        byte[] bytes = intMap.get(length);
        if (bytes != null) {
            log.debug("exPassword走缓存 passwoed:{},length:{}", passwordStr, length);
            return bytes;
        }
        //当前生成密钥长度
        int pos = 0;
        byte[] password = passwordStr.getBytes(StandardCharsets.UTF_8);
        ByteBuf result = Unpooled.buffer(length);
        do {
            byte[] hash = DigestUtils.md5(password);
            result.writeBytes(hash, 0, Math.min(hash.length, length - pos));
            pos += hash.length;
            //如果一次md5生成的hash数量不够，则 拷贝 hash+原始密码，再次hash
            password = ByteBufUtil.getBytes(Unpooled.wrappedBuffer(hash, password));
        } while (pos < length);
        intMap.put(length, result.array());
        return intMap.get(length);
    }

}
