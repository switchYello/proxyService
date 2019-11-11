package com.utils;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class Rc4 {

    private static final Map<String, Key> keyCache = new HashMap<>();
    private Cipher encoder;
    private Cipher decoder;

    public Rc4(String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        encoder = Cipher.getInstance("rc4");
        encoder.init(Cipher.ENCRYPT_MODE, getKey(password));
        decoder = Cipher.getInstance("rc4");
        decoder.init(Cipher.DECRYPT_MODE, getKey(password));
    }

    public static Key getKey(String password) throws NoSuchAlgorithmException {
        Key key = keyCache.get(password);
        if (key == null) {
            synchronized (keyCache) {
                key = keyCache.get(password);
                if (key == null) {
                    KeyGenerator keyGenerator = KeyGenerator.getInstance("rc4");
                    keyGenerator.init(256, new SecureRandom(password.getBytes(StandardCharsets.UTF_8)));
                    key = keyGenerator.generateKey();
                    keyCache.put(password, key);
                }
            }
        }
        return key;
    }

    public byte[] encoder(byte[] content) {
        return encoder.update(content);
    }

    public byte[] decoder(byte[] content) {
        return decoder.update(content);
    }

    public byte[] finshEncoder() throws BadPaddingException, IllegalBlockSizeException {
        return encoder.doFinal();
    }

    public byte[] finshDecoder() throws BadPaddingException, IllegalBlockSizeException {
        return decoder.doFinal();
    }
}
