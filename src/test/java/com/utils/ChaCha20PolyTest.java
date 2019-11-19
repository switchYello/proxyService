package com.utils;

import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.subtle.ChaCha20Poly1305;
import org.junit.Test;

import java.security.GeneralSecurityException;

import static org.junit.Assert.assertArrayEquals;

public class ChaCha20PolyTest {

    @Test
    public void test1() throws GeneralSecurityException {
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) i;
        }

        ChaCha20Poly1305 cha = new ChaCha20Poly1305(key);
        String text = "aaa";
        byte[] encrypt = cha.encrypt(text.getBytes(), null);
        byte[] decrypt = cha.decrypt(encrypt, null);
        assertArrayEquals(decrypt, text.getBytes());

    }
    @Test
    public void test2() throws GeneralSecurityException {
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) i;
        }
        //KeysetHandle.generateNew(KeyTemplate.)

        ChaCha20Poly1305 cha = new ChaCha20Poly1305(key);
        String text = "aaa";
        byte[] encrypt = cha.encrypt(text.getBytes(), null);
        byte[] decrypt = cha.decrypt(encrypt, null);
        assertArrayEquals(decrypt, text.getBytes());

    }

}
