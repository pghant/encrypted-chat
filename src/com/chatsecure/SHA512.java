package com.chatsecure;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Contains logic to perform a SHA-512 hash on a byte[]
 * Using pseudocode from Wikipedia (https://en.wikipedia.org/wiki/SHA-2)
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

    public static byte[] hash256(byte[] toHash) {
        // initialize hash values
        // (first 32 bits of the fractional parts of the square roots of the first 8 primes 2..19):
        int h0 = 0x6a09e667;
        int h1 = 0xbb67ae85;
        int h2 = 0x3c6ef372;
        int h3 = 0xa54ff53a;
        int h4 = 0x510e527f;
        int h5 = 0x9b05688c;
        int h6 = 0x1f83d9ab;
        int h7 = 0x5be0cd19;

        // Initialize array of round constants:
        // (first 32 bits of the fractional parts of the cube roots of the first 64 primes 2..311):
        int[] k = {
            0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
            0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
            0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
            0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
            0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
            0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
            0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
            0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};

        /*
        Message Preprocessing:
        message needs to be multiple of 512 bits long (i.e. multiple of 64 bytes long)
        first append new byte 0x80 to message
        next append enough 0 bytes such that length + 8 is divisible by 64
        next append L as 8 bytes
         */

        // length of original message
        long length = toHash.length;
        long KtoAppend = (int)Math.ceil((length + 9.0) / 64) * 64 - (length + 9); // append these many 0 bytes after appending 0x80 byte
        long messageLength = length + 9 + KtoAppend;
        byte[] message = new byte[(int)messageLength]; // new message that can be split into 64 byte chunks
        // convert length of original message to byte[]
        byte[] lengthInBytes = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(length).array();
        int i;
        for (i = 0; i < length; i++) {
            message[i] = toHash[i];
        }
        message[i] = (byte) 0x80;
        i += KtoAppend + 1;
        for (int j = 0; j < 8; j++) {
            message[i] = lengthInBytes[j];
            i++;
        }

        // break message into 512 bit (64 byte) chunks
        byte[][] chunkedMessage = new byte[(int)messageLength / 64][64];
        for (int j = 0; j < messageLength; j++) {
            chunkedMessage[j / 64][j % 64] = message[j];
        }

        // process each message chunk
        for (byte[] chunk : chunkedMessage) {
            int[] w = new int[64]; // 64-entry message schedule array w[0..63] of 32 bit (4 byte = 1 int) words

            // copy chunk into first 16 words of w
            int chunkIndex = 0;
            for (int j = 0; j < 16; j++) {
                ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
                for (int l = 0; l < 4; l++) {
                    buffer.put(chunk[chunkIndex]);
                    chunkIndex++;
                }
                w[j] = buffer.getInt(0);
            }

            // Extend the first 16 words into the remaining 48 words w[16..63] of the message schedule array
            for (int j = 16; j <= 63; j++) {
                int s0 = Integer.rotateRight(w[j-15], 7) ^ Integer.rotateRight(w[j-15], 18) ^ (w[j-15] >>> 3);
                int s1 = Integer.rotateRight(w[j-2], 17) ^ Integer.rotateRight(w[j-2], 19) ^ (w[j-2] >>> 10);
                w[j] = w[j-16] + s0 + w[j-7] + s1;
            }

            // Initialize working variables to current hash value
            int a = h0;
            int b = h1;
            int c = h2;
            int d = h3;
            int e = h4;
            int f = h5;
            int g = h6;
            int h = h7;

            // Compression function main loop
            for (int j = 0; j < 64; j++) {
                int S1 = Integer.rotateRight(e, 6) ^ Integer.rotateRight(e, 11) ^ Integer.rotateRight(e, 25);
                int ch = (e & f) ^ ((~e) & g);
                int temp1 = h + S1 + ch + k[j] + w[j];
                int S0 = Integer.rotateRight(a, 2) ^ Integer.rotateRight(a, 13) ^ Integer.rotateRight(a, 22);
                int maj = (a & b) ^ (a & c) ^ (b & c);
                int temp2 = S0 + maj;

                h = g;
                g = f;
                f = e;
                e = d + temp1;
                d = c;
                c = b;
                b = a;
                a = temp1 + temp2;
            }

            // Add compressed chunk to current hash value
            h0 = h0 + a;
            h1 = h1 + b;
            h2 = h2 + c;
            h3 = h3 + d;
            h4 = h4 + e;
            h5 = h5 + f;
            h6 = h6 + g;
            h7 = h7 + h;
        }

        // Final hash value
        return ByteBuffer.allocate(32).order(ByteOrder.BIG_ENDIAN)
                .putInt(h0).putInt(h1).putInt(h2).putInt(h3)
                .putInt(h4).putInt(h5).putInt(h6).putInt(h7)
                .array();
    }

    private static void printByteArray(byte[] array, String title) {
        System.out.println();
        if (title != null && title != "")
            System.out.print(title + ": ");
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + ", ");
        }
    }

    private static void printByteArray(byte[][] array, String title) {
        System.out.println();
        if (title != null && title != "")
            System.out.println(title + ": ");
        for (int i = 0; i < array.length; i++) {
            System.out.print(i + ": ");
            for (int j = 0; j < array[i].length; j++) {
                System.out.print(array[i][j] + ", ");
            }
            System.out.println();
        }
    }
}
