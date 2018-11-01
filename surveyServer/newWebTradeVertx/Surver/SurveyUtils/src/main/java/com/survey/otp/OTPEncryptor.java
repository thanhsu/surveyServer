package com.survey.otp;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.survey.utils.Log;

public class OTPEncryptor {
	/**
	 * 
	 * @param algorithm
	 *            (MD2, MD5, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512)
	 * @param str
	 *            is String will be encrypt
	 * @return String encrypted.
	 */
	public static String algorithm = "MD5";

	public static String encryptPassword(String str) {
		String result = new String();
		MessageDigest mStr;

		try {
			mStr = MessageDigest.getInstance(algorithm);
			mStr.update(str.getBytes(), 0, str.length());
			result = new BigInteger(1, mStr.digest()).toString(16);

		} catch (NoSuchAlgorithmException e) {
			Log.print("encrypt Error: " + e.toString(), Log.ERROR_LOG);
		}
		return result;
	}

	public static boolean checkOTP(String input, String systemPassword) {
		String encrypt = encryptPassword(input);
		return encrypt.equals(systemPassword) ? true : false;
	}
}
