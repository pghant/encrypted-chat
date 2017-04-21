package com.chatsecure.aes;
import com.chatsecure.*;
import java.nio.ByteBuffer;
import java.util.*;

public class CTR {
	private static int block_size = 16;
	private static int hash_size = 64;
	private static byte[] iv = new byte[block_size]; 
	private static byte[] aes_key;
	//save the key that will be used to encrypt the message
	public static boolean setkey(byte[] key_) throws Exception{
		if(key_.length == 16 || key_.length == 32){
			aes_key = new byte[key_.length];

			if(!ECB.setKey(key_)){
					return false;
			}
			return true;
		}else{
			return false;
		}
	}
	
	public static byte[] encryptMessage(byte[] msg_) throws Exception{
	    Random random = new Random();
	    byte[] hash = new byte[hash_size];
	    byte[] encIV;
	    byte[] rand = new byte[8];
	    int alignedLength = msg_.length + block_size- (msg_.length % block_size);
	    byte[] encMsg = new byte[block_size+block_size+alignedLength + hash_size];
	    byte[] plainText = new byte[block_size+alignedLength + hash_size];
	    random.nextBytes(iv);
	    random.nextBytes(rand);
	    ByteBuffer bb = ByteBuffer.allocate(4);
	    //copy the plaintext IV
	    bb.putInt(0);
	    System.arraycopy( bb.array(), 0, iv, 12, bb.array().length );	    
	    System.arraycopy( iv, 0, encMsg, 0, iv.length );
	    //copy the random bytes at the start of the encryption section of the buffer
	    System.arraycopy( rand, 0, plainText, 0, rand.length );
	    bb.clear();
	    bb.putInt(msg_.length);
	    //this copies the length into the first part of the encryption
	    System.arraycopy( bb.array(), 0, plainText, 8, bb.array().length );
	    bb.clear();
	    System.arraycopy(msg_, 0, plainText, block_size, msg_.length);
	    //create hash
	    
	    hash = SHA512.hash(msg_);
	    //add HASH to end of message
	    System.arraycopy(hash, 0, plainText, plainText.length-hash_size, hash_size);
		//start encrypting the IV and xoring it with the buffer
	    byte[] cipherText = xorSection(plainText, 0);
	    System.arraycopy(cipherText, 0, encMsg, block_size, cipherText.length);
		//System.out.println(Arrays.toString(encMsg));
	    System.out.println("");
		return encMsg;
	}
	public static byte[] decryptMessage(byte[] encMsg_) throws Exception{
	    byte [] newHash = new byte[hash_size];
	    byte [] origHash = new byte[hash_size];
	    int msgLength;
	    byte[] plainText = new byte[encMsg_.length-16];
	    ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE/8);
	    //get the initial IV
	    System.arraycopy( encMsg_, 0, iv, 0, block_size );
	    //decrypt the length and message id
	    //encrypt XOR
	    plainText = xorSection(encMsg_, 16);
		
	    //this copies the length into the first part of the encryption
	    System.arraycopy( plainText, 8, bb.array(), 0, Integer.SIZE/8 );
	    msgLength = bb.getInt();  

	    byte[] msg = new byte[msgLength];
	    System.arraycopy(plainText, block_size, msg, 0,msgLength);
	    System.out.println(msg.length);
	    System.arraycopy(plainText, plainText.length-hash_size, origHash, 0,hash_size);
	    //Verify message
	    newHash = SHA512.hash(msg);
	    if(Arrays.equals(newHash, origHash)){
	    	String response = "Invalid Hash";
		    msg = null;
		    msg = response.getBytes();
	    }
	    return msg;
	}
	private static byte[]  xorSection(byte[] cipher, int offset) throws Exception{
	    byte[] xorText = new byte[cipher.length];
	    //start encrypting the IV and xoring it with the buffer
	    updateIV(); 
		//DECRYPT HASH FROM END OF buffer
	    for(int iter = 0; iter < cipher.length-offset; iter+=16){
	    	byte[] tmpBuff = xorBuff(cipher, offset + iter);	    
	    	System.arraycopy(tmpBuff, 0, xorText, iter, tmpBuff.length);
	    }
	    return xorText;
	}

	private static void updateIV(){
		ByteBuffer bb = ByteBuffer.allocate(4);		
		int currIV;
		System.arraycopy( iv, 12, bb.array(), 0, bb.array().length );
		currIV = bb.getInt();
		currIV = currIV +1;
		bb.clear();		
		bb.putInt(currIV);
		System.arraycopy( bb.array(), 0, iv, 12, bb.array().length );
	}
	private static byte[] xorBuff( byte[] text, int offset) throws Exception{
		byte[] xorText = new byte[block_size];
		byte[] cipherIV = new byte[iv.length];
		int i = 0;
		cipherIV = ECB.encrypt(iv);
		for (byte b : cipherIV){
			//look at comment on top as to why this is done
			//NOTE: Add comment on top why this is done
			xorText[i] = (byte) (b ^ text[offset+i]);
			i++;
		}
		return xorText;
	}

}

