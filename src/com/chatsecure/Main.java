package com.chatsecure;

public class Main {

    public static void testSHA(String toHash) {
        byte[] bytes = SHA512.hash(toHash.getBytes());
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length; i++){
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
        testSHA("a random password");
        testSHA("a random password");
        testSHA("a random password");
    }
}
