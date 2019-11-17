package com.utils;

import com.start.Environment;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Rc4Md5 {

	private static String name = "RC4";
	//	密钥 ,整个程序过程中只初始化一次
	private static byte[] password = DigestUtils.md5(Environment.getPassWord().getBytes(StandardCharsets.UTF_8));
	//iv  真实使用的密码是 md5(md5(password) + iv)
	private byte[] iv;

	private Cipher encoder;
	private Cipher decoder;

	public Rc4Md5(byte[] iv) {
		this.iv = iv;
	}

	public byte[] getIv() {
		return iv;
	}

	public byte[] encoder(byte[] content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		if (encoder == null) {
			initEncoderChipher();
		}
		return encoder.update(content);
	}

	public byte[] decoder(byte[] content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		if (decoder == null) {
			initDecoderChipher();
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

	private void initEncoderChipher() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		encoder = Cipher.getInstance(name);
		encoder.init(Cipher.ENCRYPT_MODE, getKey(password, iv));
	}

	private void initDecoderChipher() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		decoder = Cipher.getInstance(name);
		decoder.init(Cipher.DECRYPT_MODE, getKey(password, iv));
	}

	private static Key getKey(byte[] password, byte[] iv) {
		MessageDigest md5 = DigestUtils.getDigest(MessageDigestAlgorithms.MD5);
		md5.update(password);
		md5.update(iv);
		return new SecretKeySpec(md5.digest(), name);
	}
}
