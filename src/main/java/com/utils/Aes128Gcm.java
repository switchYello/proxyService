package com.utils;

/**
 * hcy 2019/11/17
 */
public class Aes128Gcm extends AbstractAesGcm {


    @Override
    public int getKeySize() {
        return 16;
    }

    @Override
    public int getSaltSize() {
        return 16;
    }

    @Override
    public int getNonceSize() {
        return 12;
    }

    @Override
    public int getTagSize() {
        return 16;
    }

}
