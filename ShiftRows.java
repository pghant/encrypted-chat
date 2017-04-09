package com.chatsecure.aes.main;

import com.chatsecure.aes.utilities.*;

public class ShiftRows {
	
	public static void shift(int[][] array2D) {
        for (int index = 1; index < array2D.length; index++) {
        	array2D[index] = Common.rotateLeft(array2D[index], index);
        }
    }
	
    
	
}
