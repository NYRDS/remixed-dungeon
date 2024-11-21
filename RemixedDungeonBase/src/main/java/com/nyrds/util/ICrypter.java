package com.nyrds.util;

public interface ICrypter {
    void setEncryptionKey(String encryptionKey);
    String encrypt(String plainText);
    String decrypt(String encrypted);
}
