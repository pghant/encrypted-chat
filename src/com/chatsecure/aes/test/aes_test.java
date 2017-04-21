package com.chatsecure.aes.test;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.chatsecure.aes.*;
public class aes_test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//CTR ctr = new CTR();
		//if(test1_ctr())
		if(test1_LessThenBlockSize_ctr())
			System.out.println("Test 1 Success");
		else 
			System.out.println("Test 1 FAILURE");
		if(test2_BlockSize_ctr())
			System.out.println("Test 2 Success");
		else 
			System.out.println("Test 2 FAILURE");
		if(test3_LargerThenBlockSize_ctr())
			System.out.println("Test 3 Success");
		else 
			System.out.println("Test 3 FAILURE");
		if(test4_DifferentKey_ctr())
			System.out.println("Test 4 Success");
		else 
			System.out.println("Test 4 FAILURE");
		
		
	}

	public static boolean test1_LessThenBlockSize_ctr() throws Exception{
		
		
		byte[] test_key = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};

		CTR.setkey(test_key);
		String msg = "This is a test";
		byte[] encMsg = CTR.encryptMessage(msg.getBytes());
		CTR.setkey(test_key);
		byte[] decMsg = CTR.decryptMessage(encMsg);
		String s = new String(decMsg);
		System.out.print("Original String = ");
		System.out.println(msg);
		System.out.print("Output String = ");
		System.out.println(s);
		if(msg.equals(s))
			return true;
		return false;
	}
	public static boolean test2_BlockSize_ctr() throws Exception{
		
		
		byte[] test_key = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	
		CTR.setkey(test_key);
		String msg = "16 byte message!";
		byte[] encMsg = CTR.encryptMessage(msg.getBytes());
		CTR.setkey(test_key);
		byte[] decMsg = CTR.decryptMessage(encMsg);
		String s = new String(decMsg);
		System.out.print("Original String = ");
		System.out.println(msg);
		System.out.print("Output String = ");
		System.out.println(s);
		if(msg.equals(s))
			return true;
		return false;
	}


	public static boolean test3_LargerThenBlockSize_ctr() throws Exception{
		
		
		byte[] test_key = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	
		CTR.setkey(test_key);
		String msg = "This is a 26 byte message!";
		byte[] encMsg = CTR.encryptMessage(msg.getBytes());
		CTR.setkey(test_key);
		byte[] decMsg = CTR.decryptMessage(encMsg);
		String s = new String(decMsg);
		System.out.print("Original String = ");
		System.out.println(msg);
		System.out.print("Output String = ");
		System.out.println(s);
		if(msg.equals(s))
			return true;
		return false;
	}
	public static boolean test4_DifferentKey_ctr() throws Exception{
		
		
		byte[] test_key = {0x0A,0x0B,0x0C,0x0D,0x1E,0x2F,0x10,0x02,0x30,0x44,0x5A,0x6B,0x77,0x08,0x09,0x11};

		CTR.setkey(test_key);
		String msg = "16 byte message!";
		byte[] encMsg = CTR.encryptMessage(msg.getBytes());
		CTR.setkey(test_key);
		byte[] decMsg = CTR.decryptMessage(encMsg);
		String s = new String(decMsg);
		System.out.print("Original String = ");
		System.out.println(msg);
		System.out.print("Output String = ");
		System.out.println(s);
		if(msg.equals(s))
			return true;
		return false;
	}
}