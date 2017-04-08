package com.chatsecure.rsa.test;

import com.chatsecure.rsa.RSAEncryption;

public class RSAEncryptionTest {
	public static void main(String args[]){
		RSAEncryption encryption = new RSAEncryption();
		System.out.println("public key is "+ encryption.getPublicKey());
		System.out.println("private key is "+ encryption.getPrivateKey());
		String encryptedText = encryption.encrypt("checking rsa encryption");
		System.out.println("encrypted text is " + encryptedText);
		String decryptedText = encryption.decrypt(encryptedText);
		System.out.println("Decrypted text is " + decryptedText);
	}

}
