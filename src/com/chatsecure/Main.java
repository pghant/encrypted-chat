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

    private static void findK(int L) {
        // K is the minimum number >= 0 such that L + 1 + K + 64 is a multiple of 512

        // message needs to be multiple of 512 bits long (i.e. multiple of 64 bytes long)
        // first append new byte 0x80 to message
        // next append enough 0 bytes such that length + 8 is divisible by 64
        // next append L as 8 bytes
    }

    public static void main(String[] args) {
        int L = 174; // bytes
        System.out.println((int)Math.ceil((L + 9.0) / 64) * 64 - (L + 9));
    }
}
