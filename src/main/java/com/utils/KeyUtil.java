package com.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HashMap;
import java.util.Map;

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
     * */
    public static byte[] createHkdfKey(String password, byte[] salt, int keySize) throws GeneralSecurityException {
        byte[] hmacsha1s = com.google.crypto.tink.subtle.Hkdf
                .computeHkdf("HMACSHA1", exPassword(password, 16), salt, info, keySize);
        return hmacsha1s;
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


    private static byte[] exPassword(String passwordStr, int length) {
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

        int i = 0;
        byte[] password = passwordStr.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[length];
        while (true) {
            byte[] hash = DigestUtils.md5(password);
            System.arraycopy(hash, 0, result, i, hash.length);
            i += hash.length;
            if (i >= length) {
                break;
            }
            byte[] temp = new byte[hash.length + password.length];
            System.arraycopy(hash, 0, temp, 0, hash.length);
            System.arraycopy(password, 0, temp, hash.length, password.length);
            password = temp;
        }
        intMap.put(length, result);
        return result;
    }

}
