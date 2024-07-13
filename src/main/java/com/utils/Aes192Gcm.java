package com.utils;

/**
 * hcy 2019/11/17
 */
public class Aes192Gcm extends AbstractAesGcm {


	@Override
	public int getKeySize() {
		return 24;
	}

	@Override
	public int getSaltSize() {
		return 24;
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
