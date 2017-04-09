package com.chatsecure;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
        int L = 174; // bytes
        String toHash = "this is a test messagethis is a test messagethis is a test messagethis is a test messagethis is a test message";
//        SHA512.hash256(toHash.getBytes());

        ByteBuffer buffer = ByteBuffer.allocate(32).order(ByteOrder.BIG_ENDIAN);
        byte[] chunk = {(byte)0x80, (byte)0x00, (byte)0x00, (byte)0x01};
        for (int l = 0; l < 4; l++) {
            buffer.put(chunk[l]);
//            chunkIndex++;
        }
        int testVal = buffer.getInt(0);
        System.out.println(testVal);
        System.out.println(Integer.toBinaryString(testVal));
        int testVal2 = 0x900F0000;
        System.out.println(testVal2);
        System.out.println(Integer.toBinaryString(testVal2));
        int testVal3 = testVal >>> 5;
        System.out.println(Integer.toBinaryString(testVal3));

        System.out.println(Integer.toBinaryString(testVal ^ testVal2));
    }
}
