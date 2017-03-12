package com.chatsecure;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Contains logic to perform a SHA-512 hash on a byte[]
 */
public class SHA512 {
    // This is a temporary method to return a SHA-512 hash using builtin Java methods
    public static byte[] hash(byte[] toHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            return md.digest(toHash);
        } catch(NoSuchAlgorithmException e) {
            System.out.println("No such algorithm");
            return null;
        }
    }

    public static byte[] newHash(byte[] toHash) {
        return null;
    }
}
