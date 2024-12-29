package com.nyrds.platform.util;

import com.nyrds.platform.EventCollector;

import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypter {
	private final String encryptionKey;

	public Crypter(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}

	public String encrypt(String plainText) {
		Cipher cipher;
		try {
			cipher = getCipher(Cipher.ENCRYPT_MODE);
			byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
			return Base64.getEncoder().encodeToString(encryptedBytes);

		} catch (Exception e) {
			EventCollector.logException(e);
		}
		return plainText;
	}

	public String decrypt(String encryptedBase64) {
		Cipher cipher;
		try {
			cipher = getCipher(Cipher.DECRYPT_MODE);
			byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
			byte[] plainBytes = cipher.doFinal(encryptedBytes);
			return new String(plainBytes);

		} catch (Exception e) {
			EventCollector.logException(e);
		}
		return encryptedBase64;
	}

	private Cipher getCipher(int cipherMode) throws Exception {
		String encryptionAlgorithm = "AES";

		byte[] salt = {
				(byte) 0x95, (byte) 0xaa, (byte) 0x21, (byte) 0x8c,
				(byte) 0xa9, (byte) 0xc8, (byte) 0xfe, (byte) 0x99
		};

		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		// deepcode ignore HardcodedSecret~javax.crypto.spec.PBEKeySpec: <please specify a reason of ignoring this>
		KeySpec spec = new PBEKeySpec(encryptionKey.toCharArray(), salt, 1, 256);
		// deepcode ignore CipherModeWithNoIntegrity: <please specify a reason of ignoring this>
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

		Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
		cipher.init(cipherMode, secret);
		return cipher;
	}
}