package com.utils;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class KeyUtil {

    private static SecureRandom rand = new SecureRandom();

    /*
        名称	                别名	                密钥大小 盐大小     nonce大小	 tag大小
        AEAD_CHACHA20_POLY1305	chacha20-ietf-poly1305	32	      32        12	        16
        AEAD_AES_256_GCM	    AES-256-gcm	            32	      32	    12	        16
        AEAD_AES_192_GCM	    AES-192-gcm	            24	      24	    12	        16
        AEAD_AES_128_GCM	    AES-128-gcm	        16	      16	    12	        16
    */
    private final static byte[] info = "ss-subkey".getBytes();

    /*
     * 创建hkdf协议使用的key
     * */
    public static Key createHkdfKey(String password, byte[] salt, int keySize) throws GeneralSecurityException {
        byte[] hmacsha1s = com.google.crypto.tink.subtle.Hkdf
                .computeHkdf("HMACSHA1", exPassword(password.getBytes(StandardCharsets.UTF_8), 16), salt, info, keySize);
        return new SecretKeySpec(hmacsha1s, "AES");
    }

    /*
     * 根据简单密钥和iv构建复杂密钥
     *  md5(md5(password) + iv)
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

    private static byte[] exPassword(byte[] password, int length) {
        int i = 0;
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
        return result;
    }

}
