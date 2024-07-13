package com.utils;

import com.google.crypto.tink.subtle.Hkdf;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


public class KeyUtilTest {

    @Test
    public void testHkdfCreater() throws GeneralSecurityException {
        String password = "abcdefghijklmn";
        byte[] sale = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
        int length = 64;
        byte[] info = "ss-subkey".getBytes();
        byte[] myHkdf = KeyUtil.createHkdfKey(password, sale, length);
        byte[] tinkHkdf = Hkdf.computeHkdf("HmacSha1", KeyUtil.exPassword(password, length), sale, info, length);
        assertArrayEquals(myHkdf, tinkHkdf);
    }

}