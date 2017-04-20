package com.chatsecure.aes;
import com.chatsecure.*;
import java.nio.ByteBuffer;
import java.util.*;

public class CTR {
	private static int messageId = 0;
	private static int block_size = 16;
	private static int hash_size = 64;
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
		byte[] randIV = new byte[block_size];
		byte[] encIV;
		int IV = 0;
		byte[] rand = new byte[8];
		int alignedLength = msg_.length + block_size- (msg_.length % block_size);
		byte[] encMsg = new byte[block_size+block_size+alignedLength + hash_size];
		random.nextBytes(randIV);
		random.nextBytes(rand);
	    ByteBuffer bb = ByteBuffer.allocate(4);
	    //copy the plaintext IV
	    bb.putInt(IV);
	    System.out.println("Encrypt:");
	    System.out.println(Arrays.toString(msg_));
	    System.arraycopy( bb.array(), 0, randIV, 12, bb.array().length );
	    System.arraycopy( randIV, 0, encMsg, 0, randIV.length );
	    //copy the random bytes at the start of the encryption section of the buffer
	    System.arraycopy( rand, 0, encMsg, block_size, rand.length );
	    bb.clear();
	    bb.putInt(msg_.length);
	    //this copies the length into the first part of the encryption
	    System.arraycopy( bb.array(), 0, encMsg, 24, bb.array().length );
	    bb.clear();
	    bb.putInt(messageId);
	    System.arraycopy(bb.array(), 0, encMsg, 28, bb.array().length);
		messageId++;
	    System.arraycopy(msg_, 0, encMsg, 32, msg_.length);
	    //create hash
	    //copy hash to the message
	    System.arraycopy(hash, 0, encMsg, 32+alignedLength, hash.length);

	    //add HASH to end of message
	    hash = SHA512.hash(msg_);
	    System.arraycopy(hash, 0, encMsg, encMsg.length - hash.length, hash.length);
		//start encrypting the IV and xoring it with the buffer
		for(int iter = 0; iter < alignedLength + hash.length; iter+=16){
			encIV = ECB.encrypt(randIV);
			//Encrypt IV
			int i = 0;
			for (byte b : encIV){
				//look at comment on top as to why this is done
				//NOTE: Add comment on top why this is done
			    encMsg[block_size+iter+i] = (byte) (b ^ encMsg[block_size+iter+i]);
			    i++;
			}
			IV++;
			bb.clear();
			bb.putInt(IV);
		    System.arraycopy( bb.array(), 0, randIV, 12, bb.array().length );
		}
		System.out.println(Arrays.toString(encMsg));
		return encMsg;
	}
	public static byte[] decryptMessage(byte[] encMsg_) throws Exception{
		Random random = new Random();
		byte [] newHash = new byte[hash_size];
		byte [] origHash = new byte[hash_size];
		byte[] randIV = new byte[block_size];
		byte[] encIV;
		byte[] randLengthId = new byte[block_size];
		int IV = 0;
		byte[] rand = new byte[8];
		int msgLength;
		int alignedLength;
		byte[] msg;
		random.nextBytes(randIV);
		random.nextBytes(rand);
	    ByteBuffer bb = ByteBuffer.allocate(4);
	    //get the initial IV
	    System.arraycopy( encMsg_, 0, randIV, 0, randIV.length );
	    //decrypt the length and message id
	    //encrypt XOR
	    int i = 0;
	    encIV = ECB.encrypt(randIV);
		for (byte b : encIV){
			//look at comment on top as to why this is done
			//NOTE: Add comment on top why this is done
			randLengthId[i] = (byte) (b ^ encMsg_[block_size+i]);
			i++;
		}
		//System.out.println(Arrays.toString(randLengthId));
	    //this copies the length into the first part of the encryption
	    System.arraycopy( randLengthId, 8, bb.array(), 0, bb.array().length );
	    msgLength = bb.getInt();
	    msg = new byte[msgLength];
	    alignedLength = msgLength + block_size- (msgLength % block_size);
	    System.arraycopy(randLengthId, 12, bb.array(), 0, bb.array().length);
	    bb.clear();
	    //bb.putInt(messageId);
	    messageId = bb.getInt();
	    messageId++;
	    bb.clear();
	    System.out.println(randIV);
		System.arraycopy( randIV, 12, bb.array(), 0, bb.array().length ); 
		IV = bb.getInt();
		IV = IV +1;
		bb.clear();
		
		bb.putInt(IV);
		System.out.println("RandIV");
		System.out.println(randIV);
	    System.arraycopy( bb.array(), 0, randIV, 12, bb.array().length );   
	    System.out.println(randIV);
		//start encrypting the IV and xoring it with the buffer
	    System.out.println(Integer.toString(alignedLength));
		for(int iter = 0; iter < alignedLength; iter+=16){
			//Encrypt IV
			//System.out.println(randIV);
			encIV = ECB.encrypt(randIV);
			i = 0;
			for (byte b : randIV){
				//look at comment on top as to why this is done
				//NOTE: Add comment on top why this is done
				if(msgLength != 0){
					msg[iter+i] = (byte) (b ^ encMsg_[31+iter+i]);
					msgLength--;
					i++;
				}
			}
			IV++;
			bb.clear();
			bb.putInt(IV);
		    System.arraycopy( bb.array(), 0, randIV, 12, bb.array().length );   
		}
		//DECRYPT HASH FROM END OF buffer
		//encrypt the current randIV
		i = 0;
		encIV = ECB.encrypt(randIV);
		for (byte b : encIV){
			//look at comment on top as to why this is done
			//NOTE: Add comment on top why this is done
			origHash[i] = (byte) (b ^ encMsg_[31+alignedLength+i]);
			i++;
		}
		System.out.println("Decrypt:");
		System.out.println(Arrays.toString(msg));
		//Verify message
		newHash = SHA512.hash(msg);
		if(Arrays.equals(newHash, origHash)){
			String response = "Invalid Hash";
			msg = null;
			msg = response.getBytes();
		}
		return msg;
	}

}
