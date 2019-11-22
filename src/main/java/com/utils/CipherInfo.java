package com.utils;

/*
 * 加密策略的基本信息
 * */
public interface CipherInfo {

    int getKeySize();

    int getSaltSize();

    int getNonceSize();

    int getTagSize();


}
