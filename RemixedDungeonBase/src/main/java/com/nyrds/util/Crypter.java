package com.nyrds.util;

public class Crypter {
	static private ICrypter impl;

	public static void init(ICrypter impl) {
		Crypter.impl = impl;
	}

	public Crypter(String encryptionKey) {
		Crypter.impl.setEncryptionKey(encryptionKey);
	}


	public String encrypt(String plainText) {
		return impl.encrypt(plainText);
	}


	public String decrypt(String encrypted) {
		return impl.decrypt(encrypted);
	}
}