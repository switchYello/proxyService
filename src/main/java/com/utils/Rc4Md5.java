package com.utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Rc4Md5 implements CipherInfo {

    private static String name = "RC4";
    private Cipher encoder;
    private Cipher decoder;

    @Override
    public int getKeySize() {
        return 16;
    }

    @Override
    public int getSaltSize() {
        throw new RuntimeException("算法不支持salt");
    }

    /*这个在不同算法中也叫iv，有的叫nonce*/
    @Override
    public int getNonceSize() {
        return 16;
    }

    @Override
    public int getTagSize() {
        throw new RuntimeException("算法不支持tag");
    }

    /*
     * 根据密码和内容加密，
     * 密码是处理好的，满足条件的
     * */
    public byte[] encoder(byte[] password, byte[] content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        if (encoder == null) {
            encoder = Cipher.getInstance(name);
            encoder.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(password, name));
        }
        return encoder.update(content);
    }

    public byte[] decoder(byte[] password, byte[] content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        if (decoder == null) {
            decoder = Cipher.getInstance(name);
            decoder.init(Cipher.DECRYPT_MODE, new SecretKeySpec(password, name));
        }
        return decoder.update(content);
    }

    public void finishEncoder() throws BadPaddingException, IllegalBlockSizeException {
        if (encoder != null) {
            encoder.doFinal();
        }
    }

    public void finishDecoder() throws BadPaddingException, IllegalBlockSizeException {
        if (decoder != null) {
            decoder.doFinal();
        }
    }

}
