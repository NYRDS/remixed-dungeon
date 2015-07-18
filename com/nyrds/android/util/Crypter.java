package com.nyrds.android.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class Crypter {
	private String encryptionKey;

	public Crypter(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}

	public String encrypt(String plainText) {
		Cipher cipher;
		try {
			cipher = getCipher(Cipher.ENCRYPT_MODE);
			byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
			return android.util.Base64.encodeToString(encryptedBytes,
					Base64.DEFAULT);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return plainText;
	}

	public String decrypt(String encrypted) {
		Cipher cipher;
		try {
			cipher = getCipher(Cipher.DECRYPT_MODE);
			byte[] plainBytes = cipher.doFinal(Base64.decode(encrypted,
					Base64.DEFAULT));
			return new String(plainBytes);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encrypted;
	}

	private Cipher getCipher(int cipherMode) throws Exception {
		String encryptionAlgorithm = "AES";
		SecretKeySpec keySpecification;

		keySpecification = new SecretKeySpec(encryptionKey.getBytes("UTF-8"),
				encryptionAlgorithm);

		Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
		cipher.init(cipherMode, keySpecification);
		return cipher;
	}
}