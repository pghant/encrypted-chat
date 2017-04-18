/**
 * 
 */
package com.chatsecure.aes;
import com.chatsecure.aes.*;
/**
 * @author cstevens
 *
 */
public class ECB {
	private static int[][] keyArray;
	public static boolean setKey(byte[] key) throws Exception{
		if(key.length != 32)
			return false;

		keyArray = KeyGen.generate(key.toString());

		return true;
	}
	
	public static byte[] encrypt(byte[] inputArray) throws Exception{
		int numRounds = 10;
		int[][] subKey = new int[4][4];
		
		subKey = KeyGen.getSubKey(0);
		int[][] stateAES = parseByteArrayToStateMatrix(inputArray);
		for (int i = 1; i < numRounds; i++) {

			SubBytes.sub(stateAES);
			ShiftRows.shift(stateAES);
			MixColumns.mix(stateAES);

			subKey = KeyGen.getSubKey(i);
			AddRoundKey.add(stateAES, subKey);
		}

		SubBytes.sub(stateAES);
		ShiftRows.shift(stateAES);
		AddRoundKey.add(stateAES, subKey);
		
		return new byte[0];
	}
	private static int[][] parseByteArrayToStateMatrix(byte[] plainhexText) throws Exception {
		String subText = "";
		int[][] state = new int[4][4];
		
		if(plainhexText.length != 32) {
			throw new Exception("Please have the length of plain hex text be 16 bytes. Two hex strings represent one byte.");
		}
		
		

		for (int i = 0; i < 4; i++) // Parses line into a matrix
		{
			for (int j = 0; j < 4; j++) {
				subText = plainhexText.toString().substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2));
				state[j][i] = Integer.parseInt(subText, 16);
			}
		}

		return state;
	}
}
