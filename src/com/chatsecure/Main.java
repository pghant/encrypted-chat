package com.chatsecure;

public class Main {

    public static void hashStringBuiltIn(String toHash) {
        byte[] bytes = SHA512.hash(toHash.getBytes());
        StringBuilder sb = new StringBuilder();
        printByteArray(bytes, null);
        for(int i=0; i< bytes.length; i++){
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        System.out.println(sb.toString());
    }

    public static void hashStringNew(String toHash) {
        byte[] bytes = SHA512.hash256(toHash.getBytes());
        StringBuilder sb = new StringBuilder();
        printByteArray(bytes, null);
        for(int i=0; i< bytes.length; i++){
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
        int L = 174; // bytes
        String toHash = "this is a test messagethis is a test messagethis is a test messagethis is a test messagethis is a test message";
        hashStringBuiltIn(toHash);
        hashStringNew(toHash);
    }

    private static void printByteArray(byte[] array, String title) {
        if (title != null && title != "")
            System.out.print(title + ": ");
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + ", ");
        }
        System.out.println();
    }
}
